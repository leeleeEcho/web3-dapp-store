package com.di.dappstore.model.dto

import jakarta.validation.constraints.NotBlank

/**
 * 钱包登录请求 - 获取 nonce
 */
data class WalletNonceRequest(
    @field:NotBlank(message = "钱包地址不能为空")
    val walletAddress: String
)

/**
 * 钱包登录请求 - 验证签名
 */
data class WalletLoginRequest(
    @field:NotBlank(message = "钱包地址不能为空")
    val walletAddress: String,

    @field:NotBlank(message = "签名不能为空")
    val signature: String,

    @field:NotBlank(message = "消息不能为空")
    val message: String
)

/**
 * 登录响应
 */
data class AuthResponse(
    val token: String,
    val expiresIn: Long,
    val user: UserResponse
)

/**
 * 用户信息响应
 */
data class UserResponse(
    val id: Long,
    val walletAddress: String,
    val username: String?,
    val email: String?,
    val avatarUrl: String?,
    val role: String,
    val createdAt: java.time.LocalDateTime
)

/**
 * Nonce 响应
 */
data class NonceResponse(
    val nonce: String,
    val message: String
)
