package com.di.dappstore.model.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * 用户实体 - 支持 Google OAuth 和钱包双认证
 */
@Table("users")
data class User(
    @Column("wallet_address")
    var walletAddress: String? = null,

    @Column("google_id")
    var googleId: String? = null,

    @Column("auth_provider")
    var authProvider: AuthProvider = AuthProvider.WALLET,

    @Column("username")
    var username: String? = null,

    @Column("email")
    var email: String? = null,

    @Column("avatar_url")
    var avatarUrl: String? = null,

    @Column("role")
    var role: UserRole = UserRole.USER,

    @Column("is_active")
    var isActive: Boolean = true,

    @Column("last_login_at")
    var lastLoginAt: java.time.LocalDateTime? = null,

    @Column("nonce")
    var nonce: String? = null  // 用于钱包签名验证
) : BaseEntity()

/**
 * 认证提供者枚举
 */
enum class AuthProvider {
    GOOGLE,     // Google OAuth
    WALLET      // 钱包签名
}

/**
 * 用户角色枚举
 */
enum class UserRole {
    USER,       // 普通用户
    DEVELOPER,  // 开发者
    ADMIN       // 管理员
}
