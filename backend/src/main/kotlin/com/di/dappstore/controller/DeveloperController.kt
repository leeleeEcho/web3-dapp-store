package com.di.dappstore.controller

import com.di.dappstore.model.dto.DeveloperRegistrationRequest
import com.di.dappstore.model.dto.UpdateDeveloperRequest
import com.di.dappstore.model.entity.Developer
import com.di.dappstore.model.vo.ApiResponse
import com.di.dappstore.security.SecurityUtils
import com.di.dappstore.service.DeveloperService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/developers")
@Tag(name = "Developers", description = "开发者相关接口")
class DeveloperController(
    private val developerService: DeveloperService
) {

    @PostMapping("/register")
    @Operation(summary = "注册为开发者", description = "普通用户注册成为开发者（需要登录）")
    fun registerDeveloper(
        @Valid @RequestBody request: DeveloperRegistrationRequest
    ): Mono<ApiResponse<Developer>> {
        return SecurityUtils.getCurrentUserId()
            .flatMap { userId ->
                developerService.registerDeveloper(userId, request)
            }
            .map { ApiResponse.success(it, "注册成功") }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "注册失败", -1))
            }
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前开发者信息", description = "获取当前登录用户的开发者信息（需要登录）")
    fun getCurrentDeveloper(): Mono<ApiResponse<Developer>> {
        return SecurityUtils.getCurrentUserId()
            .flatMap { userId ->
                developerService.getDeveloperByUserId(userId)
            }
            .map { ApiResponse.success(it) }
            .switchIfEmpty(Mono.just(ApiResponse.error("您还不是开发者", 404)))
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "获取失败", -1))
            }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取开发者信息", description = "根据ID获取开发者公开信息")
    fun getDeveloperById(
        @Parameter(description = "开发者ID") @PathVariable id: Long
    ): Mono<ApiResponse<Developer>> {
        return developerService.getDeveloperById(id)
            .map { ApiResponse.success(it) }
            .defaultIfEmpty(ApiResponse.error("开发者不存在", 404))
    }

    @PutMapping("/me")
    @Operation(summary = "更新开发者信息", description = "更新当前开发者的信息（需要登录）")
    fun updateDeveloper(
        @Valid @RequestBody request: UpdateDeveloperRequest
    ): Mono<ApiResponse<Developer>> {
        return SecurityUtils.getCurrentUserId()
            .flatMap { userId ->
                developerService.updateDeveloper(userId, request)
            }
            .map { ApiResponse.success(it, "更新成功") }
            .switchIfEmpty(Mono.just(ApiResponse.error("开发者不存在", 404)))
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "更新失败", -1))
            }
    }
}
