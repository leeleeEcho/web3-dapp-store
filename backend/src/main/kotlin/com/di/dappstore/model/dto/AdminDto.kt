package com.di.dappstore.model.dto

import com.di.dappstore.model.entity.AppStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

// ============================================
// 管理员审核请求
// ============================================

/**
 * 审核应用请求
 */
data class ReviewAppRequest(
    val approved: Boolean,

    @field:Size(max = 1000, message = "拒绝原因不能超过1000个字符")
    val rejectionReason: String? = null
)

/**
 * 批量审核请求
 */
data class BatchReviewRequest(
    val appIds: List<Long>,
    val approved: Boolean,

    @field:Size(max = 1000, message = "拒绝原因不能超过1000个字符")
    val rejectionReason: String? = null
)

/**
 * 设置精选请求
 */
data class SetFeaturedRequest(
    val featured: Boolean
)

/**
 * 暂停/恢复应用请求
 */
data class SuspendAppRequest(
    val suspended: Boolean,

    @field:Size(max = 1000, message = "暂停原因不能超过1000个字符")
    val reason: String? = null
)

// ============================================
// 管理员审核响应
// ============================================

/**
 * 待审核应用列表项
 */
data class PendingAppItem(
    val id: Long,
    val packageName: String,
    val name: String,
    val iconUrl: String?,
    val versionName: String,
    val versionCode: Long,
    val status: AppStatus,
    val developerName: String?,
    val developerEmail: String?,
    val isWeb3: Boolean,
    val blockchain: String?,
    val categoryName: String?,
    val submittedAt: LocalDateTime?,
    val createdAt: LocalDateTime?
)

/**
 * 管理员应用详情
 */
data class AdminAppDetail(
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
    val isDeleted: Boolean,
    val developer: AdminDeveloperInfo,
    val screenshots: List<ScreenshotInfo>,
    val submittedAt: LocalDateTime?,
    val reviewedAt: LocalDateTime?,
    val reviewerName: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

/**
 * 管理员开发者信息
 */
data class AdminDeveloperInfo(
    val id: Long,
    val userId: Long,
    val companyName: String?,
    val contactEmail: String,
    val websiteUrl: String?,
    val isVerified: Boolean,
    val totalApps: Int,
    val totalDownloads: Long
)

/**
 * 审核统计
 */
data class ReviewStatistics(
    val totalApps: Int,
    val pendingCount: Int,
    val approvedCount: Int,
    val rejectedCount: Int,
    val suspendedCount: Int,
    val todayReviewed: Int,
    val weekReviewed: Int
)

/**
 * 批量审核结果
 */
data class BatchReviewResult(
    val total: Int,
    val succeeded: Int,
    val failed: Int,
    val failedIds: List<Long>
)
