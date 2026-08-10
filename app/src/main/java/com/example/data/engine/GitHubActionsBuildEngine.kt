package com.example.data.engine

import android.util.Base64
import android.util.Log
import com.example.data.api.GitHubApiService
import com.example.data.api.models.CreateOrUpdateFileRequest
import com.example.data.api.models.WorkflowDispatchRequest
import com.example.data.api.models.WorkflowRun
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class GitHubActionsBuildEngine(private val apiService: GitHubApiService) : BuildEngine {

    override val engineName: String = "GitHub Actions Remote Runner"

    companion object {
        const val WORKFLOW_FILE_PATH = ".github/workflows/android-build.yml"
        const val WORKFLOW_NAME = "android-build.yml"

        val WORKFLOW_YAML_CONTENT = """
name: Android APK Build

on:
  workflow_dispatch:
    inputs:
      build_type:
        description: 'Build type'
        required: true
        default: 'debug'
        type: choice
        options:
          - debug
          - release

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Make Gradle Wrapper Executable
        run: chmod +x gradlew || true

      - name: Build Debug APK
        if: ${'$'}{{ inputs.build_type == 'debug' }}
        run: ./gradlew assembleDebug --no-daemon

      - name: Build Release APK
        if: ${'$'}{{ inputs.build_type == 'release' }}
        run: ./gradlew assembleRelease --no-daemon || ./gradlew assembleUnsignedRelease --no-daemon

      - name: Upload Debug APK
        if: ${'$'}{{ inputs.build_type == 'debug' }}
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: '**/build/outputs/apk/debug/*.apk'
          retention-days: 7

      - name: Upload Release APK
        if: ${'$'}{{ inputs.build_type == 'release' }}
        uses: actions/upload-artifact@v4
        with:
          name: release-apk
          path: '**/build/outputs/apk/**/*.apk'
          retention-days: 7
""".trimIndent()
    }

    override suspend fun ensureWorkflowExists(
        token: String,
        owner: String,
        repo: String,
        branch: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val authHeader = "Bearer $token"
        try {
            val base64Content = Base64.encodeToString(WORKFLOW_YAML_CONTENT.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

            val resp = apiService.createOrUpdateFile(
                authHeader, owner, repo, WORKFLOW_FILE_PATH,
                CreateOrUpdateFileRequest(
                    message = "Add/Update Native APK Builder GitHub Actions workflow",
                    contentBase64 = base64Content,
                    branch = branch
                )
            )

            if (resp.isSuccessful || resp.code() == 422) { // 422 means already up to date or identical sha
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update GitHub Actions workflow: HTTP ${resp.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun triggerBuild(
        token: String,
        owner: String,
        repo: String,
        buildType: String,
        branch: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        val authHeader = "Bearer $token"
        try {
            ensureWorkflowExists(token, owner, repo, branch)

            val dispatchResp = apiService.dispatchWorkflow(
                authHeader, owner, repo, WORKFLOW_NAME,
                WorkflowDispatchRequest(
                    ref = branch,
                    inputs = mapOf("build_type" to buildType.lowercase())
                )
            )

            if (!dispatchResp.isSuccessful) {
                val err = dispatchResp.errorBody()?.string() ?: "Workflow dispatch failed HTTP ${dispatchResp.code()}"
                return@withContext Result.failure(Exception("Workflow trigger failed: $err"))
            }

            // Poll for the triggered run ID (wait a few seconds for GitHub to register run)
            delay(3000)

            val runsResp = apiService.getWorkflowRuns(authHeader, owner, repo, event = "workflow_dispatch", perPage = 5)
            if (runsResp.isSuccessful && runsResp.body() != null) {
                val runs = runsResp.body()!!.workflowRuns
                if (runs.isNotEmpty()) {
                    val latestRun = runs.first()
                    return@withContext Result.success(latestRun.id)
                }
            }

            Result.failure(Exception("Workflow dispatched successfully, but could not retrieve run ID. Please check GitHub Actions."))
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
                logSummary.append("Workflow Run #${run.id}\n")
                logSummary.append("Status: ${run.status.uppercase()}\n")
                logSummary.append("Conclusion: ${run.conclusion?.uppercase() ?: "PENDING"}\n")
                logSummary.append("Created At: ${run.createdAt ?: "N/A"}\n")
                logSummary.append("Updated At: ${run.updatedAt ?: "N/A"}\n")
                logSummary.append("GitHub Web URL: ${run.htmlUrl}\n\n")

                if (run.conclusion == "failure") {
                    logSummary.append("--- BUILD FAILURE SUMMARY ---\n")
                    logSummary.append("Gradle build encountered errors during execution.\n")
                    logSummary.append("Please verify:\n")
                    logSummary.append("1. Gradle wrapper permissions ('chmod +x gradlew')\n")
                    logSummary.append("2. Kotlin / Java compiler syntax compatibility\n")
                    logSummary.append("3. Missing repository dependencies or secret variables\n\n")
                } else if (run.conclusion == "success") {
                    logSummary.append("--- BUILD SUCCESS ---\n")
                    logSummary.append("Gradle compiled APK successfully and uploaded build artifact.\n")
                }

                Result.success(logSummary.toString())
            } else {
                Result.failure(Exception("Unable to retrieve logs from GitHub API."))
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
                return@withContext Result.failure(Exception("No APK artifacts found in workflow run."))
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
}
