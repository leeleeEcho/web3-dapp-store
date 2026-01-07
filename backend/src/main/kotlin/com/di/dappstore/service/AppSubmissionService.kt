package com.di.dappstore.service

import com.di.dappstore.exception.BusinessException
import com.di.dappstore.model.dto.*
import com.di.dappstore.model.entity.App
import com.di.dappstore.model.entity.AppStatus
import com.di.dappstore.model.entity.Screenshot
import com.di.dappstore.model.vo.PageResponse
import com.di.dappstore.repository.AppRepository
import com.di.dappstore.repository.CategoryRepository
import com.di.dappstore.repository.DeveloperRepository
import com.di.dappstore.repository.ScreenshotRepository
import com.di.dappstore.service.search.AppSearchService
import mu.KotlinLogging
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * 应用提交服务
 * 用于开发者提交和管理应用
 */
@Service
class AppSubmissionService(
    private val appRepository: AppRepository,
    private val developerRepository: DeveloperRepository,
    private val categoryRepository: CategoryRepository,
    private val screenshotRepository: ScreenshotRepository,
    @Lazy private val appSearchService: AppSearchService
) {

    /**
     * 提交新应用
     */
    fun submitApp(developerId: Long, request: SubmitAppRequest): Mono<DeveloperAppDetail> {
        // 检查包名是否已存在
        return appRepository.findByPackageName(request.packageName)
            .flatMap<App> {
                Mono.error(BusinessException("包名已存在: ${request.packageName}"))
            }
            .switchIfEmpty(Mono.defer {
                val status = if (request.saveAsDraft) AppStatus.DRAFT else AppStatus.PENDING
                val submittedAt = if (request.saveAsDraft) null else LocalDateTime.now()

                val app = App(
                    packageName = request.packageName,
                    name = request.name,
                    description = request.description,
                    shortDescription = request.shortDescription,
                    versionName = request.versionName,
                    versionCode = request.versionCode,
                    minSdkVersion = request.minSdkVersion,
                    targetSdkVersion = request.targetSdkVersion,
                    iconUrl = request.iconUrl,
                    apkUrl = request.apkUrl,
                    apkHash = request.apkHash,
                    apkSize = request.apkSize,
                    developerId = developerId,
                    categoryId = request.categoryId,
                    isWeb3 = request.isWeb3,
                    blockchain = request.blockchain,
                    contractAddress = request.contractAddress,
                    websiteUrl = request.websiteUrl,
                    sourceCodeUrl = request.sourceCodeUrl,
                    status = status,
                    submittedAt = submittedAt
                )

                appRepository.save(app)
            })
            .flatMap { app ->
                // 保存截图
                saveScreenshots(app.id!!, request.screenshotUrls)
                    .then(Mono.just(app))
            }
            .flatMap { app ->
                // 更新开发者应用计数
                developerRepository.incrementTotalApps(developerId)
                    .then(Mono.just(app))
            }
            .flatMap { app -> toDeveloperAppDetail(app) }
            .doOnSuccess { logger.info { "App submitted: ${request.packageName} by developer: $developerId" } }
    }

    /**
     * 更新应用信息 (仅限草稿或被拒绝的应用)
     */
    fun updateApp(developerId: Long, appId: Long, request: UpdateAppSubmissionRequest): Mono<DeveloperAppDetail> {
        return appRepository.findByIdAndDeveloperId(appId, developerId)
            .switchIfEmpty(Mono.error(BusinessException("应用不存在或无权访问")))
            .filter { it.status == AppStatus.DRAFT || it.status == AppStatus.REJECTED }
            .switchIfEmpty(Mono.error(BusinessException("只能编辑草稿或被拒绝的应用")))
            .map { app ->
                request.name?.let { app.name = it }
                request.description?.let { app.description = it }
                request.shortDescription?.let { app.shortDescription = it }
                request.categoryId?.let { app.categoryId = it }
                request.blockchain?.let { app.blockchain = it }
                request.contractAddress?.let { app.contractAddress = it }
                request.websiteUrl?.let { app.websiteUrl = it }
                request.sourceCodeUrl?.let { app.sourceCodeUrl = it }
                request.iconUrl?.let { app.iconUrl = it }

                // 如果是被拒绝的应用，清除拒绝原因
                if (app.status == AppStatus.REJECTED) {
                    app.rejectionReason = null
                }
                app
            }
            .flatMap { app ->
                // 更新截图
                request.screenshotUrls?.let { urls ->
                    screenshotRepository.deleteByAppId(app.id!!)
                        .then(saveScreenshots(app.id!!, urls))
                        .then(Mono.just(app))
                } ?: Mono.just(app)
            }
            .flatMap { appRepository.save(it) }
            .flatMap { toDeveloperAppDetail(it) }
    }

    /**
     * 提交审核 (将草稿/被拒绝的应用提交审核)
     */
    fun submitForReview(developerId: Long, appId: Long): Mono<DeveloperAppDetail> {
        return appRepository.findByIdAndDeveloperId(appId, developerId)
            .switchIfEmpty(Mono.error(BusinessException("应用不存在或无权访问")))
            .filter { it.status == AppStatus.DRAFT || it.status == AppStatus.REJECTED }
            .switchIfEmpty(Mono.error(BusinessException("只有草稿或被拒绝的应用可以提交审核")))
            .map { app ->
                app.status = AppStatus.PENDING
                app.submittedAt = LocalDateTime.now()
                app.rejectionReason = null
                app
            }
            .flatMap { appRepository.save(it) }
            .flatMap { toDeveloperAppDetail(it) }
            .doOnSuccess { logger.info { "App submitted for review: appId=$appId, developerId=$developerId" } }
    }

    /**
     * 发布新版本
     */
    fun publishNewVersion(developerId: Long, appId: Long, request: PublishVersionRequest): Mono<DeveloperAppDetail> {
        return appRepository.findByIdAndDeveloperId(appId, developerId)
            .switchIfEmpty(Mono.error(BusinessException("应用不存在或无权访问")))
            .filter { it.status == AppStatus.APPROVED }
            .switchIfEmpty(Mono.error(BusinessException("只有已上架的应用可以发布新版本")))
            .filter { request.versionCode > it.versionCode }
            .switchIfEmpty(Mono.error(BusinessException("新版本号必须大于当前版本")))
            .map { app ->
                app.versionName = request.versionName
                app.versionCode = request.versionCode
                app.apkUrl = request.apkUrl
                app.apkHash = request.apkHash
                app.apkSize = request.apkSize
                request.minSdkVersion?.let { app.minSdkVersion = it }
                request.targetSdkVersion?.let { app.targetSdkVersion = it }

                // 新版本需要重新审核，除非保存为草稿
                app.status = if (request.saveAsDraft) AppStatus.DRAFT else AppStatus.PENDING
                app.submittedAt = if (request.saveAsDraft) null else LocalDateTime.now()
                app.reviewedAt = null
                app.reviewerId = null
                app
            }
            .flatMap { appRepository.save(it) }
            .flatMap { toDeveloperAppDetail(it) }
            .doOnSuccess { logger.info { "New version published: appId=$appId, version=${request.versionName}" } }
    }

    /**
     * 获取开发者应用列表
     */
    fun getDeveloperApps(
        developerId: Long,
        status: AppStatus? = null,
        page: Int = 0,
        size: Int = 20
    ): Mono<PageResponse<DeveloperAppItem>> {
        val offset = page * size

        val appsFlux = if (status != null) {
            appRepository.findByDeveloperIdAndStatus(developerId, status)
        } else {
            appRepository.findByDeveloperIdPaged(developerId, size, offset)
        }

        val countMono = if (status != null) {
            appRepository.countByDeveloperIdAndStatus(developerId, status)
        } else {
            appRepository.countByDeveloperId(developerId)
        }

        return appsFlux
            .flatMap { app -> toDeveloperAppItem(app) }
            .collectList()
            .zipWith(countMono) { apps, total ->
                PageResponse.of(apps, page, size, total)
            }
    }

    /**
     * 获取开发者应用详情
     */
    fun getDeveloperAppDetail(developerId: Long, appId: Long): Mono<DeveloperAppDetail> {
        return appRepository.findByIdAndDeveloperId(appId, developerId)
            .switchIfEmpty(Mono.error(BusinessException("应用不存在或无权访问")))
            .flatMap { toDeveloperAppDetail(it) }
    }

    /**
     * 获取开发者应用统计
     */
    fun getAppStatistics(developerId: Long): Mono<AppStatistics> {
        return Mono.zip(
            appRepository.countByDeveloperId(developerId),
            appRepository.countByDeveloperIdAndStatus(developerId, AppStatus.DRAFT),
            appRepository.countByDeveloperIdAndStatus(developerId, AppStatus.PENDING),
            appRepository.countByDeveloperIdAndStatus(developerId, AppStatus.APPROVED),
            appRepository.countByDeveloperIdAndStatus(developerId, AppStatus.REJECTED),
            appRepository.sumDownloadsByDeveloperId(developerId).defaultIfEmpty(0L),
            appRepository.avgRatingByDeveloperId(developerId).defaultIfEmpty(0.0)
        ).map { tuple ->
            AppStatistics(
                totalApps = tuple.t1.toInt(),
                draftCount = tuple.t2.toInt(),
                pendingCount = tuple.t3.toInt(),
                approvedCount = tuple.t4.toInt(),
                rejectedCount = tuple.t5.toInt(),
                totalDownloads = tuple.t6,
                averageRating = tuple.t7
            )
        }
    }

    /**
     * 删除应用 (软删除，仅限草稿)
     */
    fun deleteApp(developerId: Long, appId: Long): Mono<Void> {
        return appRepository.findByIdAndDeveloperId(appId, developerId)
            .switchIfEmpty(Mono.error(BusinessException("应用不存在或无权访问")))
            .filter { it.status == AppStatus.DRAFT }
            .switchIfEmpty(Mono.error(BusinessException("只能删除草稿状态的应用")))
            .map { app ->
                app.isDeleted = true
                app
            }
            .flatMap { appRepository.save(it) }
            .then()
            .doOnSuccess { logger.info { "App deleted: appId=$appId, developerId=$developerId" } }
    }

    /**
     * 保存截图
     */
    private fun saveScreenshots(appId: Long, screenshotUrls: List<String>?): Mono<Void> {
        if (screenshotUrls.isNullOrEmpty()) {
            return Mono.empty()
        }

        return Flux.fromIterable(screenshotUrls.mapIndexed { index, url ->
            Screenshot(
                appId = appId,
                imageUrl = url,
                sortOrder = index
            )
        })
            .flatMap { screenshotRepository.save(it) }
            .then()
    }

    /**
     * 转换为开发者应用列表项
     */
    private fun toDeveloperAppItem(app: App): Mono<DeveloperAppItem> {
        val categoryMono = app.categoryId?.let {
            categoryRepository.findById(it).map { c -> c.displayName }
        } ?: Mono.just("")

        return categoryMono
            .defaultIfEmpty("")
            .map { categoryName ->
                DeveloperAppItem(
                    id = app.id!!,
                    packageName = app.packageName,
                    name = app.name,
                    iconUrl = app.iconUrl,
                    versionName = app.versionName,
                    versionCode = app.versionCode,
                    status = app.status,
                    rejectionReason = app.rejectionReason,
                    downloadCount = app.downloadCount,
                    ratingAverage = app.ratingAverage,
                    categoryName = categoryName.ifEmpty { null },
                    submittedAt = app.submittedAt,
                    reviewedAt = app.reviewedAt,
                    createdAt = app.createdAt,
                    updatedAt = app.updatedAt
                )
            }
    }

    /**
     * 转换为开发者应用详情
     */
    private fun toDeveloperAppDetail(app: App): Mono<DeveloperAppDetail> {
        val categoryMono = app.categoryId?.let {
            categoryRepository.findById(it)
        } ?: Mono.empty()

        val screenshotsMono = screenshotRepository.findByAppIdSorted(app.id!!).collectList()

        return Mono.zip(
            categoryMono.map { it.displayName }.defaultIfEmpty(""),
            categoryMono.map { it.id }.defaultIfEmpty(0L),
            screenshotsMono
        ).map { tuple ->
            val categoryName = tuple.t1.ifEmpty { null }
            val categoryId = if (tuple.t2 == 0L) null else tuple.t2
            val screenshots = tuple.t3

            DeveloperAppDetail(
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
                categoryId = app.categoryId ?: categoryId,
                categoryName = categoryName,
                isWeb3 = app.isWeb3,
                blockchain = app.blockchain,
                contractAddress = app.contractAddress,
                websiteUrl = app.websiteUrl,
                sourceCodeUrl = app.sourceCodeUrl,
                downloadCount = app.downloadCount,
                ratingAverage = app.ratingAverage,
                ratingCount = app.ratingCount,
                status = app.status,
                rejectionReason = app.rejectionReason,
                isFeatured = app.isFeatured,
                screenshots = screenshots.map { s ->
                    ScreenshotInfo(
                        id = s.id!!,
                        imageUrl = s.imageUrl,
                        sortOrder = s.sortOrder
                    )
                },
                submittedAt = app.submittedAt,
                reviewedAt = app.reviewedAt,
                createdAt = app.createdAt,
                updatedAt = app.updatedAt
            )
        }.onErrorResume {
            // 如果分类查询失败，返回不带分类的详情
            screenshotRepository.findByAppIdSorted(app.id!!).collectList()
                .map { screenshots ->
                    DeveloperAppDetail(
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
                        categoryId = app.categoryId,
                        categoryName = null,
                        isWeb3 = app.isWeb3,
                        blockchain = app.blockchain,
                        contractAddress = app.contractAddress,
                        websiteUrl = app.websiteUrl,
                        sourceCodeUrl = app.sourceCodeUrl,
                        downloadCount = app.downloadCount,
                        ratingAverage = app.ratingAverage,
                        ratingCount = app.ratingCount,
                        status = app.status,
                        rejectionReason = app.rejectionReason,
                        isFeatured = app.isFeatured,
                        screenshots = screenshots.map { s ->
                            ScreenshotInfo(
                                id = s.id!!,
                                imageUrl = s.imageUrl,
                                sortOrder = s.sortOrder
                            )
                        },
                        submittedAt = app.submittedAt,
                        reviewedAt = app.reviewedAt,
                        createdAt = app.createdAt,
                        updatedAt = app.updatedAt
                    )
                }
        }
    }
}
