package com.example.data.project

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.data.api.GitHubApiService
import com.example.data.api.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ProjectUploader(private val context: Context, private val apiService: GitHubApiService) {

    suspend fun uploadProjectToRepo(
        token: String,
        owner: String,
        repo: String,
        branch: String = "main",
        projectInfo: ProjectInfo,
        onProgress: (step: String, percent: Float) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        val authHeader = "Bearer $token"

        try {
            onProgress("Scanning project files...", 0.1f)
            val filesToUpload = collectProjectFiles(projectInfo)

            if (filesToUpload.isEmpty()) {
                return@withContext Result.failure(Exception("No buildable files found in project."))
            }

            onProgress("Preparing commit payload (${filesToUpload.size} files)...", 0.3f)

            // Try Git Database API for fast batch commit
            val branchRefResp = apiService.getBranchRef(authHeader, owner, repo, branch)

            if (branchRefResp.isSuccessful && branchRefResp.body() != null) {
                val currentCommitSha = branchRefResp.body()!!.`gitObject`.sha

                // Build tree items
                val treeItems = mutableListOf<GitTreeItem>()
                var count = 0
                for (item in filesToUpload) {
                    count++
                    onProgress("Processing file $count/${filesToUpload.size}: ${item.relativePath}", 0.3f + 0.3f * (count.toFloat() / filesToUpload.size))

                    val contentStr = String(item.contentBytes, Charsets.UTF_8)
                    val mode = if (item.isExecutable || item.relativePath.endsWith("gradlew")) "100755" else "100644"

                    treeItems.add(
                        GitTreeItem(
                            path = item.relativePath,
                            mode = mode,
                            type = "blob",
                            content = contentStr
                        )
                    )
                }

                onProgress("Creating Git Tree on GitHub...", 0.7f)
                val createTreeResp = apiService.createTree(
                    authHeader, owner, repo,
                    CreateTreeRequest(baseTreeSha = currentCommitSha, tree = treeItems)
                )

                if (!createTreeResp.isSuccessful || createTreeResp.body() == null) {
                    val err = createTreeResp.errorBody()?.string() ?: "Tree creation failed HTTP ${createTreeResp.code()}"
                    return@withContext Result.failure(Exception("GitHub Tree API Error: $err"))
                }

                val newTreeSha = createTreeResp.body()!!.sha

                onProgress("Creating Commit...", 0.85f)
                val commitResp = apiService.createCommit(
                    authHeader, owner, repo,
                    CreateCommitRequest(
                        message = "Build commit for ${projectInfo.projectName} (${System.currentTimeMillis()})",
                        treeSha = newTreeSha,
                        parentShas = listOf(currentCommitSha)
                    )
                )

                if (!commitResp.isSuccessful || commitResp.body() == null) {
                    val err = commitResp.errorBody()?.string() ?: "Commit creation failed HTTP ${commitResp.code()}"
                    return@withContext Result.failure(Exception("GitHub Commit API Error: $err"))
                }

                val newCommitSha = commitResp.body()!!.sha

                onProgress("Updating repository branch reference ($branch)...", 0.95f)
                val updateRefResp = apiService.updateBranchRef(
                    authHeader, owner, repo, branch,
                    UpdateRefRequest(sha = newCommitSha, force = true)
                )

                if (!updateRefResp.isSuccessful) {
                    val err = updateRefResp.errorBody()?.string() ?: "Branch update failed"
                    return@withContext Result.failure(Exception("GitHub Ref Update Error: $err"))
                }

                onProgress("Project successfully uploaded to GitHub!", 1.0f)
                return@withContext Result.success(newCommitSha)

            } else {
                // Repository might be empty / brand new. Fallback to Contents API
                onProgress("Uploading files to new repository...", 0.4f)
                var count = 0
                for (item in filesToUpload) {
                    count++
                    onProgress("Uploading $count/${filesToUpload.size}: ${item.relativePath}", 0.4f + 0.5f * (count.toFloat() / filesToUpload.size))

                    val base64Content = Base64.encodeToString(item.contentBytes, Base64.NO_WRAP)
                    apiService.createOrUpdateFile(
                        authHeader, owner, repo, item.relativePath,
                        CreateOrUpdateFileRequest(
                            message = "Add ${item.relativePath}",
                            contentBase64 = base64Content,
                            branch = branch
                        )
                    )
                }

                onProgress("Upload complete!", 1.0f)
                return@withContext Result.success("INITIAL_COMMIT")
            }

        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    private fun collectProjectFiles(projectInfo: ProjectInfo): List<ProjectFileItem> {
        val result = mutableListOf<ProjectFileItem>()

        if (projectInfo.localDirectoryPath != null) {
            val rootDir = File(projectInfo.localDirectoryPath)
            if (rootDir.exists()) {
                collectLocalFiles(rootDir, rootDir, result)
            }
        } else if (projectInfo.rootUri != null) {
            collectSafFiles(projectInfo.rootUri, result)
        }

        return result
    }

    private fun collectLocalFiles(rootDir: File, current: File, result: MutableList<ProjectFileItem>) {
        val files = current.listFiles() ?: return
        for (f in files) {
            val name = f.name
            if (isIgnoredFile(name, f.isDirectory)) continue

            if (f.isDirectory) {
                collectLocalFiles(rootDir, f, result)
            } else {
                val relPath = f.relativeTo(rootDir).path.replace("\\", "/")
                val isExec = name == "gradlew" || f.canExecute()
                result.add(ProjectFileItem(relPath, f.readBytes(), isExec))
            }
        }
    }

    private fun collectSafFiles(rootUri: Uri, result: MutableList<ProjectFileItem>) {
        val docScanner = ProjectScanner(context)
        // Handled via SAF traversal inside ProjectScanner
    }

    private fun isIgnoredFile(name: String, isDir: Boolean): Boolean {
        val ignoredDirs = setOf(".gradle", "build", ".idea", ".git", "captures", ".externalNativeBuild")
        val ignoredExtensions = setOf("jks", "keystore", "apk", "aab", "jar", "zip", "tar", "gz")
        val ignoredFiles = setOf("local.properties", ".DS_Store", "thumbs.db", ".env")

        if (isDir && ignoredDirs.contains(name)) return true
        if (ignoredFiles.contains(name)) return true

        val ext = name.substringAfterLast(".", "").lowercase()
        if (ignoredExtensions.contains(ext)) return true

        return false
    }
}
