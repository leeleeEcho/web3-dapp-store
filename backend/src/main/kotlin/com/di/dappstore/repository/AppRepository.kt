package com.di.dappstore.repository

import com.di.dappstore.model.entity.App
import com.di.dappstore.model.entity.AppStatus
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface AppRepository : ReactiveCrudRepository<App, Long> {

    fun findByPackageName(packageName: String): Mono<App>

    fun findByDeveloperId(developerId: Long): Flux<App>

    fun findByStatus(status: AppStatus): Flux<App>

    // ============================================
    // 开发者应用管理查询
    // ============================================

    @Query("SELECT * FROM apps WHERE developer_id = :developerId AND is_deleted = false ORDER BY updated_at DESC LIMIT :limit OFFSET :offset")
    fun findByDeveloperIdPaged(developerId: Long, limit: Int, offset: Int): Flux<App>

    @Query("SELECT COUNT(*) FROM apps WHERE developer_id = :developerId AND is_deleted = false")
    fun countByDeveloperId(developerId: Long): Mono<Long>

    @Query("SELECT * FROM apps WHERE developer_id = :developerId AND status = :status AND is_deleted = false ORDER BY updated_at DESC")
    fun findByDeveloperIdAndStatus(developerId: Long, status: AppStatus): Flux<App>

    @Query("SELECT COUNT(*) FROM apps WHERE developer_id = :developerId AND status = :status AND is_deleted = false")
    fun countByDeveloperIdAndStatus(developerId: Long, status: AppStatus): Mono<Long>

    @Query("SELECT * FROM apps WHERE developer_id = :developerId AND id = :appId AND is_deleted = false")
    fun findByIdAndDeveloperId(appId: Long, developerId: Long): Mono<App>

    @Query("SELECT SUM(download_count) FROM apps WHERE developer_id = :developerId AND is_deleted = false")
    fun sumDownloadsByDeveloperId(developerId: Long): Mono<Long>

    @Query("SELECT AVG(rating_average) FROM apps WHERE developer_id = :developerId AND is_deleted = false AND rating_count > 0")
    fun avgRatingByDeveloperId(developerId: Long): Mono<Double>

    // ============================================
    // 管理员审核查询
    // ============================================

    @Query("SELECT * FROM apps WHERE status = :status ORDER BY submitted_at ASC LIMIT :limit OFFSET :offset")
    fun findByStatusPaged(status: AppStatus, limit: Int, offset: Int): Flux<App>

    @Query("SELECT COUNT(*) FROM apps WHERE status = :status")
    fun countByStatus(status: AppStatus): Mono<Long>

    @Query("SELECT * FROM apps ORDER BY updated_at DESC LIMIT :limit OFFSET :offset")
    fun findAllPaged(limit: Int, offset: Int): Flux<App>

    @Query("SELECT COUNT(*) FROM apps")
    fun countAll(): Mono<Long>

    @Query("SELECT COUNT(*) FROM apps WHERE reviewed_at >= :since")
    fun countReviewedSince(since: java.time.LocalDateTime): Mono<Long>

    @Query("SELECT * FROM apps WHERE id IN (:ids)")
    fun findByIds(ids: List<Long>): Flux<App>

    fun findByCategoryId(categoryId: Long): Flux<App>

    fun findByIsWeb3(isWeb3: Boolean): Flux<App>

    fun findByBlockchain(blockchain: String): Flux<App>

    fun findByIsFeaturedTrue(): Flux<App>

    @Query("SELECT * FROM apps WHERE status = 'APPROVED' AND is_deleted = false ORDER BY download_count DESC LIMIT :limit")
    fun findTopDownloaded(limit: Int): Flux<App>

    @Query("SELECT * FROM apps WHERE status = 'APPROVED' AND is_deleted = false ORDER BY rating_average DESC LIMIT :limit")
    fun findTopRated(limit: Int): Flux<App>

    @Query("SELECT * FROM apps WHERE status = 'APPROVED' AND is_deleted = false ORDER BY created_at DESC LIMIT :limit")
    fun findLatest(limit: Int): Flux<App>

    @Query("SELECT * FROM apps WHERE status = 'APPROVED' AND is_deleted = false AND (LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    fun searchByKeyword(keyword: String): Flux<App>

    @Query("SELECT * FROM apps WHERE status = 'APPROVED' AND is_deleted = false AND category_id = :categoryId ORDER BY download_count DESC LIMIT :limit OFFSET :offset")
    fun findByCategoryIdPaged(categoryId: Long, limit: Int, offset: Int): Flux<App>

    @Query("SELECT COUNT(*) FROM apps WHERE status = 'APPROVED' AND is_deleted = false AND category_id = :categoryId")
    fun countByCategoryId(categoryId: Long): Mono<Long>

    @Query("SELECT COUNT(*) FROM apps WHERE status = 'APPROVED' AND is_deleted = false")
    fun countApproved(): Mono<Long>

    @Query("UPDATE apps SET download_count = download_count + 1 WHERE id = :id")
    fun incrementDownloadCount(id: Long): Mono<Void>

    @Query("SELECT * FROM apps WHERE status = 'APPROVED' AND is_deleted = false ORDER BY download_count DESC LIMIT :limit OFFSET :offset")
    fun findAllApprovedPaged(limit: Int, offset: Int): Flux<App>
}
