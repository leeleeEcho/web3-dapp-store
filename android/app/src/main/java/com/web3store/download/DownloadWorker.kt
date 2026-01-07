package com.web3store.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.web3store.R
import com.web3store.cache.ApkCache
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * WorkManager worker for background APK downloads
 */
@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val apkDownloader: ApkDownloader
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_APP_ID = "app_id"
        const val KEY_PACKAGE_NAME = "package_name"
        const val KEY_VERSION_NAME = "version_name"
        const val KEY_APK_URL = "apk_url"
        const val KEY_APK_HASH = "apk_hash"
        const val KEY_APK_SIZE = "apk_size"

        const val KEY_PROGRESS = "progress"
        const val KEY_FILE_PATH = "file_path"
        const val KEY_ERROR = "error"

        private const val NOTIFICATION_CHANNEL_ID = "download_channel"
        private const val NOTIFICATION_ID = 1001

        /**
         * Enqueue a download work request
         */
        fun enqueue(
            context: Context,
            appId: Long,
            packageName: String,
            versionName: String,
            apkUrl: String,
            apkHash: String,
            apkSize: Long
        ): UUID {
            val inputData = workDataOf(
                KEY_APP_ID to appId,
                KEY_PACKAGE_NAME to packageName,
                KEY_VERSION_NAME to versionName,
                KEY_APK_URL to apkUrl,
                KEY_APK_HASH to apkHash,
                KEY_APK_SIZE to apkSize
            )

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "download_$appId",
                    ExistingWorkPolicy.KEEP,
                    downloadRequest
                )

            return downloadRequest.id
        }

        /**
         * Cancel a download
         */
        fun cancel(context: Context, appId: Long) {
            WorkManager.getInstance(context).cancelUniqueWork("download_$appId")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val packageName = inputData.getString(KEY_PACKAGE_NAME)
            ?: return@withContext Result.failure(workDataOf(KEY_ERROR to "Missing package name"))

        val versionName = inputData.getString(KEY_VERSION_NAME)
            ?: return@withContext Result.failure(workDataOf(KEY_ERROR to "Missing version name"))

        val apkUrl = inputData.getString(KEY_APK_URL)
            ?: return@withContext Result.failure(workDataOf(KEY_ERROR to "Missing APK URL"))

        val apkHash = inputData.getString(KEY_APK_HASH) ?: ""

        // Check if APK already exists
        val existingApk = ApkCache.getExistingApk(context, packageName, versionName)
        if (existingApk != null) {
            return@withContext Result.success(workDataOf(KEY_FILE_PATH to existingApk.absolutePath))
        }

        // Prepare files
        val fileName = ApkCache.generateFileName(packageName, versionName)
        val partialFile = ApkCache.getPartialApkFile(context, fileName)
        val finalFile = ApkCache.getApkFile(context, fileName)

        try {
            // Set foreground for long-running work
            setForeground(createForegroundInfo(packageName, 0))

            // Download APK
            val result = apkDownloader.download(
                url = apkUrl,
                targetFile = partialFile
            ) { bytesRead, totalBytes ->
                val progress = if (totalBytes != null && totalBytes > 0) {
                    (bytesRead * 100 / totalBytes).toInt()
                } else {
                    -1
                }

                // Update progress
                setProgressAsync(workDataOf(KEY_PROGRESS to progress))

                // Update notification
                updateNotification(packageName, progress)
            }

            result.fold(
                onSuccess = { file ->
                    // Verify hash if provided
                    if (apkHash.isNotBlank() && !apkDownloader.verifyHash(file, apkHash)) {
                        file.delete()
                        return@withContext Result.failure(
                            workDataOf(KEY_ERROR to "APK hash verification failed")
                        )
                    }

                    // Rename partial to final
                    if (!file.renameTo(finalFile)) {
                        file.copyTo(finalFile, overwrite = true)
                        file.delete()
                    }

                    Result.success(workDataOf(KEY_FILE_PATH to finalFile.absolutePath))
                },
                onFailure = { error ->
                    partialFile.delete()
                    Result.failure(workDataOf(KEY_ERROR to (error.message ?: "Download failed")))
                }
            )
        } catch (e: Exception) {
            partialFile.delete()
            Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Unknown error")))
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val packageName = inputData.getString(KEY_PACKAGE_NAME) ?: "App"
        return createForegroundInfo(packageName, 0)
    }

    private fun createForegroundInfo(appName: String, progress: Int): ForegroundInfo {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading $appName")
            .setContentText(if (progress >= 0) "$progress%" else "Downloading...")
            .setProgress(100, progress.coerceAtLeast(0), progress < 0)
            .setOngoing(true)
            .setSilent(true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun updateNotification(appName: String, progress: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading $appName")
            .setContentText(if (progress >= 0) "$progress%" else "Downloading...")
            .setProgress(100, progress.coerceAtLeast(0), progress < 0)
            .setOngoing(true)
            .setSilent(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "APK download notifications"
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
