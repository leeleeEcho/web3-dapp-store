package com.di.dappstore.controller

import com.di.dappstore.model.vo.ApiResponse
import com.di.dappstore.model.vo.CategorySummary
import com.di.dappstore.service.CategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "应用分类相关接口")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    @Operation(summary = "获取所有分类", description = "获取所有可用的应用分类")
    fun getAllCategories(): Mono<ApiResponse<List<CategorySummary>>> {
        return categoryService.getAllCategories()
            .collectList()
            .map { ApiResponse.success(it) }
    }
}
