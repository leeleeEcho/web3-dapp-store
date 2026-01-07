package com.di.dappstore.controller

import com.di.dappstore.model.dto.*
import com.di.dappstore.model.entity.AppStatus
import com.di.dappstore.model.vo.ApiResponse
import com.di.dappstore.model.vo.PageResponse
import com.di.dappstore.security.SecurityUtils
import com.di.dappstore.service.AppSubmissionService
import com.di.dappstore.service.DeveloperService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/developer/apps")
@Tag(name = "Developer Apps", description = "开发者应用管理接口")
class DeveloperAppController(
    private val appSubmissionService: AppSubmissionService,
    private val developerService: DeveloperService
) {

    @PostMapping
    @Operation(summary = "提交新应用", description = "开发者提交新应用")
    fun submitApp(
        @Valid @RequestBody request: SubmitAppRequest
    ): Mono<ApiResponse<DeveloperAppDetail>> {
        return getDeveloperId()
            .flatMap { developerId ->
                appSubmissionService.submitApp(developerId, request)
            }
            .map { ApiResponse.success(it, "应用提交成功") }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "提交失败", -1))
            }
    }

    @GetMapping
    @Operation(summary = "获取我的应用列表", description = "获取当前开发者的所有应用")
    fun getMyApps(
        @Parameter(description = "应用状态过滤") @RequestParam(required = false) status: AppStatus?,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") size: Int
    ): Mono<ApiResponse<PageResponse<DeveloperAppItem>>> {
        return getDeveloperId()
            .flatMap { developerId ->
                appSubmissionService.getDeveloperApps(developerId, status, page, size)
            }
            .map { ApiResponse.success(it) }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "获取失败", -1))
            }
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取应用统计", description = "获取开发者的应用统计信息")
    fun getStatistics(): Mono<ApiResponse<AppStatistics>> {
        return getDeveloperId()
            .flatMap { developerId ->
                appSubmissionService.getAppStatistics(developerId)
            }
            .map { ApiResponse.success(it) }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "获取失败", -1))
            }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取应用详情", description = "获取指定应用的详细信息")
    fun getAppDetail(
        @Parameter(description = "应用ID") @PathVariable id: Long
    ): Mono<ApiResponse<DeveloperAppDetail>> {
        return getDeveloperId()
            .flatMap { developerId ->
                appSubmissionService.getDeveloperAppDetail(developerId, id)
            }
            .map { ApiResponse.success(it) }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "获取失败", -1))
            }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新应用信息", description = "更新草稿或被拒绝的应用信息")
    fun updateApp(
        @Parameter(description = "应用ID") @PathVariable id: Long,
        @Valid @RequestBody request: UpdateAppSubmissionRequest
    ): Mono<ApiResponse<DeveloperAppDetail>> {
        return getDeveloperId()
            .flatMap { developerId ->
                appSubmissionService.updateApp(developerId, id, request)
            }
            .map { ApiResponse.success(it, "更新成功") }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "更新失败", -1))
            }
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交审核", description = "将草稿或被拒绝的应用提交审核")
    fun submitForReview(
        @Parameter(description = "应用ID") @PathVariable id: Long
    ): Mono<ApiResponse<DeveloperAppDetail>> {
        return getDeveloperId()
            .flatMap { developerId ->
                appSubmissionService.submitForReview(developerId, id)
            }
            .map { ApiResponse.success(it, "已提交审核") }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "提交失败", -1))
            }
    }

    @PostMapping("/{id}/version")
    @Operation(summary = "发布新版本", description = "为已上架的应用发布新版本")
    fun publishNewVersion(
        @Parameter(description = "应用ID") @PathVariable id: Long,
        @Valid @RequestBody request: PublishVersionRequest
    ): Mono<ApiResponse<DeveloperAppDetail>> {
        return getDeveloperId()
            .flatMap { developerId ->
                appSubmissionService.publishNewVersion(developerId, id, request)
            }
            .map { ApiResponse.success(it, "新版本已提交") }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "发布失败", -1))
            }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除应用", description = "删除草稿状态的应用")
    fun deleteApp(
        @Parameter(description = "应用ID") @PathVariable id: Long
    ): Mono<ApiResponse<String>> {
        return getDeveloperId()
            .flatMap { developerId ->
                appSubmissionService.deleteApp(developerId, id)
            }
            .then(Mono.just(ApiResponse.success("deleted", "删除成功")))
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "删除失败", -1))
            }
    }

    /**
     * 获取当前用户的开发者ID
     */
    private fun getDeveloperId(): Mono<Long> {
        return SecurityUtils.getCurrentUserId()
            .flatMap { userId ->
                developerService.getDeveloperByUserId(userId)
                    .switchIfEmpty(Mono.error(RuntimeException("您还不是开发者，请先注册")))
                    .map { it.id!! }
            }
    }
}
