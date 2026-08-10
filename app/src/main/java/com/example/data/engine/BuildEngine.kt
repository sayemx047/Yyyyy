package com.example.data.engine

import com.example.data.api.models.WorkflowRun
import java.io.File

interface BuildEngine {
    val engineName: String

    suspend fun ensureWorkflowExists(token: String, owner: String, repo: String, branch: String = "main"): Result<Boolean>

    suspend fun triggerBuild(token: String, owner: String, repo: String, buildType: String, branch: String = "main"): Result<Long>

    suspend fun pollBuildStatus(token: String, owner: String, repo: String, runId: Long): Result<WorkflowRun>

    suspend fun getBuildLogs(token: String, owner: String, repo: String, runId: Long): Result<String>

    suspend fun downloadApkArtifact(token: String, owner: String, repo: String, runId: Long, destDir: File): Result<File>
}
