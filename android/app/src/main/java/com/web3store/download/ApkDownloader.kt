package com.web3store.download

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * APK downloader using OkHttp
 */
@Singleton
class ApkDownloader @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val BUFFER_SIZE = 8 * 1024L // 8KB buffer
    }

    /**
     * Download an APK file from the given URL
     *
     * @param url Download URL
     * @param targetFile Target file to save the APK
     * @param onProgress Progress callback (bytesRead, totalBytes)
     * @return Result containing the downloaded file or an error
     */
    suspend fun download(
        url: String,
        targetFile: File,
        onProgress: (bytesRead: Long, totalBytes: Long?) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Ensure parent directory exists
            targetFile.parentFile?.mkdirs()

            val request = Request.Builder()
                .url(url)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("Download failed: HTTP ${response.code}")
                )
            }

            val body = response.body
                ?: return@withContext Result.failure(IOException("Empty response body"))

            val contentLength = body.contentLength().takeIf { it > 0 }

            body.source().use { source ->
                targetFile.sink().buffer().use { sink ->
                    var totalBytesRead = 0L

                    while (isActive) {
                        val bytesRead = source.read(sink.buffer, BUFFER_SIZE)
                        if (bytesRead == -1L) break

                        totalBytesRead += bytesRead
                        sink.emitCompleteSegments()

                        onProgress(totalBytesRead, contentLength)
                    }

                    // Check if download was cancelled
                    if (!isActive) {
                        targetFile.delete()
                        return@withContext Result.failure(
                            IOException("Download cancelled")
                        )
                    }
                }
            }

            Result.success(targetFile)
        } catch (e: Exception) {
            targetFile.delete()
            Result.failure(e)
        }
    }

    /**
     * Verify the APK file hash
     *
     * @param file The APK file to verify
     * @param expectedHash Expected hash (format: "sha256:hash" or just "hash")
     * @return true if hash matches, false otherwise
     */
    fun verifyHash(file: File, expectedHash: String): Boolean {
        if (expectedHash.isBlank()) return true // Skip if no hash provided

        return try {
            val hash = if (expectedHash.contains(":")) {
                expectedHash.substringAfter(":")
            } else {
                expectedHash
            }

            val algorithm = if (expectedHash.startsWith("sha256", ignoreCase = true)) {
                "SHA-256"
            } else {
                "SHA-256" // Default to SHA-256
            }

            val digest = MessageDigest.getInstance(algorithm)
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }

            val computedHash = digest.digest().joinToString("") { "%02x".format(it) }
            computedHash.equals(hash, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
}
