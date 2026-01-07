package com.di.dappstore.controller

import com.di.dappstore.model.dto.*
import com.di.dappstore.model.entity.AppStatus
import com.di.dappstore.model.vo.ApiResponse
import com.di.dappstore.model.vo.PageResponse
import com.di.dappstore.security.SecurityUtils
import com.di.dappstore.service.AdminAppService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/admin/apps")
@Tag(name = "Admin Apps", description = "管理员应用审核接口")
class AdminAppController(
    private val adminAppService: AdminAppService
) {

    @GetMapping("/pending")
    @Operation(summary = "获取待审核应用列表", description = "获取所有待审核的应用")
    fun getPendingApps(
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") size: Int
    ): Mono<ApiResponse<PageResponse<PendingAppItem>>> {
        return requireAdmin()
            .flatMap {
                adminAppService.getPendingApps(page, size)
            }
            .map { ApiResponse.success(it) }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "获取失败", -1))
            }
    }

    @GetMapping
    @Operation(summary = "获取所有应用列表", description = "管理员查看所有应用")
    fun getAllApps(
        @Parameter(description = "应用状态过滤") @RequestParam(required = false) status: AppStatus?,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") size: Int
    ): Mono<ApiResponse<PageResponse<PendingAppItem>>> {
        return requireAdmin()
            .flatMap {
                adminAppService.getAllApps(status, page, size)
            }
            .map { ApiResponse.success(it) }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "获取失败", -1))
            }
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取审核统计", description = "获取应用审核统计信息")
    fun getStatistics(): Mono<ApiResponse<ReviewStatistics>> {
        return requireAdmin()
            .flatMap {
                adminAppService.getReviewStatistics()
            }
            .map { ApiResponse.success(it) }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "获取失败", -1))
            }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取应用详情", description = "管理员查看应用详情")
    fun getAppDetail(
        @Parameter(description = "应用ID") @PathVariable id: Long
    ): Mono<ApiResponse<AdminAppDetail>> {
        return requireAdmin()
            .flatMap {
                adminAppService.getAppDetail(id)
            }
            .map { ApiResponse.success(it) }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "获取失败", -1))
            }
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "审核应用", description = "通过或拒绝应用")
    fun reviewApp(
        @Parameter(description = "应用ID") @PathVariable id: Long,
        @Valid @RequestBody request: ReviewAppRequest
    ): Mono<ApiResponse<AdminAppDetail>> {
        return requireAdmin()
            .flatMap { userId ->
                adminAppService.reviewApp(id, userId, request)
            }
            .map {
                val message = if (request.approved) "应用已通过审核" else "应用已拒绝"
                ApiResponse.success(it, message)
            }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "审核失败", -1))
            }
    }

    @PostMapping("/batch-review")
    @Operation(summary = "批量审核应用", description = "批量通过或拒绝多个应用")
    fun batchReviewApps(
        @Valid @RequestBody request: BatchReviewRequest
    ): Mono<ApiResponse<BatchReviewResult>> {
        return requireAdmin()
            .flatMap { userId ->
                adminAppService.batchReviewApps(userId, request)
            }
            .map { ApiResponse.success(it, "批量审核完成") }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "批量审核失败", -1))
            }
    }

    @PostMapping("/{id}/featured")
    @Operation(summary = "设置精选", description = "设置或取消应用精选状态")
    fun setFeatured(
        @Parameter(description = "应用ID") @PathVariable id: Long,
        @Valid @RequestBody request: SetFeaturedRequest
    ): Mono<ApiResponse<AdminAppDetail>> {
        return requireAdmin()
            .flatMap {
                adminAppService.setFeatured(id, request)
            }
            .map {
                val message = if (request.featured) "已设置为精选" else "已取消精选"
                ApiResponse.success(it, message)
            }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "操作失败", -1))
            }
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "暂停/恢复应用", description = "暂停或恢复应用上架状态")
    fun suspendApp(
        @Parameter(description = "应用ID") @PathVariable id: Long,
        @Valid @RequestBody request: SuspendAppRequest
    ): Mono<ApiResponse<AdminAppDetail>> {
        return requireAdmin()
            .flatMap { userId ->
                adminAppService.suspendApp(id, userId, request)
            }
            .map {
                val message = if (request.suspended) "应用已暂停" else "应用已恢复"
                ApiResponse.success(it, message)
            }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "操作失败", -1))
            }
    }

    /**
     * 验证当前用户是否为管理员
     */
    private fun requireAdmin(): Mono<Long> {
        return SecurityUtils.getCurrentUser()
            .flatMap { user ->
                if (user.role == "ADMIN") {
                    Mono.just(user.userId)
                } else {
                    Mono.error(RuntimeException("无权限，仅管理员可访问"))
                }
            }
    }
}
