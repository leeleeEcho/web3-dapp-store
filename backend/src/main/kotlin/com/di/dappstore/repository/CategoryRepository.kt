package com.di.dappstore.repository

import com.di.dappstore.model.entity.Category
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface CategoryRepository : ReactiveCrudRepository<Category, Long> {

    fun findByName(name: String): Mono<Category>

    fun findByIsActiveTrue(): Flux<Category>

    @Query("SELECT * FROM categories WHERE is_active = true ORDER BY sort_order ASC")
    fun findAllActiveSorted(): Flux<Category>

    @Query("UPDATE categories SET app_count = app_count + 1 WHERE id = :id")
    fun incrementAppCount(id: Long): Mono<Void>

    @Query("UPDATE categories SET app_count = app_count - 1 WHERE id = :id AND app_count > 0")
    fun decrementAppCount(id: Long): Mono<Void>
}
