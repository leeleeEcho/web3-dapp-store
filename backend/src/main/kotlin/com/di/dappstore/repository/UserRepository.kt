package com.di.dappstore.repository

import com.di.dappstore.model.entity.User
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserRepository : ReactiveCrudRepository<User, Long> {

    fun findByWalletAddress(walletAddress: String): Mono<User>

    fun findByGoogleId(googleId: String): Mono<User>

    fun existsByWalletAddress(walletAddress: String): Mono<Boolean>

    fun existsByGoogleId(googleId: String): Mono<Boolean>

    fun findByEmail(email: String): Mono<User>

    fun findByUsername(username: String): Mono<User>
}
