package com.di.dappstore.security

import org.springframework.security.core.context.ReactiveSecurityContextHolder
import reactor.core.publisher.Mono

/**
 * 安全工具类 - 用于获取当前认证用户信息
 */
object SecurityUtils {

    /**
     * 获取当前用户 ID
     */
    fun getCurrentUserId(): Mono<Long> {
        return ReactiveSecurityContextHolder.getContext()
            .map { context ->
                val principal = context.authentication?.principal
                when (principal) {
                    is AuthenticatedUser -> principal.userId
                    else -> throw UnauthorizedException("用户未认证")
                }
            }
            .switchIfEmpty(Mono.error(UnauthorizedException("用户未认证")))
    }

    /**
     * 获取当前用户角色
     */
    fun getCurrentUserRole(): Mono<String> {
        return ReactiveSecurityContextHolder.getContext()
            .map { context ->
                val principal = context.authentication?.principal
                when (principal) {
                    is AuthenticatedUser -> principal.role
                    else -> throw UnauthorizedException("用户未认证")
                }
            }
            .switchIfEmpty(Mono.error(UnauthorizedException("用户未认证")))
    }

    /**
     * 获取当前认证用户
     */
    fun getCurrentUser(): Mono<AuthenticatedUser> {
        return ReactiveSecurityContextHolder.getContext()
            .map { context ->
                val principal = context.authentication?.principal
                when (principal) {
                    is AuthenticatedUser -> principal
                    else -> throw UnauthorizedException("用户未认证")
                }
            }
            .switchIfEmpty(Mono.error(UnauthorizedException("用户未认证")))
    }

    /**
     * 检查是否已认证
     */
    fun isAuthenticated(): Mono<Boolean> {
        return ReactiveSecurityContextHolder.getContext()
            .map { context ->
                context.authentication?.principal is AuthenticatedUser
            }
            .defaultIfEmpty(false)
    }

    /**
     * 检查是否有指定角色
     */
    fun hasRole(role: String): Mono<Boolean> {
        return getCurrentUserRole()
            .map { currentRole -> currentRole == role }
            .onErrorReturn(false)
    }
}

/**
 * 未授权异常
 */
class UnauthorizedException(message: String) : RuntimeException(message)
