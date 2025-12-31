package com.web3store.data.remote.dto

/**
 * API 统一响应格式
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val code: Int
)

/**
 * 分页响应
 */
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)
