package com.di.dappstore.service

import com.di.dappstore.model.StorageBucket
import io.minio.*
import io.minio.http.Method
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * Storage service for MinIO operations
 */
@Service
class StorageService(private val minioClient: MinioClient) {

    companion object {
        private const val PRESIGNED_URL_EXPIRY_HOURS = 24L
    }

    /**
     * Upload a file to MinIO
     *
     * @param bucket Target bucket
     * @param objectName Object name (file path in bucket)
     * @param inputStream File input stream
     * @param size File size
     * @param contentType MIME type
     * @return Object name on success
     */
    fun uploadFile(
        bucket: StorageBucket,
        objectName: String,
        inputStream: InputStream,
        size: Long,
        contentType: String
    ): Mono<String> = Mono.fromCallable {
        ensureBucketExists(bucket.bucketName)

        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket.bucketName)
                .`object`(objectName)
                .stream(inputStream, size, -1)
                .contentType(contentType)
                .build()
        )

        logger.info { "Uploaded file: ${bucket.bucketName}/$objectName ($size bytes)" }
        objectName
    }.subscribeOn(Schedulers.boundedElastic())

    /**
     * Get a presigned URL for downloading a file
     *
     * @param bucket Target bucket
     * @param objectName Object name
     * @param expiryHours URL expiry time in hours
     * @return Presigned URL
     */
    fun getPresignedUrl(
        bucket: StorageBucket,
        objectName: String,
        expiryHours: Long = PRESIGNED_URL_EXPIRY_HOURS
    ): String {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket.bucketName)
                .`object`(objectName)
                .expiry(expiryHours.toInt(), TimeUnit.HOURS)
                .build()
        )
    }

    /**
     * Get a presigned URL (reactive version)
     */
    fun getPresignedUrlMono(
        bucket: StorageBucket,
        objectName: String,
        expiryHours: Long = PRESIGNED_URL_EXPIRY_HOURS
    ): Mono<String> = Mono.fromCallable {
        getPresignedUrl(bucket, objectName, expiryHours)
    }.subscribeOn(Schedulers.boundedElastic())

    /**
     * Delete a file from MinIO
     *
     * @param bucket Target bucket
     * @param objectName Object name
     */
    fun deleteFile(bucket: StorageBucket, objectName: String): Mono<Void> = Mono.fromRunnable<Void> {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucket.bucketName)
                .`object`(objectName)
                .build()
        )
        logger.info { "Deleted file: ${bucket.bucketName}/$objectName" }
    }.subscribeOn(Schedulers.boundedElastic())

    /**
     * Check if a file exists
     */
    fun fileExists(bucket: StorageBucket, objectName: String): Mono<Boolean> = Mono.fromCallable {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucket.bucketName)
                    .`object`(objectName)
                    .build()
            )
            true
        } catch (e: Exception) {
            false
        }
    }.subscribeOn(Schedulers.boundedElastic())

    /**
     * Calculate SHA-256 hash of input stream
     */
    fun calculateHash(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        var bytesRead: Int

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Ensure bucket exists, create if not
     */
    private fun ensureBucketExists(bucketName: String) {
        val exists = minioClient.bucketExists(
            BucketExistsArgs.builder()
                .bucket(bucketName)
                .build()
        )

        if (!exists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build()
            )
            logger.info { "Created bucket: $bucketName" }
        }
    }

    /**
     * Initialize all buckets on startup
     */
    fun initializeBuckets(): Mono<Void> = Mono.fromRunnable<Void> {
        StorageBucket.entries.forEach { bucket ->
            ensureBucketExists(bucket.bucketName)
        }
        logger.info { "All storage buckets initialized" }
    }.subscribeOn(Schedulers.boundedElastic())

    /**
     * Get file info
     */
    fun getFileInfo(bucket: StorageBucket, objectName: String): Mono<FileInfo> = Mono.fromCallable {
        val stat = minioClient.statObject(
            StatObjectArgs.builder()
                .bucket(bucket.bucketName)
                .`object`(objectName)
                .build()
        )
        FileInfo(
            objectName = objectName,
            size = stat.size(),
            contentType = stat.contentType(),
            etag = stat.etag()
        )
    }.subscribeOn(Schedulers.boundedElastic())

    data class FileInfo(
        val objectName: String,
        val size: Long,
        val contentType: String,
        val etag: String
    )
}
