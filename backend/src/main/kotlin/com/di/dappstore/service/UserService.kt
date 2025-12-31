package com.di.dappstore.service

import com.di.dappstore.model.entity.User
import com.di.dappstore.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class UserService(
    private val userRepository: UserRepository
) {

    /**
     * 根据钱包地址获取用户
     */
    fun getUserByWalletAddress(walletAddress: String): Mono<User> {
        return userRepository.findByWalletAddress(walletAddress.lowercase())
    }

    /**
     * 根据 ID 获取用户
     */
    fun getUserById(id: Long): Mono<User> {
        return userRepository.findById(id)
    }

    /**
     * 创建用户或获取现有用户
     */
    fun getOrCreateUser(walletAddress: String): Mono<User> {
        val normalizedAddress = walletAddress.lowercase()
        return userRepository.findByWalletAddress(normalizedAddress)
            .switchIfEmpty(
                createUser(normalizedAddress)
            )
    }

    /**
     * 创建新用户
     */
    private fun createUser(walletAddress: String): Mono<User> {
        val user = User(
            walletAddress = walletAddress,
            nonce = generateNonce()
        )
        return userRepository.save(user)
            .doOnSuccess { logger.info { "New user created: ${it.walletAddress}" } }
    }

    /**
     * 生成新的 nonce
     */
    fun generateAndSaveNonce(walletAddress: String): Mono<String> {
        return getOrCreateUser(walletAddress)
            .flatMap { user ->
                user.nonce = generateNonce()
                userRepository.save(user)
            }
            .map { it.nonce!! }
    }

    /**
     * 更新最后登录时间
     */
    fun updateLastLogin(userId: Long): Mono<User> {
        return userRepository.findById(userId)
            .flatMap { user ->
                user.lastLoginAt = LocalDateTime.now()
                user.nonce = generateNonce() // 登录后刷新 nonce
                userRepository.save(user)
            }
    }

    /**
     * 更新用户信息
     */
    fun updateUser(userId: Long, username: String?, email: String?, avatarUrl: String?): Mono<User> {
        return userRepository.findById(userId)
            .flatMap { user ->
                username?.let { user.username = it }
                email?.let { user.email = it }
                avatarUrl?.let { user.avatarUrl = it }
                userRepository.save(user)
            }
    }

    /**
     * 生成随机 nonce
     */
    private fun generateNonce(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
}
