package com.di.dappstore.security

import com.di.dappstore.model.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

private val logger = KotlinLogging.logger {}

/**
 * JWT Token 服务
 */
@Service
class JwtService(
    @Value("\${app.jwt.secret}")
    private val jwtSecret: String,

    @Value("\${app.jwt.expiration:86400000}")
    private val jwtExpiration: Long  // 默认 24 小时
) {

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    /**
     * 生成 JWT Token
     */
    fun generateToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration)

        return Jwts.builder()
            .subject(user.id.toString())
            .claim("role", user.role.name)
            .claim("walletAddress", user.walletAddress)
            .claim("googleId", user.googleId)
            .claim("authProvider", user.authProvider.name)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    /**
     * 从 Token 中获取用户 ID
     */
    fun getUserIdFromToken(token: String): Long? {
        return try {
            val claims = parseToken(token)
            claims?.subject?.toLongOrNull()
        } catch (e: Exception) {
            logger.warn { "Failed to get user ID from token: ${e.message}" }
            null
        }
    }

    /**
     * 从 Token 中获取用户角色
     */
    fun getRoleFromToken(token: String): String? {
        return try {
            val claims = parseToken(token)
            claims?.get("role", String::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 验证 Token 是否有效
     */
    fun validateToken(token: String): Boolean {
        return try {
            val claims = parseToken(token)
            claims != null && !isTokenExpired(claims)
        } catch (e: ExpiredJwtException) {
            logger.debug { "Token expired" }
            false
        } catch (e: Exception) {
            logger.warn { "Token validation failed: ${e.message}" }
            false
        }
    }

    /**
     * 解析 Token
     */
    private fun parseToken(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            logger.debug { "Failed to parse token: ${e.message}" }
            null
        }
    }

    /**
     * 检查 Token 是否过期
     */
    private fun isTokenExpired(claims: Claims): Boolean {
        return claims.expiration.before(Date())
    }

    /**
     * 获取 Token 过期时间（毫秒）
     */
    fun getExpirationTime(): Long = jwtExpiration
}
