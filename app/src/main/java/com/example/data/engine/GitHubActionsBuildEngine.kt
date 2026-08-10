package com.example.data.engine

import android.util.Base64
import android.util.Log
import com.example.data.api.GitHubApiService
import com.example.data.api.models.CreateOrUpdateFileRequest
import com.example.data.api.models.GitHubWorkflow
import com.example.data.api.models.WorkflowDispatchRequest
import com.example.data.api.models.WorkflowRun
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.util.zip.ZipInputStream

class GitHubActionsBuildEngine(private val apiService: GitHubApiService) : BuildEngine {

    override val engineName: String = "GitHub Actions Remote Runner"

    override suspend fun findWorkflows(
        token: String,
        owner: String,
        repo: String
    ): Result<List<GitHubWorkflow>> = withContext(Dispatchers.IO) {
        val authHeader = "Bearer $token"
        try {
            val resp = apiService.getWorkflows(authHeader, owner, repo)
            if (resp.isSuccessful && resp.body() != null) {
                Result.success(resp.body()!!.workflows)
            } else {
                val err = resp.errorBody()?.string() ?: "HTTP ${resp.code()}"
                Result.failure(Exception("Failed to list repository workflows: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun ensureWorkflowExists(
        token: String,
        owner: String,
        repo: String,
        branch: String
    ): Result<GitHubWorkflow> = withContext(Dispatchers.IO) {
        val authHeader = "Bearer $token"
        try {
            val workflowsResult = findWorkflows(token, owner, repo)
            val workflows = workflowsResult.getOrDefault(emptyList())

            val existingAndroidWf = workflows.find { wf ->
                wf.path.contains("android", ignoreCase = true) ||
                wf.name.contains("Android", ignoreCase = true) ||
                wf.name.contains("APK", ignoreCase = true) ||
                wf.path.endsWith(".yml", ignoreCase = true) ||
                wf.path.endsWith(".yaml", ignoreCase = true)
            }

            if (existingAndroidWf != null) {
                return@withContext Result.success(existingAndroidWf)
            }

            // Otherwise, create .github/workflows/android.yml
            val workflowContent = getStandardAndroidWorkflowYaml(branch)
            val base64Content = Base64.encodeToString(workflowContent.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

            var fileSha: String? = null
            val fileResp = apiService.getFileContent(authHeader, owner, repo, ".github/workflows/android.yml", ref = branch)
            if (fileResp.isSuccessful && fileResp.body() != null) {
                fileSha = fileResp.body()!!.sha
            }

            val createResp = apiService.createOrUpdateFile(
                authHeader, owner, repo, ".github/workflows/android.yml",
                CreateOrUpdateFileRequest(
                    message = "Add Android CI workflow",
                    contentBase64 = base64Content,
                    sha = fileSha,
                    branch = branch
                )
            )

            if (!createResp.isSuccessful) {
                val err = createResp.errorBody()?.string() ?: "HTTP ${createResp.code()}"
                return@withContext Result.failure(Exception("Failed to create workflow file: $err"))
            }

            delay(1500)
            val refreshedResult = findWorkflows(token, owner, repo)
            val refreshedWfs = refreshedResult.getOrDefault(emptyList())
            val newlyCreated = refreshedWfs.find { it.path.endsWith("android.yml") }
                ?: GitHubWorkflow(
                    id = 0L,
                    nodeId = "",
                    name = "Build Android APK",
                    path = ".github/workflows/android.yml",
                    state = "active"
                )

            Result.success(newlyCreated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getStandardAndroidWorkflowYaml(branch: String): String {
        return """
name: Build Android APK

on:
  push:
    branches: ["$branch"]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: "17"
          distribution: "temurin"

      - name: Find and setup gradlew
        shell: bash
        run: |
          set -e

          GRADLEW="${'$'}(find . -type f -name gradlew -print -quit)"

          if [ -z "${'$'}GRADLEW" ]; then
            echo "::error::No Gradle wrapper (gradlew) found in repository"
            exit 1
          fi

          chmod +x "${'$'}GRADLEW"

          echo "GRADLEW=${'$'}GRADLEW" >> "${'$'}GITHUB_ENV"

      - name: Build Debug APK
        shell: bash
        run: |
          set -o pipefail

          echo "Using Gradle wrapper: ${'$'}GRADLEW"

          PROJECT_DIR="${'$'}(dirname "${'$'}GRADLEW")"

          if [ "${'$'}PROJECT_DIR" = "." ]; then
            "${'$'}GRADLEW" --no-daemon --stacktrace --warning-mode all assembleDebug
          else
            cd "${'$'}PROJECT_DIR"
            ./gradlew --no-daemon --stacktrace --warning-mode all assembleDebug
          fi

      - name: Upload all APKs
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: android-apks
          path: |
            **/build/outputs/apk/**/*.apk
            **/build/outputs/apk/**/*.aab
          if-no-files-found: warn
""".trimIndent()
    }

    override suspend fun findOrTriggerRun(
        token: String,
        owner: String,
        repo: String,
        workflowFileNameOrId: String,
        headSha: String?,
        branch: String,
        dispatchStartTimeMillis: Long,
        buildType: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        val authHeader = "Bearer $token"
        try {
            // Step 1: Poll for an automatically triggered push workflow run matching headSha or created after dispatchStartTimeMillis
            for (attempt in 1..4) {
                delay(2500)
                val runsResp = apiService.getWorkflowRuns(
                    authHeader = authHeader,
                    owner = owner,
                    repo = repo,
                    branch = branch,
                    event = null,
                    headSha = headSha,
                    perPage = 10
                )

                if (runsResp.isSuccessful && runsResp.body() != null) {
                    val runs = runsResp.body()!!.workflowRuns
                    val matchingRun = runs.firstOrNull { run ->
                        val createdAtMillis = parseIsoToEpochMillis(run.createdAt)
                        val shaMatch = headSha != null && run.headSha == headSha
                        val timeMatch = createdAtMillis >= (dispatchStartTimeMillis - 10000L)
                        val branchMatch = run.headBranch == null || run.headBranch == branch
                        branchMatch && (shaMatch || timeMatch)
                    }

                    if (matchingRun != null) {
                        Log.d("GitHubActionsBuildEngine", "Found push workflow run #${matchingRun.id}")
                        return@withContext Result.success(matchingRun.id)
                    }
                }
            }

            // Step 2: No push run found. Trigger workflow_dispatch manually.
            val dispatchResp = apiService.dispatchWorkflow(
                authHeader, owner, repo, workflowFileNameOrId,
                WorkflowDispatchRequest(
                    ref = branch,
                    inputs = emptyMap()
                )
            )

            if (!dispatchResp.isSuccessful) {
                val err = dispatchResp.errorBody()?.string() ?: "HTTP ${dispatchResp.code()}"
                return@withContext Result.failure(Exception("Failed to dispatch workflow: $err"))
            }

            // Step 3: Poll for the workflow_dispatch run created after dispatchStartTimeMillis
            for (attempt in 1..10) {
                delay(3000)
                val runsResp = apiService.getWorkflowRuns(
                    authHeader = authHeader,
                    owner = owner,
                    repo = repo,
                    branch = branch,
                    event = "workflow_dispatch",
                    perPage = 10
                )

                if (runsResp.isSuccessful && runsResp.body() != null) {
                    val runs = runsResp.body()!!.workflowRuns
                    val newRun = runs.firstOrNull { run ->
                        val createdAtMillis = parseIsoToEpochMillis(run.createdAt)
                        (run.headBranch == null || run.headBranch == branch) && createdAtMillis >= (dispatchStartTimeMillis - 5000L)
                    }

                    if (newRun != null) {
                        return@withContext Result.success(newRun.id)
                    }
                }
            }

            Result.failure(Exception("Workflow dispatched successfully, but timed out waiting for run to register on GitHub Actions."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pollBuildStatus(
        token: String,
        owner: String,
        repo: String,
        runId: Long
    ): Result<WorkflowRun> = withContext(Dispatchers.IO) {
        val authHeader = "Bearer $token"
        try {
            val resp = apiService.getWorkflowRun(authHeader, owner, repo, runId)
            if (resp.isSuccessful && resp.body() != null) {
                Result.success(resp.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch workflow run status: HTTP ${resp.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBuildLogs(
        token: String,
        owner: String,
        repo: String,
        runId: Long
    ): Result<String> = withContext(Dispatchers.IO) {
        val authHeader = "Bearer $token"
        try {
            val runResp = apiService.getWorkflowRun(authHeader, owner, repo, runId)
            if (runResp.isSuccessful && runResp.body() != null) {
                val run = runResp.body()!!
                val logSummary = StringBuilder()
                
                logSummary.append("Build Status: ${if (run.conclusion == "success") "Successful ✓" else "Failed ❌"}\n")
                logSummary.append("Run #${run.id}\n")
                logSummary.append("Status: ${run.status.uppercase()}\n")
                logSummary.append("Conclusion: ${run.conclusion?.uppercase() ?: "IN PROGRESS"}\n")
                logSummary.append("Created At: ${run.createdAt ?: "N/A"}\n")
                logSummary.append("Web URL: ${run.htmlUrl}\n\n")

                // Try fetching detailed jobs and steps
                val jobsResp = apiService.getRunJobs(authHeader, owner, repo, runId)
                if (jobsResp.isSuccessful && jobsResp.body() != null) {
                    val jobs = jobsResp.body()!!.jobs
                    logSummary.append("--- JOB STEPS ---\n")
                    var failedJobId: Long? = null
                    for (job in jobs) {
                        logSummary.append("Job: ${job.name} (${job.conclusion ?: job.status})\n")
                        if (job.conclusion == "failure") {
                            failedJobId = job.id
                        }
                        job.steps?.forEach { step ->
                            val mark = when (step.conclusion) {
                                "success" -> "✓"
                                "failure" -> "❌"
                                "skipped" -> "⊝"
                                else -> "•"
                            }
                            logSummary.append("  $mark ${step.name}: ${step.conclusion ?: step.status}\n")
                        }
                    }

                    val targetJobId = failedJobId ?: jobs.firstOrNull()?.id
                    if (targetJobId != null && (failedJobId != null || run.conclusion == "failure")) {
                        try {
                            val logResp = apiService.downloadJobLogs(authHeader, owner, repo, targetJobId)
                            if (logResp.isSuccessful && logResp.body() != null) {
                                val rawLogText = logResp.body()!!.string()
                                val extractedError = extractGradleErrorDetails(rawLogText)
                                if (extractedError.isNotBlank()) {
                                    logSummary.append("\n--- GRADLE BUILD FAILURE DETAILS ---\n")
                                    logSummary.append(extractedError)
                                    logSummary.append("\n")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("GitHubActionsBuildEngine", "Error fetching job log text", e)
                        }
                    }
                } else if (run.conclusion == "failure") {
                    logSummary.append("--- BUILD FAILURE DETAILS ---\n")
                    logSummary.append("Gradle build failed on GitHub Actions runner.\n")
                    logSummary.append("Check GitHub Actions web logs for full stack trace: ${run.htmlUrl}\n")
                }

                Result.success(logSummary.toString())
            } else {
                Result.failure(Exception("Unable to retrieve workflow logs from GitHub API."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelBuild(
        token: String,
        owner: String,
        repo: String,
        runId: Long
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val authHeader = "Bearer $token"
        try {
            val resp = apiService.cancelWorkflowRun(authHeader, owner, repo, runId)
            if (resp.isSuccessful || resp.code() == 202) {
                Result.success(true)
            } else {
                val err = resp.errorBody()?.string() ?: "HTTP ${resp.code()}"
                Result.failure(Exception("Failed to cancel workflow run: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadApkArtifact(
        token: String,
        owner: String,
        repo: String,
        runId: Long,
        destDir: File
    ): Result<File> = withContext(Dispatchers.IO) {
        val authHeader = "Bearer $token"
        try {
            val artifactsResp = apiService.getRunArtifacts(authHeader, owner, repo, runId)
            if (!artifactsResp.isSuccessful || artifactsResp.body() == null) {
                return@withContext Result.failure(Exception("Failed to list workflow artifacts: HTTP ${artifactsResp.code()}"))
            }

            val artifacts = artifactsResp.body()!!.artifacts
            if (artifacts.isEmpty()) {
                return@withContext Result.failure(Exception("No APK artifacts found in workflow run #${runId}."))
            }

            // Look for debug-apk or release-apk or any apk artifact
            val artifact = artifacts.find { it.name.contains("apk", ignoreCase = true) } ?: artifacts.first()

            val downloadResp = apiService.downloadArtifactZip(authHeader, owner, repo, artifact.id)
            if (!downloadResp.isSuccessful || downloadResp.body() == null) {
                return@withContext Result.failure(Exception("Failed to download artifact zip: HTTP ${downloadResp.code()}"))
            }

            val zipStream = downloadResp.body()!!.byteStream()
            val zipInputStream = ZipInputStream(zipStream)

            var extractedApkFile: File? = null
            destDir.mkdirs()

            var entry = zipInputStream.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".apk", ignoreCase = true)) {
                    val apkFileName = entry.name.substringAfterLast("/")
                    val targetFile = File(destDir, apkFileName)
                    val canonicalDest = destDir.canonicalPath
                    val canonicalFile = targetFile.canonicalPath
                    if (!canonicalFile.startsWith(canonicalDest + File.separator) && canonicalFile != canonicalDest) {
                        throw SecurityException("Zip Slip vulnerability attempt blocked: $apkFileName")
                    }
                    FileOutputStream(targetFile).use { fos ->
                        zipInputStream.copyTo(fos)
                    }
                    extractedApkFile = targetFile
                    break
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
            zipInputStream.close()

            if (extractedApkFile != null && extractedApkFile.exists() && extractedApkFile.length() > 0) {
                Result.success(extractedApkFile)
            } else {
                Result.failure(Exception("Downloaded artifact ZIP did not contain any valid .apk file."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractGradleErrorDetails(rawLogText: String): String {
        val lines = rawLogText.lines()
        val cleanedLines = lines.map { line ->
            var l = line
            if (l.length > 28 && l.substring(0, 4).all { it.isDigit() } && l[10] == 'T' && l[27] == 'Z') {
                l = l.substring(28).trimStart()
            }
            l.replace(Regex("\u001B\\[[;\\d]*m"), "")
        }

        val errorKeywords = listOf(
            "FAILURE: Build failed",
            "* What went wrong:",
            "Execution failed for task",
            "Could not resolve",
            "Could not find",
            "A problem occurred",
            "Caused by:",
            "ERROR:"
        )

        val matchingIndices = mutableListOf<Int>()
        for ((idx, line) in cleanedLines.withIndex()) {
            if (errorKeywords.any { line.contains(it, ignoreCase = true) }) {
                matchingIndices.add(idx)
            }
        }

        if (matchingIndices.isNotEmpty()) {
            val startIdx = matchingIndices.first()
            val endIdx = minOf(startIdx + 40, cleanedLines.size)
            return cleanedLines.subList(startIdx, endIdx)
                .filter { it.isNotBlank() }
                .joinToString("\n")
        }

        return cleanedLines.takeLast(30)
            .filter { it.isNotBlank() }
            .joinToString("\n")
    }

    private fun parseIsoToEpochMillis(isoString: String?): Long {
        if (isoString.isNullOrEmpty()) return 0L
        return try {
            Instant.parse(isoString).toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }
}
