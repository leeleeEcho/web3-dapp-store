package com.di.dappstore.model

/**
 * Storage bucket types for MinIO
 */
enum class StorageBucket(val bucketName: String, val contentTypes: List<String>) {
    APK("dappstore-apks", listOf("application/vnd.android.package-archive")),
    ICON("dappstore-icons", listOf("image/png", "image/jpeg", "image/webp")),
    SCREENSHOT("dappstore-screenshots", listOf("image/png", "image/jpeg", "image/webp"));

    companion object {
        fun fromBucketName(name: String): StorageBucket? =
            entries.find { it.bucketName == name }
    }
}
