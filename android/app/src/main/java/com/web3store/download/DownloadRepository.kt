package com.web3store.download

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.web3store.cache.ApkCache
import com.web3store.domain.model.AppDetail
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing APK downloads
 */
@Singleton
class DownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    // Track download states by app ID
    private val _downloadStates = MutableStateFlow<Map<Long, DownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<Long, DownloadState>> = _downloadStates.asStateFlow()

    // Track work IDs by app ID
    private val workIds = mutableMapOf<Long, UUID>()

    /**
     * Start downloading an app
     */
    fun startDownload(appDetail: AppDetail): UUID {
        val workId = DownloadWorker.enqueue(
            context = context,
            appId = appDetail.id,
            packageName = appDetail.packageName,
            versionName = appDetail.versionName,
            apkUrl = appDetail.apkUrl,
            apkHash = appDetail.apkHash,
            apkSize = appDetail.apkSize
        )

        workIds[appDetail.id] = workId
        updateState(appDetail.id, DownloadState.Pending)

        return workId
    }

    /**
     * Cancel a download
     */
    fun cancelDownload(appId: Long) {
        DownloadWorker.cancel(context, appId)
        workIds.remove(appId)
        updateState(appId, DownloadState.Idle)
    }

    /**
     * Get download state for an app
     */
    fun getDownloadState(appId: Long): DownloadState {
        return _downloadStates.value[appId] ?: DownloadState.Idle
    }

    /**
     * Observe work state for a download
     */
    fun observeWorkState(workId: UUID): Flow<WorkInfo?> {
        return workManager.getWorkInfoByIdFlow(workId)
    }

    /**
     * Observe work state and convert to DownloadState
     */
    fun observeDownloadState(appId: Long, workId: UUID): Flow<DownloadState> {
        return workManager.getWorkInfoByIdFlow(workId).map { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.ENQUEUED -> DownloadState.Pending
                WorkInfo.State.RUNNING -> {
                    val progress = workInfo.progress.getInt(DownloadWorker.KEY_PROGRESS, 0)
                    DownloadState.Downloading(
                        bytesDownloaded = 0,
                        totalBytes = null,
                        progress = progress / 100f
                    )
                }
                WorkInfo.State.SUCCEEDED -> {
                    val filePath = workInfo.outputData.getString(DownloadWorker.KEY_FILE_PATH)
                    if (filePath != null) {
                        DownloadState.Completed(File(filePath))
                    } else {
                        DownloadState.Failed(DownloadError.Unknown("File path not found"))
                    }
                }
                WorkInfo.State.FAILED -> {
                    val error = workInfo.outputData.getString(DownloadWorker.KEY_ERROR)
                        ?: "Download failed"
                    DownloadState.Failed(DownloadError.Unknown(error))
                }
                WorkInfo.State.CANCELLED -> {
                    DownloadState.Failed(DownloadError.Cancelled())
                }
                WorkInfo.State.BLOCKED, null -> DownloadState.Pending
            }.also { state ->
                updateState(appId, state)
            }
        }
    }

    /**
     * Check if an APK is already downloaded
     */
    fun hasDownloadedApk(packageName: String, versionName: String): Boolean {
        return ApkCache.hasValidApk(context, packageName, versionName)
    }

    /**
     * Get downloaded APK file
     */
    fun getDownloadedApk(packageName: String, versionName: String): File? {
        return ApkCache.getExistingApk(context, packageName, versionName)
    }

    /**
     * Clear download cache
     */
    fun clearCache() {
        ApkCache.clearAll(context)
    }

    /**
     * Cleanup old downloads
     */
    fun cleanup(maxAgeHours: Int = 24) {
        ApkCache.cleanup(context, maxAgeHours)
    }

    private fun updateState(appId: Long, state: DownloadState) {
        _downloadStates.value = _downloadStates.value.toMutableMap().apply {
            put(appId, state)
        }
    }
}
