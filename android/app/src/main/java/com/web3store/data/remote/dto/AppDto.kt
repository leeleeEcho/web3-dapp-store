package com.web3store.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 应用列表项
 */
data class AppListItemDto(
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
    val updatedAt: String
)

/**
 * 应用详情
 */
data class AppDetailDto(
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
    val developer: DeveloperSummaryDto,
    val category: CategorySummaryDto?,
    val screenshots: List<ScreenshotDto>,
    val createdAt: String,
    val updatedAt: String
)

/**
 * 开发者摘要
 */
data class DeveloperSummaryDto(
    val id: Long,
    val companyName: String?,
    val isVerified: Boolean,
    val totalApps: Int
)

/**
 * 分类摘要
 */
data class CategorySummaryDto(
    val id: Long,
    val name: String,
    val displayName: String
)

/**
 * 截图
 */
data class ScreenshotDto(
    val id: Long,
    val imageUrl: String,
    val sortOrder: Int,
    val width: Int?,
    val height: Int?
)
