package com.di.dappstore.controller

import com.di.dappstore.model.dto.CreateReviewRequest
import com.di.dappstore.model.vo.ApiResponse
import com.di.dappstore.model.vo.PageResponse
import com.di.dappstore.model.vo.ReviewVo
import com.di.dappstore.security.SecurityUtils
import com.di.dappstore.service.ReviewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Reviews", description = "应用评论相关接口")
class ReviewController(
    private val reviewService: ReviewService
) {

    @GetMapping("/apps/{appId}/reviews")
    @Operation(summary = "获取应用评论", description = "分页获取应用的评论列表")
    fun getReviews(
        @Parameter(description = "应用ID") @PathVariable appId: Long,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") size: Int
    ): Mono<ApiResponse<PageResponse<ReviewVo>>> {
        return reviewService.getReviewsByAppId(appId, page, size)
            .map { ApiResponse.success(it) }
    }

    @PostMapping("/apps/{appId}/reviews")
    @Operation(summary = "提交评论", description = "为应用提交评论（需要登录）")
    fun createReview(
        @Parameter(description = "应用ID") @PathVariable appId: Long,
        @Valid @RequestBody request: CreateReviewRequest
    ): Mono<ApiResponse<Unit>> {
        return SecurityUtils.getCurrentUserId()
            .flatMap { userId ->
                reviewService.createReview(appId, userId, request)
            }
            .map { ApiResponse.success(Unit, "评论已提交") }
            .onErrorResume { e ->
                Mono.just(ApiResponse.error(e.message ?: "评论提交失败", -1))
            }
    }

    @PostMapping("/reviews/{reviewId}/helpful")
    @Operation(summary = "标记评论有帮助", description = "标记一条评论为有帮助")
    fun markAsHelpful(
        @Parameter(description = "评论ID") @PathVariable reviewId: Long
    ): Mono<ApiResponse<Unit>> {
        return reviewService.markAsHelpful(reviewId)
            .thenReturn(ApiResponse.success(Unit, "已标记"))
    }
}
