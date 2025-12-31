package com.di.dappstore.model.vo

import java.time.LocalDateTime

/**
 * App 列表项 VO
 */
data class AppListItem(
    val id: Long,
    val packageName: String,
    val name: String,
    val shortDescription: String?,
    val iconUrl: String?,
    val versionName: String,
    val apkSize: Long,
    val downloadCount: Long,
    val ratingAverage: Double,
    val ratingCount: Long,
    val isWeb3: Boolean,
    val blockchain: String?,
    val isFeatured: Boolean,
    val developerName: String?,
    val categoryName: String?,
    val updatedAt: LocalDateTime
)

/**
 * App 详情 VO
 */
data class AppDetail(
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
    val downloadCount: Long,
    val ratingAverage: Double,
    val ratingCount: Long,
    val isWeb3: Boolean,
    val blockchain: String?,
    val contractAddress: String?,
    val websiteUrl: String?,
    val sourceCodeUrl: String?,
    val isFeatured: Boolean,
    val developer: DeveloperSummary,
    val category: CategorySummary?,
    val screenshots: List<ScreenshotVo>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * 开发者摘要 VO
 */
data class DeveloperSummary(
    val id: Long,
    val companyName: String?,
    val isVerified: Boolean,
    val totalApps: Int
)

/**
 * 分类摘要 VO
 */
data class CategorySummary(
    val id: Long,
    val name: String,
    val displayName: String
)

/**
 * 截图 VO
 */
data class ScreenshotVo(
    val id: Long,
    val imageUrl: String,
    val sortOrder: Int,
    val width: Int?,
    val height: Int?
)

/**
 * 评论 VO
 */
data class ReviewVo(
    val id: Long,
    val userId: Long,
    val username: String?,
    val userAvatar: String?,
    val rating: Int,
    val title: String?,
    val content: String?,
    val helpfulCount: Int,
    val developerReply: String?,
    val developerReplyAt: LocalDateTime?,
    val createdAt: LocalDateTime
)
