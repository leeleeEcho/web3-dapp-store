package com.di.dappstore.security

import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * JWT 认证过滤器
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : WebFilter {

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = extractToken(exchange)

        if (token == null || !jwtService.validateToken(token)) {
            return chain.filter(exchange)
        }

        val userId = jwtService.getUserIdFromToken(token)
        val role = jwtService.getRoleFromToken(token)

        if (userId == null) {
            return chain.filter(exchange)
        }

        // 创建认证信息
        val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
        val principal = AuthenticatedUser(userId, role ?: "USER")
        val authentication = UsernamePasswordAuthenticationToken(principal, null, authorities)

        logger.debug { "Authenticated user: $userId with role: $role" }

        return chain.filter(exchange)
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
    }

    private fun extractToken(exchange: ServerWebExchange): String? {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        return if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            authHeader.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }
}

/**
 * 已认证用户信息
 */
data class AuthenticatedUser(
    val userId: Long,
    val role: String
)
