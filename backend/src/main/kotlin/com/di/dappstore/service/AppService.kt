package com.di.dappstore.service

import com.di.dappstore.model.dto.CreateAppRequest
import com.di.dappstore.model.dto.UpdateAppRequest
import com.di.dappstore.model.entity.App
import com.di.dappstore.model.entity.AppStatus
import com.di.dappstore.model.vo.AppDetail
import com.di.dappstore.model.vo.AppListItem
import com.di.dappstore.model.vo.PageResponse
import com.di.dappstore.repository.AppRepository
import com.di.dappstore.repository.CategoryRepository
import com.di.dappstore.repository.DeveloperRepository
import com.di.dappstore.repository.ScreenshotRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.Optional

private val logger = KotlinLogging.logger {}

@Service
class AppService(
    private val appRepository: AppRepository,
    private val developerRepository: DeveloperRepository,
    private val categoryRepository: CategoryRepository,
    private val screenshotRepository: ScreenshotRepository
) {

    /**
     * 获取应用列表 (分页)
     */
    fun getApps(page: Int, size: Int, categoryId: Long? = null): Mono<PageResponse<AppListItem>> {
        val offset = page * size

        val appsFlux: Flux<App> = if (categoryId != null) {
            appRepository.findByCategoryIdPaged(categoryId, size, offset)
        } else {
            appRepository.findAllApprovedPaged(size, offset)
        }

        val countMono: Mono<Long> = if (categoryId != null) {
            appRepository.countByCategoryId(categoryId)
        } else {
            appRepository.countApproved()
        }

        return appsFlux.flatMap { app -> toAppListItem(app) }
            .collectList()
            .zipWith(countMono) { apps, total ->
                PageResponse.of(apps, page, size, total)
            }
    }

    /**
     * 获取应用详情
     */
    fun getAppById(id: Long): Mono<AppDetail> {
        return appRepository.findById(id)
            .filter { it.status == AppStatus.APPROVED && !it.isDeleted }
            .flatMap { toAppDetail(it) }
    }

    /**
     * 根据包名获取应用
     */
    fun getAppByPackageName(packageName: String): Mono<AppDetail> {
        return appRepository.findByPackageName(packageName)
            .filter { it.status == AppStatus.APPROVED && !it.isDeleted }
            .flatMap { toAppDetail(it) }
    }

    /**
     * 搜索应用
     */
    fun searchApps(keyword: String): Flux<AppListItem> {
        return appRepository.searchByKeyword(keyword)
            .flatMap { toAppListItem(it) }
    }

    /**
     * 获取热门应用
     */
    fun getTopDownloaded(limit: Int = 20): Flux<AppListItem> {
        return appRepository.findTopDownloaded(limit)
            .flatMap { toAppListItem(it) }
    }

    /**
     * 获取高评分应用
     */
    fun getTopRated(limit: Int = 20): Flux<AppListItem> {
        return appRepository.findTopRated(limit)
            .flatMap { toAppListItem(it) }
    }

    /**
     * 获取最新应用
     */
    fun getLatestApps(limit: Int = 20): Flux<AppListItem> {
        return appRepository.findLatest(limit)
            .flatMap { toAppListItem(it) }
    }

    /**
     * 获取推荐应用
     */
    fun getFeaturedApps(): Flux<AppListItem> {
        return appRepository.findByIsFeaturedTrue()
            .filter { it.status == AppStatus.APPROVED && !it.isDeleted }
            .flatMap { toAppListItem(it) }
    }

    /**
     * 创建应用 (开发者提交)
     */
    fun createApp(developerId: Long, request: CreateAppRequest, apkUrl: String, apkHash: String, apkSize: Long): Mono<App> {
        val app = App(
            packageName = request.packageName,
            name = request.name,
            description = request.description,
            shortDescription = request.shortDescription,
            versionName = request.versionName,
            versionCode = request.versionCode,
            minSdkVersion = request.minSdkVersion,
            targetSdkVersion = request.targetSdkVersion,
            apkUrl = apkUrl,
            apkHash = apkHash,
            apkSize = apkSize,
            developerId = developerId,
            categoryId = request.categoryId,
            isWeb3 = request.isWeb3,
            blockchain = request.blockchain,
            contractAddress = request.contractAddress,
            websiteUrl = request.websiteUrl,
            sourceCodeUrl = request.sourceCodeUrl,
            status = AppStatus.PENDING
        )

        return appRepository.save(app)
            .doOnSuccess { logger.info { "App created: ${it.packageName}" } }
    }

    /**
     * 更新应用
     */
    fun updateApp(id: Long, developerId: Long, request: UpdateAppRequest): Mono<App> {
        return appRepository.findById(id)
            .filter { it.developerId == developerId }
            .map { app ->
                request.name?.let { app.name = it }
                request.description?.let { app.description = it }
                request.shortDescription?.let { app.shortDescription = it }
                request.versionName?.let { app.versionName = it }
                request.versionCode?.let { app.versionCode = it }
                request.categoryId?.let { app.categoryId = it }
                request.websiteUrl?.let { app.websiteUrl = it }
                request.sourceCodeUrl?.let { app.sourceCodeUrl = it }
                app
            }
            .flatMap { appRepository.save(it) }
    }

    /**
     * 增加下载计数
     */
    fun incrementDownloadCount(id: Long): Mono<Void> {
        return appRepository.incrementDownloadCount(id)
            .doOnSuccess { logger.debug { "Download count incremented for app: $id" } }
    }

    /**
     * 转换为列表项 VO
     */
    private fun toAppListItem(app: App): Mono<AppListItem> {
        val developerMono = developerRepository.findById(app.developerId)
        val categoryMono = app.categoryId?.let {
            categoryRepository.findById(it).map { c -> Optional.of(c) }
        }?.defaultIfEmpty(Optional.empty()) ?: Mono.just(Optional.empty())

        return Mono.zip(developerMono, categoryMono)
            .map { tuple ->
                AppListItem(
                    id = app.id!!,
                    packageName = app.packageName,
                    name = app.name,
                    shortDescription = app.shortDescription,
                    iconUrl = app.iconUrl,
                    versionName = app.versionName,
                    apkSize = app.apkSize,
                    downloadCount = app.downloadCount,
                    ratingAverage = app.ratingAverage,
                    ratingCount = app.ratingCount,
                    isWeb3 = app.isWeb3,
                    blockchain = app.blockchain,
                    isFeatured = app.isFeatured,
                    developerName = tuple.t1.companyName,
                    categoryName = tuple.t2.orElse(null)?.displayName,
                    updatedAt = app.updatedAt
                )
            }
            .onErrorResume {
                Mono.just(
                    AppListItem(
                        id = app.id!!,
                        packageName = app.packageName,
                        name = app.name,
                        shortDescription = app.shortDescription,
                        iconUrl = app.iconUrl,
                        versionName = app.versionName,
                        apkSize = app.apkSize,
                        downloadCount = app.downloadCount,
                        ratingAverage = app.ratingAverage,
                        ratingCount = app.ratingCount,
                        isWeb3 = app.isWeb3,
                        blockchain = app.blockchain,
                        isFeatured = app.isFeatured,
                        developerName = null,
                        categoryName = null,
                        updatedAt = app.updatedAt
                    )
                )
            }
    }

    /**
     * 转换为详情 VO
     */
    private fun toAppDetail(app: App): Mono<AppDetail> {
        val developerMono = developerRepository.findById(app.developerId)
        val categoryMono = app.categoryId?.let {
            categoryRepository.findById(it).map { c -> Optional.of(c) }
        }?.defaultIfEmpty(Optional.empty()) ?: Mono.just(Optional.empty())
        val screenshotsMono = screenshotRepository.findByAppIdSorted(app.id!!).collectList()

        return Mono.zip(developerMono, categoryMono, screenshotsMono)
            .map { tuple ->
                val developer = tuple.t1
                val category = tuple.t2.orElse(null)
                val screenshots = tuple.t3

                AppDetail(
                    id = app.id!!,
                    packageName = app.packageName,
                    name = app.name,
                    description = app.description,
                    shortDescription = app.shortDescription,
                    versionName = app.versionName,
                    versionCode = app.versionCode,
                    minSdkVersion = app.minSdkVersion,
                    targetSdkVersion = app.targetSdkVersion,
                    iconUrl = app.iconUrl,
                    apkUrl = app.apkUrl,
                    apkSize = app.apkSize,
                    apkHash = app.apkHash,
                    downloadCount = app.downloadCount,
                    ratingAverage = app.ratingAverage,
                    ratingCount = app.ratingCount,
                    isWeb3 = app.isWeb3,
                    blockchain = app.blockchain,
                    contractAddress = app.contractAddress,
                    websiteUrl = app.websiteUrl,
                    sourceCodeUrl = app.sourceCodeUrl,
                    isFeatured = app.isFeatured,
                    developer = com.di.dappstore.model.vo.DeveloperSummary(
                        id = developer.id!!,
                        companyName = developer.companyName,
                        isVerified = developer.isVerified,
                        totalApps = developer.totalApps
                    ),
                    category = category?.let {
                        com.di.dappstore.model.vo.CategorySummary(
                            id = it.id!!,
                            name = it.name,
                            displayName = it.displayName
                        )
                    },
                    screenshots = screenshots.map { s ->
                        com.di.dappstore.model.vo.ScreenshotVo(
                            id = s.id!!,
                            imageUrl = s.imageUrl,
                            sortOrder = s.sortOrder,
                            width = s.width,
                            height = s.height
                        )
                    },
                    createdAt = app.createdAt,
                    updatedAt = app.updatedAt
                )
            }
    }
}
