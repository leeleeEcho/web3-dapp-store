package com.web3store.data.remote.dto

/**
 * Nonce 请求
 */
data class WalletNonceRequest(
    val walletAddress: String
)

/**
 * Nonce 响应
 */
data class NonceResponseDto(
    val nonce: String,
    val message: String
)

/**
 * 钱包登录请求
 */
data class WalletLoginRequest(
    val walletAddress: String,
    val signature: String,
    val message: String
)

/**
 * Google 登录请求
 */
data class GoogleLoginRequest(
    val idToken: String
)

/**
 * 登录响应
 */
data class AuthResponseDto(
    val token: String,
    val expiresIn: Long,
    val user: UserDto
)

/**
 * 用户信息
 */
data class UserDto(
    val id: Long,
    val googleId: String? = null,
    val walletAddress: String? = null,
    val authProvider: String,
    val email: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val role: String,
    val createdAt: String
)
