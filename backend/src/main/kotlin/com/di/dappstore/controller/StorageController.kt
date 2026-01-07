package com.di.dappstore.controller

import com.di.dappstore.model.StorageBucket
import com.di.dappstore.model.dto.ApkUploadResponse
import com.di.dappstore.model.dto.UploadResponse
import com.di.dappstore.model.vo.ApiResponse
import com.di.dappstore.service.StorageService
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Storage API for file uploads
 */
@RestController
@RequestMapping("/api/v1/storage")
class StorageController(private val storageService: StorageService) {

    /**
     * Upload APK file
     * POST /api/v1/storage/apk
     */
    @PostMapping("/apk", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadApk(
        @RequestPart("file") filePart: FilePart,
        @RequestPart("packageName") packageName: String,
        @RequestPart("versionName") versionName: String
    ): Mono<ApiResponse<ApkUploadResponse>> {
        val originalFilename = filePart.filename()
        if (!originalFilename.endsWith(".apk")) {
            return Mono.just(ApiResponse.error("Invalid file type. Only APK files are allowed."))
        }

        val objectName = "apps/$packageName/$versionName/${UUID.randomUUID()}.apk"

        return filePart.content()
            .reduce(ByteArrayOutputStream()) { baos, dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                baos.write(bytes)
                baos
            }
            .flatMap { baos ->
                val bytes = baos.toByteArray()
                val size = bytes.size.toLong()
                val hash = calculateSha256(bytes)

                storageService.uploadFile(
                    bucket = StorageBucket.APK,
                    objectName = objectName,
                    inputStream = ByteArrayInputStream(bytes),
                    size = size,
                    contentType = "application/vnd.android.package-archive"
                ).map { uploadedName ->
                    val url = storageService.getPresignedUrl(StorageBucket.APK, uploadedName)
                    ApiResponse.success(
                        ApkUploadResponse(
                            url = url,
                            objectName = uploadedName,
                            size = size,
                            hash = "sha256:$hash"
                        )
                    )
                }
            }
            .doOnSuccess { logger.info { "APK uploaded: $objectName" } }
            .onErrorResume { e ->
                logger.error(e) { "Failed to upload APK: ${e.message}" }
                Mono.just(ApiResponse.error("Failed to upload APK: ${e.message}"))
            }
    }

    /**
     * Upload app icon
     * POST /api/v1/storage/icon
     */
    @PostMapping("/icon", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadIcon(
        @RequestPart("file") filePart: FilePart,
        @RequestPart("packageName") packageName: String
    ): Mono<ApiResponse<UploadResponse>> {
        val contentType = getImageContentType(filePart.filename())
            ?: return Mono.just(ApiResponse.error("Invalid file type. Only PNG, JPG, WEBP are allowed."))

        val extension = getExtension(filePart.filename())
        val objectName = "icons/$packageName/${UUID.randomUUID()}.$extension"

        return uploadImage(filePart, StorageBucket.ICON, objectName, contentType)
    }

    /**
     * Upload screenshot
     * POST /api/v1/storage/screenshot
     */
    @PostMapping("/screenshot", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadScreenshot(
        @RequestPart("file") filePart: FilePart,
        @RequestPart("packageName") packageName: String,
        @RequestPart("sortOrder", required = false) sortOrder: String?
    ): Mono<ApiResponse<UploadResponse>> {
        val contentType = getImageContentType(filePart.filename())
            ?: return Mono.just(ApiResponse.error("Invalid file type. Only PNG, JPG, WEBP are allowed."))

        val extension = getExtension(filePart.filename())
        val order = sortOrder?.toIntOrNull() ?: 0
        val objectName = "screenshots/$packageName/${order}_${UUID.randomUUID()}.$extension"

        return uploadImage(filePart, StorageBucket.SCREENSHOT, objectName, contentType)
    }

    /**
     * Get presigned URL for a file
     * GET /api/v1/storage/url
     */
    @GetMapping("/url")
    fun getPresignedUrl(
        @RequestParam bucket: String,
        @RequestParam objectName: String
    ): Mono<ApiResponse<String>> {
        val storageBucket = StorageBucket.fromBucketName(bucket)
            ?: return Mono.just(ApiResponse.error("Invalid bucket name"))

        return storageService.getPresignedUrlMono(storageBucket, objectName)
            .map { url -> ApiResponse.success(url) }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error("Failed to generate URL: ${e.message}"))
            }
    }

    private fun uploadImage(
        filePart: FilePart,
        bucket: StorageBucket,
        objectName: String,
        contentType: String
    ): Mono<ApiResponse<UploadResponse>> {
        return filePart.content()
            .reduce(ByteArrayOutputStream()) { baos, dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                baos.write(bytes)
                baos
            }
            .flatMap { baos ->
                val bytes = baos.toByteArray()
                val size = bytes.size.toLong()

                storageService.uploadFile(
                    bucket = bucket,
                    objectName = objectName,
                    inputStream = ByteArrayInputStream(bytes),
                    size = size,
                    contentType = contentType
                ).map { uploadedName ->
                    val url = storageService.getPresignedUrl(bucket, uploadedName)
                    ApiResponse.success(
                        UploadResponse(
                            url = url,
                            objectName = uploadedName,
                            size = size,
                            contentType = contentType
                        )
                    )
                }
            }
            .doOnSuccess { logger.info { "Image uploaded: $objectName" } }
            .onErrorResume { e ->
                logger.error(e) { "Failed to upload image: ${e.message}" }
                Mono.just(ApiResponse.error("Failed to upload image: ${e.message}"))
            }
    }

    private fun getImageContentType(filename: String): String? {
        val lower = filename.lowercase()
        return when {
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".webp") -> "image/webp"
            else -> null
        }
    }

    private fun getExtension(filename: String): String {
        return filename.substringAfterLast('.', "png")
    }

    private fun calculateSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
