package com.web3store.data.remote.mapper

import com.web3store.data.remote.dto.*
import com.web3store.domain.model.*

/**
 * DTO 到领域模型的转换器
 */
object AppMapper {

    fun AppListItemDto.toDomain(): AppListItem {
        return AppListItem(
            id = id,
            packageName = packageName,
            name = name,
            shortDescription = shortDescription,
            iconUrl = iconUrl,
            versionName = versionName,
            apkSize = apkSize,
            downloadCount = downloadCount,
            ratingAverage = ratingAverage,
            ratingCount = ratingCount,
            isWeb3 = isWeb3,
            blockchain = blockchain,
            isFeatured = isFeatured,
            developerName = developerName,
            categoryName = categoryName
        )
    }

    fun AppDetailDto.toDomain(): AppDetail {
        return AppDetail(
            id = id,
            packageName = packageName,
            name = name,
            description = description,
            shortDescription = shortDescription,
            versionName = versionName,
            versionCode = versionCode,
            minSdkVersion = minSdkVersion,
            targetSdkVersion = targetSdkVersion,
            iconUrl = iconUrl,
            apkUrl = apkUrl,
            apkSize = apkSize,
            apkHash = apkHash,
            downloadCount = downloadCount,
            ratingAverage = ratingAverage,
            ratingCount = ratingCount,
            isWeb3 = isWeb3,
            blockchain = blockchain,
            contractAddress = contractAddress,
            websiteUrl = websiteUrl,
            sourceCodeUrl = sourceCodeUrl,
            isFeatured = isFeatured,
            developer = developer.toDomain(),
            category = category?.toDomain(),
            screenshots = screenshots.map { it.toDomain() }
        )
    }

    fun DeveloperSummaryDto.toDomain(): DeveloperSummary {
        return DeveloperSummary(
            id = id,
            companyName = companyName,
            isVerified = isVerified,
            totalApps = totalApps
        )
    }

    fun CategorySummaryDto.toDomain(): CategorySummary {
        return CategorySummary(
            id = id,
            name = name,
            displayName = displayName
        )
    }

    fun ScreenshotDto.toDomain(): Screenshot {
        return Screenshot(
            id = id,
            imageUrl = imageUrl,
            sortOrder = sortOrder,
            width = width,
            height = height
        )
    }

    fun CategoryDto.toDomain(): Category {
        return Category(
            id = id,
            name = name,
            displayName = displayName
        )
    }

    fun List<AppListItemDto>.toDomainList(): List<AppListItem> {
        return map { it.toDomain() }
    }
}
