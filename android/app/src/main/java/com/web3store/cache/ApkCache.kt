package com.web3store.cache

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * APK file cache management
 */
object ApkCache {
    private const val APK_DIR = "apk"
    private const val PARTIAL_SUFFIX = ".partial"

    /**
     * Get the APK cache directory
     * Prefers external cache, falls back to internal cache
     */
    fun getApkDirectory(context: Context): File {
        val externalDir = context.externalCacheDir?.let { File(it, APK_DIR) }
        val internalDir = File(context.cacheDir, APK_DIR)

        val targetDir = if (externalDir != null && (externalDir.exists() || externalDir.mkdirs())) {
            externalDir
        } else {
            internalDir.also { it.mkdirs() }
        }

        return targetDir
    }

    /**
     * Generate a unique filename for an APK
     */
    fun generateFileName(packageName: String, versionName: String): String {
        return "${packageName}_${versionName}.apk"
    }

    /**
     * Get the partial (downloading) APK file
     */
    fun getPartialApkFile(context: Context, fileName: String): File {
        return File(getApkDirectory(context), fileName + PARTIAL_SUFFIX)
    }

    /**
     * Get the final APK file
     */
    fun getApkFile(context: Context, fileName: String): File {
        return File(getApkDirectory(context), fileName)
    }

    /**
     * Get a content URI for the APK file via FileProvider
     */
    fun getApkUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Check if a valid APK exists for the given package
     */
    fun hasValidApk(context: Context, packageName: String, versionName: String): Boolean {
        val fileName = generateFileName(packageName, versionName)
        val file = getApkFile(context, fileName)
        return file.exists() && file.length() > 0
    }

    /**
     * Get existing APK file if valid
     */
    fun getExistingApk(context: Context, packageName: String, versionName: String): File? {
        val fileName = generateFileName(packageName, versionName)
        val file = getApkFile(context, fileName)
        return if (file.exists() && file.length() > 0) file else null
    }

    /**
     * Delete all cached APKs
     */
    fun clearAll(context: Context) {
        getApkDirectory(context).listFiles()?.forEach { it.delete() }
    }

    /**
     * Clean up old/partial downloads
     * @param maxAgeHours Maximum age in hours for APK files
     */
    fun cleanup(context: Context, maxAgeHours: Int = 24) {
        val now = System.currentTimeMillis()
        val maxAge = maxAgeHours * 60 * 60 * 1000L

        getApkDirectory(context).listFiles()?.forEach { file ->
            // Delete partial files
            if (file.name.endsWith(PARTIAL_SUFFIX)) {
                file.delete()
                return@forEach
            }

            // Delete old files
            if (now - file.lastModified() > maxAge) {
                file.delete()
            }
        }
    }

    /**
     * Get total cache size in bytes
     */
    fun getCacheSize(context: Context): Long {
        return getApkDirectory(context).listFiles()?.sumOf { it.length() } ?: 0L
    }
}
