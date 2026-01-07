package com.di.dappstore.service

import com.di.dappstore.exception.BusinessException
import com.di.dappstore.model.dto.*
import com.di.dappstore.model.entity.App
import com.di.dappstore.model.entity.AppStatus
import com.di.dappstore.model.vo.PageResponse
import com.di.dappstore.repository.*
import com.di.dappstore.service.search.AppIndexSyncService
import mu.KotlinLogging
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * 管理员应用审核服务
 */
@Service
class AdminAppService(
    private val appRepository: AppRepository,
    private val developerRepository: DeveloperRepository,
    private val categoryRepository: CategoryRepository,
    private val screenshotRepository: ScreenshotRepository,
    private val userRepository: UserRepository,
    @Lazy private val appIndexSyncService: AppIndexSyncService
) {

    /**
     * 获取待审核应用列表
     */
    fun getPendingApps(page: Int, size: Int): Mono<PageResponse<PendingAppItem>> {
        val offset = page * size
        return appRepository.findByStatusPaged(AppStatus.PENDING, size, offset)
            .flatMap { app -> toPendingAppItem(app) }
            .collectList()
            .zipWith(appRepository.countByStatus(AppStatus.PENDING)) { apps, total ->
                PageResponse.of(apps, page, size, total)
            }
    }

    /**
     * 获取所有应用列表 (管理员视角)
     */
    fun getAllApps(
        status: AppStatus? = null,
        page: Int = 0,
        size: Int = 20
    ): Mono<PageResponse<PendingAppItem>> {
        val offset = page * size

        val appsFlux = if (status != null) {
            appRepository.findByStatusPaged(status, size, offset)
        } else {
            appRepository.findAllPaged(size, offset)
        }

        val countMono = if (status != null) {
            appRepository.countByStatus(status)
        } else {
            appRepository.countAll()
        }

        return appsFlux
            .flatMap { app -> toPendingAppItem(app) }
            .collectList()
            .zipWith(countMono) { apps, total ->
                PageResponse.of(apps, page, size, total)
            }
    }

    /**
     * 获取应用详情 (管理员视角)
     */
    fun getAppDetail(appId: Long): Mono<AdminAppDetail> {
        return appRepository.findById(appId)
            .switchIfEmpty(Mono.error(BusinessException("应用不存在")))
            .flatMap { app -> toAdminAppDetail(app) }
    }

    /**
     * 审核应用
     */
    fun reviewApp(appId: Long, reviewerId: Long, request: ReviewAppRequest): Mono<AdminAppDetail> {
        return appRepository.findById(appId)
            .switchIfEmpty(Mono.error(BusinessException("应用不存在")))
            .filter { it.status == AppStatus.PENDING }
            .switchIfEmpty(Mono.error(BusinessException("只能审核待审核状态的应用")))
            .map { app ->
                if (request.approved) {
                    app.status = AppStatus.APPROVED
                    app.rejectionReason = null
                } else {
                    if (request.rejectionReason.isNullOrBlank()) {
                        throw BusinessException("拒绝应用时必须提供拒绝原因")
                    }
                    app.status = AppStatus.REJECTED
                    app.rejectionReason = request.rejectionReason
                }
                app.reviewedAt = LocalDateTime.now()
                app.reviewerId = reviewerId
                app
            }
            .flatMap { app ->
                appRepository.save(app)
                    .flatMap { savedApp ->
                        // 如果通过审核，索引到 ES
                        if (request.approved) {
                            indexAppToElasticsearch(savedApp)
                                .then(Mono.just(savedApp))
                        } else {
                            Mono.just(savedApp)
                        }
                    }
            }
            .flatMap { app -> toAdminAppDetail(app) }
            .doOnSuccess {
                val action = if (request.approved) "approved" else "rejected"
                logger.info { "App $action: appId=$appId, reviewerId=$reviewerId" }
            }
    }

    /**
     * 批量审核应用
     */
    fun batchReviewApps(reviewerId: Long, request: BatchReviewRequest): Mono<BatchReviewResult> {
        val failedIds = mutableListOf<Long>()
        var succeeded = 0

        return Flux.fromIterable(request.appIds)
            .flatMap { appId ->
                val reviewRequest = ReviewAppRequest(
                    approved = request.approved,
                    rejectionReason = request.rejectionReason
                )
                reviewApp(appId, reviewerId, reviewRequest)
                    .map { appId to true }
                    .onErrorResume { e ->
                        logger.warn { "Batch review failed for app $appId: ${e.message}" }
                        Mono.just(appId to false)
                    }
            }
            .collectList()
            .map { results ->
                results.forEach { (appId, success) ->
                    if (success) {
                        succeeded++
                    } else {
                        failedIds.add(appId)
                    }
                }
                BatchReviewResult(
                    total = request.appIds.size,
                    succeeded = succeeded,
                    failed = failedIds.size,
                    failedIds = failedIds
                )
            }
    }

    /**
     * 设置/取消精选
     */
    fun setFeatured(appId: Long, request: SetFeaturedRequest): Mono<AdminAppDetail> {
        return appRepository.findById(appId)
            .switchIfEmpty(Mono.error(BusinessException("应用不存在")))
            .filter { it.status == AppStatus.APPROVED }
            .switchIfEmpty(Mono.error(BusinessException("只有已上架的应用可以设置精选")))
            .map { app ->
                app.isFeatured = request.featured
                app
            }
            .flatMap { appRepository.save(it) }
            .flatMap { toAdminAppDetail(it) }
            .doOnSuccess {
                val action = if (request.featured) "featured" else "unfeatured"
                logger.info { "App $action: appId=$appId" }
            }
    }

    /**
     * 暂停/恢复应用
     */
    fun suspendApp(appId: Long, reviewerId: Long, request: SuspendAppRequest): Mono<AdminAppDetail> {
        return appRepository.findById(appId)
            .switchIfEmpty(Mono.error(BusinessException("应用不存在")))
            .flatMap { app ->
                if (request.suspended) {
                    // 暂停应用
                    if (app.status != AppStatus.APPROVED) {
                        return@flatMap Mono.error<App>(BusinessException("只能暂停已上架的应用"))
                    }
                    app.status = AppStatus.SUSPENDED
                    app.rejectionReason = request.reason ?: "管理员暂停"
                    app.reviewedAt = LocalDateTime.now()
                    app.reviewerId = reviewerId

                    // 从 ES 中删除
                    removeAppFromElasticsearch(appId)
                        .then(appRepository.save(app))
                } else {
                    // 恢复应用
                    if (app.status != AppStatus.SUSPENDED) {
                        return@flatMap Mono.error<App>(BusinessException("只能恢复已暂停的应用"))
                    }
                    app.status = AppStatus.APPROVED
                    app.rejectionReason = null
                    app.reviewedAt = LocalDateTime.now()
                    app.reviewerId = reviewerId

                    // 重新索引到 ES
                    appRepository.save(app)
                        .flatMap { savedApp ->
                            indexAppToElasticsearch(savedApp)
                                .then(Mono.just(savedApp))
                        }
                }
            }
            .flatMap { toAdminAppDetail(it) }
            .doOnSuccess {
                val action = if (request.suspended) "suspended" else "resumed"
                logger.info { "App $action: appId=$appId, reviewerId=$reviewerId" }
            }
    }

    /**
     * 获取审核统计
     */
    fun getReviewStatistics(): Mono<ReviewStatistics> {
        val now = LocalDateTime.now()
        val todayStart = now.toLocalDate().atStartOfDay()
        val weekStart = now.minusDays(7)

        return Mono.zip(
            appRepository.countAll(),
            appRepository.countByStatus(AppStatus.PENDING),
            appRepository.countByStatus(AppStatus.APPROVED),
            appRepository.countByStatus(AppStatus.REJECTED),
            appRepository.countByStatus(AppStatus.SUSPENDED),
            appRepository.countReviewedSince(todayStart),
            appRepository.countReviewedSince(weekStart)
        ).map { tuple ->
            ReviewStatistics(
                totalApps = tuple.t1.toInt(),
                pendingCount = tuple.t2.toInt(),
                approvedCount = tuple.t3.toInt(),
                rejectedCount = tuple.t4.toInt(),
                suspendedCount = tuple.t5.toInt(),
                todayReviewed = tuple.t6.toInt(),
                weekReviewed = tuple.t7.toInt()
            )
        }
    }

    /**
     * 索引应用到 Elasticsearch
     */
    private fun indexAppToElasticsearch(app: App): Mono<String> {
        return appIndexSyncService.indexApp(app)
            .onErrorResume { e ->
                logger.warn { "Failed to index app to ES: ${e.message}" }
                Mono.just("")
            }
    }

    /**
     * 从 Elasticsearch 删除应用
     */
    private fun removeAppFromElasticsearch(appId: Long): Mono<Boolean> {
        return appIndexSyncService.deleteFromIndex(appId)
            .onErrorResume { e ->
                logger.warn { "Failed to remove app from ES: ${e.message}" }
                Mono.just(false)
            }
    }

    /**
     * 转换为待审核应用列表项
     */
    private fun toPendingAppItem(app: App): Mono<PendingAppItem> {
        val developerMono = developerRepository.findById(app.developerId)
        val categoryMono = app.categoryId?.let {
            categoryRepository.findById(it).map { c -> c.displayName }
        } ?: Mono.just("")

        return Mono.zip(developerMono, categoryMono.defaultIfEmpty(""))
            .map { tuple ->
                val developer = tuple.t1
                val categoryName = tuple.t2.ifEmpty { null }

                PendingAppItem(
                    id = app.id!!,
                    packageName = app.packageName,
                    name = app.name,
                    iconUrl = app.iconUrl,
                    versionName = app.versionName,
                    versionCode = app.versionCode,
                    status = app.status,
                    developerName = developer.companyName,
                    developerEmail = developer.contactEmail,
                    isWeb3 = app.isWeb3,
                    blockchain = app.blockchain,
                    categoryName = categoryName,
                    submittedAt = app.submittedAt,
                    createdAt = app.createdAt
                )
            }
            .onErrorResume {
                Mono.just(
                    PendingAppItem(
                        id = app.id!!,
                        packageName = app.packageName,
                        name = app.name,
                        iconUrl = app.iconUrl,
                        versionName = app.versionName,
                        versionCode = app.versionCode,
                        status = app.status,
                        developerName = null,
                        developerEmail = null,
                        isWeb3 = app.isWeb3,
                        blockchain = app.blockchain,
                        categoryName = null,
                        submittedAt = app.submittedAt,
                        createdAt = app.createdAt
                    )
                )
            }
    }

    /**
     * 转换为管理员应用详情
     */
    private fun toAdminAppDetail(app: App): Mono<AdminAppDetail> {
        val developerMono = developerRepository.findById(app.developerId)
        val categoryMono = app.categoryId?.let {
            categoryRepository.findById(it)
        } ?: Mono.empty()
        val screenshotsMono = screenshotRepository.findByAppIdSorted(app.id!!).collectList()
        val reviewerMono = app.reviewerId?.let {
            userRepository.findById(it).map { u -> u.username ?: u.email ?: "Admin" }
        } ?: Mono.just("")

        return Mono.zip(
            developerMono,
            categoryMono.map { it.displayName }.defaultIfEmpty(""),
            screenshotsMono,
            reviewerMono.defaultIfEmpty("")
        ).map { tuple ->
            val developer = tuple.t1
            val categoryName = tuple.t2.ifEmpty { null }
            val screenshots = tuple.t3
            val reviewerName = tuple.t4.ifEmpty { null }

            AdminAppDetail(
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
                isDeleted = app.isDeleted,
                developer = AdminDeveloperInfo(
                    id = developer.id!!,
                    userId = developer.userId,
                    companyName = developer.companyName,
                    contactEmail = developer.contactEmail,
                    websiteUrl = developer.websiteUrl,
                    isVerified = developer.isVerified,
                    totalApps = developer.totalApps,
                    totalDownloads = developer.totalDownloads
                ),
                screenshots = screenshots.map { s ->
                    ScreenshotInfo(
                        id = s.id!!,
                        imageUrl = s.imageUrl,
                        sortOrder = s.sortOrder
                    )
                },
                submittedAt = app.submittedAt,
                reviewedAt = app.reviewedAt,
                reviewerName = reviewerName,
                createdAt = app.createdAt,
                updatedAt = app.updatedAt
            )
        }
    }
}
