package com.di.dappstore.model.dto

/**
 * Response for file upload operations
 */
data class UploadResponse(
    val url: String,
    val objectName: String,
    val size: Long,
    val contentType: String,
    val hash: String? = null
)

/**
 * Response for APK upload with additional metadata
 */
data class ApkUploadResponse(
    val url: String,
    val objectName: String,
    val size: Long,
    val hash: String,
    val contentType: String = "application/vnd.android.package-archive"
)
