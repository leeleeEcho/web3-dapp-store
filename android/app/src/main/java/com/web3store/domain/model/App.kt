package com.web3store.domain.model

/**
 * 应用列表项 - 领域模型
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
    val categoryName: String?
) {
    val formattedDownloads: String
        get() = when {
            downloadCount >= 1_000_000 -> "${downloadCount / 1_000_000}M"
            downloadCount >= 1_000 -> "${downloadCount / 1_000}K"
            else -> downloadCount.toString()
        }

    val formattedSize: String
        get() = when {
            apkSize >= 1_000_000_000 -> "${apkSize / 1_000_000_000} GB"
            apkSize >= 1_000_000 -> "${apkSize / 1_000_000} MB"
            apkSize >= 1_000 -> "${apkSize / 1_000} KB"
            else -> "$apkSize B"
        }

    val chains: List<String>
        get() = blockchain?.split(",")?.map { it.trim().uppercase() } ?: emptyList()
}

/**
 * 应用详情 - 领域模型
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
    val screenshots: List<Screenshot>
) {
    val formattedDownloads: String
        get() = when {
            downloadCount >= 1_000_000 -> "${downloadCount / 1_000_000}M"
            downloadCount >= 1_000 -> "${downloadCount / 1_000}K"
            else -> downloadCount.toString()
        }

    val formattedSize: String
        get() = when {
            apkSize >= 1_000_000_000 -> "${apkSize / 1_000_000_000} GB"
            apkSize >= 1_000_000 -> "${apkSize / 1_000_000} MB"
            apkSize >= 1_000 -> "${apkSize / 1_000} KB"
            else -> "$apkSize B"
        }

    val chains: List<String>
        get() = blockchain?.split(",")?.map { it.trim().uppercase() } ?: emptyList()
}

/**
 * 开发者摘要
 */
data class DeveloperSummary(
    val id: Long,
    val companyName: String?,
    val isVerified: Boolean,
    val totalApps: Int
)

/**
 * 分类摘要
 */
data class CategorySummary(
    val id: Long,
    val name: String,
    val displayName: String
)

/**
 * 截图
 */
data class Screenshot(
    val id: Long,
    val imageUrl: String,
    val sortOrder: Int,
    val width: Int?,
    val height: Int?
)

/**
 * 分类
 */
data class Category(
    val id: Long,
    val name: String,
    val displayName: String
)
