package com.di.dappstore.repository

import com.di.dappstore.model.entity.Review
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ReviewRepository : ReactiveCrudRepository<Review, Long> {

    fun findByAppId(appId: Long): Flux<Review>

    fun findByUserId(userId: Long): Flux<Review>

    fun findByAppIdAndUserId(appId: Long, userId: Long): Mono<Review>

    @Query("SELECT * FROM reviews WHERE app_id = :appId AND is_deleted = false ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun findByAppIdPaged(appId: Long, limit: Int, offset: Int): Flux<Review>

    @Query("SELECT COUNT(*) FROM reviews WHERE app_id = :appId AND is_deleted = false")
    fun countByAppId(appId: Long): Mono<Long>

    @Query("SELECT AVG(rating) FROM reviews WHERE app_id = :appId AND is_deleted = false")
    fun calculateAverageRating(appId: Long): Mono<Double>

    @Query("UPDATE reviews SET is_helpful_count = is_helpful_count + 1 WHERE id = :id")
    fun incrementHelpfulCount(id: Long): Mono<Void>
}
