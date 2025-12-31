package com.di.dappstore.repository

import com.di.dappstore.model.entity.Developer
import com.di.dappstore.model.entity.VerificationStatus
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface DeveloperRepository : ReactiveCrudRepository<Developer, Long> {

    fun findByUserId(userId: Long): Mono<Developer>

    fun findByContactEmail(email: String): Mono<Developer>

    fun findByVerificationStatus(status: VerificationStatus): Flux<Developer>

    fun findByIsVerifiedTrue(): Flux<Developer>

    @Query("UPDATE developers SET total_apps = total_apps + 1 WHERE id = :id")
    fun incrementTotalApps(id: Long): Mono<Void>

    @Query("UPDATE developers SET total_downloads = total_downloads + 1 WHERE id = :id")
    fun incrementTotalDownloads(id: Long): Mono<Void>
}
