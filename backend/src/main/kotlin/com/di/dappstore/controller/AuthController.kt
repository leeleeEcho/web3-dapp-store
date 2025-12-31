package com.di.dappstore.controller

import com.di.dappstore.model.dto.NonceResponse
import com.di.dappstore.model.dto.WalletLoginRequest
import com.di.dappstore.model.dto.WalletNonceRequest
import com.di.dappstore.model.vo.ApiResponse
import com.di.dappstore.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "用户认证相关接口")
class AuthController(
    private val userService: UserService
) {

    @PostMapping("/nonce")
    @Operation(summary = "获取登录 Nonce", description = "获取用于钱包签名的随机 nonce")
    fun getNonce(
        @Valid @RequestBody request: WalletNonceRequest
    ): Mono<ApiResponse<NonceResponse>> {
        return userService.generateAndSaveNonce(request.walletAddress)
            .map { nonce ->
                val message = "Sign this message to login to DApp Store.\n\nNonce: $nonce"
                ApiResponse.success(NonceResponse(nonce = nonce, message = message))
            }
    }

    @PostMapping("/login")
    @Operation(summary = "钱包登录", description = "使用钱包签名进行登录")
    fun login(
        @Valid @RequestBody request: WalletLoginRequest
    ): Mono<ApiResponse<Map<String, Any>>> {
        // TODO: 实现签名验证和 JWT 生成
        // 1. 验证签名是否有效
        // 2. 验证 nonce 是否匹配
        // 3. 生成 JWT token
        // 4. 返回 token 和用户信息

        return Mono.just(
            ApiResponse.error<Map<String, Any>>("登录功能开发中", -1)
        )
    }

    @PostMapping("/logout")
    @Operation(summary = "登出", description = "用户登出")
    fun logout(): Mono<ApiResponse<Unit>> {
        // JWT 无状态，客户端直接删除 token 即可
        return Mono.just(ApiResponse.success(Unit, "已登出"))
    }
}
