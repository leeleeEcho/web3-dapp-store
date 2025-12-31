package com.di.dappstore.controller

import com.di.dappstore.model.vo.ApiResponse
import com.di.dappstore.model.vo.AppDetail
import com.di.dappstore.model.vo.AppListItem
import com.di.dappstore.model.vo.PageResponse
import com.di.dappstore.service.AppService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/apps")
@Tag(name = "Apps", description = "DApp 应用相关接口")
class AppController(
    private val appService: AppService
) {

    @GetMapping
    @Operation(summary = "获取应用列表", description = "分页获取已上架的应用列表")
    fun getApps(
        @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "分类ID") @RequestParam(required = false) categoryId: Long?
    ): Mono<ApiResponse<PageResponse<AppListItem>>> {
        return appService.getApps(page, size, categoryId)
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取应用详情", description = "根据ID获取应用详情")
    fun getAppById(
        @Parameter(description = "应用ID") @PathVariable id: Long
    ): Mono<ApiResponse<AppDetail>> {
        return appService.getAppById(id)
            .map { ApiResponse.success(it) }
            .defaultIfEmpty(ApiResponse.error("应用不存在", 404))
    }

    @GetMapping("/package/{packageName}")
    @Operation(summary = "根据包名获取应用", description = "根据包名获取应用详情")
    fun getAppByPackageName(
        @Parameter(description = "应用包名") @PathVariable packageName: String
    ): Mono<ApiResponse<AppDetail>> {
        return appService.getAppByPackageName(packageName)
            .map { ApiResponse.success(it) }
            .defaultIfEmpty(ApiResponse.error("应用不存在", 404))
    }

    @GetMapping("/search")
    @Operation(summary = "搜索应用", description = "根据关键词搜索应用")
    fun searchApps(
        @Parameter(description = "搜索关键词") @RequestParam keyword: String
    ): Mono<ApiResponse<List<AppListItem>>> {
        return appService.searchApps(keyword)
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/featured")
    @Operation(summary = "获取推荐应用", description = "获取首页推荐的应用列表")
    fun getFeaturedApps(): Mono<ApiResponse<List<AppListItem>>> {
        return appService.getFeaturedApps()
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/top-downloads")
    @Operation(summary = "获取热门下载", description = "获取下载量最高的应用")
    fun getTopDownloaded(
        @Parameter(description = "返回数量") @RequestParam(defaultValue = "20") limit: Int
    ): Mono<ApiResponse<List<AppListItem>>> {
        return appService.getTopDownloaded(limit)
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/top-rated")
    @Operation(summary = "获取高分应用", description = "获取评分最高的应用")
    fun getTopRated(
        @Parameter(description = "返回数量") @RequestParam(defaultValue = "20") limit: Int
    ): Mono<ApiResponse<List<AppListItem>>> {
        return appService.getTopRated(limit)
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/latest")
    @Operation(summary = "获取最新应用", description = "获取最新上架的应用")
    fun getLatestApps(
        @Parameter(description = "返回数量") @RequestParam(defaultValue = "20") limit: Int
    ): Mono<ApiResponse<List<AppListItem>>> {
        return appService.getLatestApps(limit)
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @PostMapping("/{id}/download")
    @Operation(summary = "记录下载", description = "记录应用下载，增加下载计数")
    fun recordDownload(
        @Parameter(description = "应用ID") @PathVariable id: Long
    ): Mono<ApiResponse<Unit>> {
        return appService.incrementDownloadCount(id)
            .thenReturn(ApiResponse.success(Unit, "下载已记录"))
    }
}
