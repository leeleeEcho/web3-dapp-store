package com.di.dappstore.model.dto

import com.di.dappstore.model.entity.AppStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

// ============================================
// 应用提交请求
// ============================================

/**
 * 提交新应用请求
 */
data class SubmitAppRequest(
    @field:NotBlank(message = "包名不能为空")
    val packageName: String,

    @field:NotBlank(message = "应用名称不能为空")
    @field:Size(max = 100, message = "应用名称不能超过100个字符")
    val name: String,

    @field:Size(max = 5000, message = "描述不能超过5000个字符")
    val description: String? = null,

    @field:Size(max = 200, message = "简短描述不能超过200个字符")
    val shortDescription: String? = null,

    @field:NotBlank(message = "版本号不能为空")
    val versionName: String,

    val versionCode: Long,

    val minSdkVersion: Int = 21,
    val targetSdkVersion: Int = 34,

    val categoryId: Long? = null,

    val isWeb3: Boolean = true,
    val blockchain: String? = null,
    val contractAddress: String? = null,

    val websiteUrl: String? = null,
    val sourceCodeUrl: String? = null,

    // 文件信息 (上传后获得)
    @field:NotBlank(message = "APK URL 不能为空")
    val apkUrl: String,

    @field:NotBlank(message = "APK Hash 不能为空")
    val apkHash: String,

    val apkSize: Long = 0,

    val iconUrl: String? = null,

    val screenshotUrls: List<String>? = null,

    // 是否保存为草稿
    val saveAsDraft: Boolean = false
)

/**
 * 更新应用请求
 */
data class UpdateAppSubmissionRequest(
    val name: String? = null,

    @field:Size(max = 5000, message = "描述不能超过5000个字符")
    val description: String? = null,

    @field:Size(max = 200, message = "简短描述不能超过200个字符")
    val shortDescription: String? = null,

    val categoryId: Long? = null,

    val blockchain: String? = null,
    val contractAddress: String? = null,

    val websiteUrl: String? = null,
    val sourceCodeUrl: String? = null,

    val iconUrl: String? = null,

    val screenshotUrls: List<String>? = null
)

/**
 * 发布新版本请求
 */
data class PublishVersionRequest(
    @field:NotBlank(message = "版本号不能为空")
    val versionName: String,

    val versionCode: Long,

    val minSdkVersion: Int? = null,
    val targetSdkVersion: Int? = null,

    @field:NotBlank(message = "APK URL 不能为空")
    val apkUrl: String,

    @field:NotBlank(message = "APK Hash 不能为空")
    val apkHash: String,

    val apkSize: Long = 0,

    @field:Size(max = 2000, message = "更新日志不能超过2000个字符")
    val changelog: String? = null,

    // 是否保存为草稿
    val saveAsDraft: Boolean = false
)

// ============================================
// 应用提交响应
// ============================================

/**
 * 开发者应用列表项
 */
data class DeveloperAppItem(
    val id: Long,
    val packageName: String,
    val name: String,
    val iconUrl: String?,
    val versionName: String,
    val versionCode: Long,
    val status: AppStatus,
    val rejectionReason: String?,
    val downloadCount: Long,
    val ratingAverage: Double,
    val categoryName: String?,
    val submittedAt: LocalDateTime?,
    val reviewedAt: LocalDateTime?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

/**
 * 开发者应用详情
 */
data class DeveloperAppDetail(
    val id: Long,
    val packageName: String,
    val name: String,
    val description: String?,
    val shortDescription: String?,
    val versionName: String,
    val versionCode: Long,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val iconUrl: String?,
    val apkUrl: String,
    val apkSize: Long,
    val apkHash: String,
    val categoryId: Long?,
    val categoryName: String?,
    val isWeb3: Boolean,
    val blockchain: String?,
    val contractAddress: String?,
    val websiteUrl: String?,
    val sourceCodeUrl: String?,
    val downloadCount: Long,
    val ratingAverage: Double,
    val ratingCount: Long,
    val status: AppStatus,
    val rejectionReason: String?,
    val isFeatured: Boolean,
    val screenshots: List<ScreenshotInfo>,
    val submittedAt: LocalDateTime?,
    val reviewedAt: LocalDateTime?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

/**
 * 截图信息
 */
data class ScreenshotInfo(
    val id: Long,
    val imageUrl: String,
    val sortOrder: Int
)

/**
 * 应用统计信息
 */
data class AppStatistics(
    val totalApps: Int,
    val draftCount: Int,
    val pendingCount: Int,
    val approvedCount: Int,
    val rejectedCount: Int,
    val totalDownloads: Long,
    val averageRating: Double
)
