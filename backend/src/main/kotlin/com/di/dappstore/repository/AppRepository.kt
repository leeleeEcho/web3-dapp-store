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
