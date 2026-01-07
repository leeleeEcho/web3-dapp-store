package com.web3store.download

import java.io.File

/**
 * Download state sealed class
 */
sealed class DownloadState {
    object Idle : DownloadState()
    object Pending : DownloadState()

    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long?,
        val progress: Float
    ) : DownloadState()

    data class Completed(val file: File) : DownloadState()
    data class Failed(val error: DownloadError) : DownloadState()
}

/**
 * Download error types
 */
sealed class DownloadError {
    abstract val errorMessage: String

    data class Network(override val errorMessage: String) : DownloadError()
    data class Storage(override val errorMessage: String) : DownloadError()
    data class Integrity(override val errorMessage: String) : DownloadError()
    data class Cancelled(override val errorMessage: String = "Download cancelled") : DownloadError()
    data class Unknown(override val errorMessage: String) : DownloadError()

    fun getMessage(): String = errorMessage
}
