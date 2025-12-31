package com.di.dappstore.repository

import com.di.dappstore.model.entity.Screenshot
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ScreenshotRepository : ReactiveCrudRepository<Screenshot, Long> {

    @Query("SELECT * FROM screenshots WHERE app_id = :appId ORDER BY sort_order ASC")
    fun findByAppIdSorted(appId: Long): Flux<Screenshot>

    fun deleteByAppId(appId: Long): Mono<Void>
}
