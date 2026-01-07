package com.di.dappstore.model.dto

import com.di.dappstore.model.entity.AuthProvider
import jakarta.validation.constraints.NotBlank

// ============================================
// Google OAuth 认证
// ============================================

/**
 * Google 登录请求
 */
data class GoogleLoginRequest(
    @field:NotBlank(message = "ID Token 不能为空")
    val idToken: String
)

// ============================================
// 钱包认证
// ============================================

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

// ============================================
// 通用响应
// ============================================

/**
 * 登录响应
 */
data class AuthResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserResponse
)

/**
 * 用户信息响应
 */
data class UserResponse(
    val id: Long,
    val walletAddress: String?,
    val googleId: String?,
    val authProvider: AuthProvider,
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

/**
 * 当前认证用户响应
 */
data class AuthenticatedUserResponse(
    val userId: Long,
    val role: String
)

// ============================================
// 钱包绑定
// ============================================

/**
 * 绑定钱包请求 (Google 用户绑定钱包)
 */
data class BindWalletRequest(
    @field:NotBlank(message = "钱包地址不能为空")
    val walletAddress: String,

    @field:NotBlank(message = "签名不能为空")
    val signature: String,

    @field:NotBlank(message = "消息不能为空")
    val message: String
)
