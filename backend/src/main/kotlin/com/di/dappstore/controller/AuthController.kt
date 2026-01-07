package com.di.dappstore.controller

import com.di.dappstore.model.dto.*
import com.di.dappstore.model.vo.ApiResponse
import com.di.dappstore.security.AuthService
import com.di.dappstore.security.AuthenticationException
import com.di.dappstore.security.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "用户认证相关接口 - 支持 Google OAuth 和钱包签名双认证")
class AuthController(
    private val authService: AuthService
) {

    // ============================================
    // Google OAuth 认证
    // ============================================

    @PostMapping("/google")
    @Operation(summary = "Google 登录", description = "使用 Google ID Token 进行登录")
    fun loginWithGoogle(
        @Valid @RequestBody request: GoogleLoginRequest
    ): Mono<ApiResponse<AuthResponse>> {
        return authService.loginWithGoogle(request.idToken)
            .map { ApiResponse.success(it, "登录成功") }
            .onErrorResume(AuthenticationException::class.java) { e ->
                Mono.just(ApiResponse.error(e.message ?: "认证失败", 401))
            }
    }

    // ============================================
    // 钱包签名认证
    // ============================================

    @PostMapping("/wallet/nonce")
    @Operation(summary = "获取登录 Nonce", description = "获取用于钱包签名的随机 nonce")
    fun getNonce(
        @Valid @RequestBody request: WalletNonceRequest
    ): Mono<ApiResponse<NonceResponse>> {
        return authService.generateAndSaveNonce(request.walletAddress)
            .map { nonce ->
                val message = "Sign this message to login to DApp Store.\n\nNonce: $nonce"
                ApiResponse.success(NonceResponse(nonce = nonce, message = message))
            }
    }

    @PostMapping("/wallet/login")
    @Operation(summary = "钱包登录", description = "使用钱包签名进行登录")
    fun loginWithWallet(
        @Valid @RequestBody request: WalletLoginRequest
    ): Mono<ApiResponse<AuthResponse>> {
        return authService.loginWithWallet(request.walletAddress, request.signature, request.message)
            .map { ApiResponse.success(it, "登录成功") }
            .onErrorResume(AuthenticationException::class.java) { e ->
                Mono.just(ApiResponse.error(e.message ?: "认证失败", 401))
            }
    }

    // ============================================
    // 钱包绑定
    // ============================================

    @PostMapping("/bind-wallet")
    @Operation(summary = "绑定钱包", description = "Google 用户绑定钱包地址")
    fun bindWallet(
        @Valid @RequestBody request: BindWalletRequest
    ): Mono<ApiResponse<UserResponse>> {
        return SecurityUtils.getCurrentUserId()
            .flatMap { userId ->
                authService.bindWallet(userId, request.walletAddress, request.signature, request.message)
            }
            .map { user ->
                ApiResponse.success(
                    UserResponse(
                        id = user.id!!,
                        walletAddress = user.walletAddress,
                        googleId = user.googleId,
                        authProvider = user.authProvider,
                        username = user.username,
                        email = user.email,
                        avatarUrl = user.avatarUrl,
                        role = user.role.name,
                        createdAt = user.createdAt!!
                    ),
                    "钱包绑定成功"
                )
            }
            .onErrorResume(AuthenticationException::class.java) { e ->
                Mono.just(ApiResponse.error(e.message ?: "绑定失败", 400))
            }
    }

    // ============================================
    // 当前用户
    // ============================================

    @GetMapping("/me")
    @Operation(summary = "获取当前用户", description = "获取当前登录用户信息")
    fun getCurrentUser(): Mono<ApiResponse<AuthenticatedUserResponse>> {
        return SecurityUtils.getCurrentUser()
            .map { user ->
                ApiResponse.success(
                    AuthenticatedUserResponse(
                        userId = user.userId,
                        role = user.role
                    )
                )
            }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error("未登录", 401))
            }
    }

    @PostMapping("/logout")
    @Operation(summary = "登出", description = "用户登出")
    fun logout(): Mono<ApiResponse<Unit>> {
        // JWT 无状态，客户端直接删除 token 即可
        return Mono.just(ApiResponse.success(Unit, "已登出"))
    }
}
