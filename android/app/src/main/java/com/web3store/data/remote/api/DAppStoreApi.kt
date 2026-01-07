package com.web3store.data.remote.api

import com.web3store.data.remote.dto.*
import retrofit2.http.*

/**
 * DApp Store API 接口定义
 */
interface DAppStoreApi {

    // ==================== Apps ====================

    /**
     * 获取应用列表
     */
    @GET("api/v1/apps")
    suspend fun getApps(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("categoryId") categoryId: Long? = null
    ): ApiResponse<PageResponse<AppListItemDto>>

    /**
     * 获取应用详情
     */
    @GET("api/v1/apps/{id}")
    suspend fun getAppById(@Path("id") id: Long): ApiResponse<AppDetailDto>

    /**
     * 根据包名获取应用
     */
    @GET("api/v1/apps/package/{packageName}")
    suspend fun getAppByPackageName(@Path("packageName") packageName: String): ApiResponse<AppDetailDto>

    /**
     * 搜索应用
     */
    @GET("api/v1/apps/search")
    suspend fun searchApps(@Query("keyword") keyword: String): ApiResponse<List<AppListItemDto>>

    /**
     * 获取推荐应用
     */
    @GET("api/v1/apps/featured")
    suspend fun getFeaturedApps(): ApiResponse<List<AppListItemDto>>

    /**
     * 获取热门下载
     */
    @GET("api/v1/apps/top-downloads")
    suspend fun getTopDownloads(@Query("limit") limit: Int = 20): ApiResponse<List<AppListItemDto>>

    /**
     * 获取高分应用
     */
    @GET("api/v1/apps/top-rated")
    suspend fun getTopRated(@Query("limit") limit: Int = 20): ApiResponse<List<AppListItemDto>>

    /**
     * 获取最新应用
     */
    @GET("api/v1/apps/latest")
    suspend fun getLatestApps(@Query("limit") limit: Int = 20): ApiResponse<List<AppListItemDto>>

    /**
     * 记录下载
     */
    @POST("api/v1/apps/{id}/download")
    suspend fun recordDownload(@Path("id") id: Long): ApiResponse<Unit>

    // ==================== Categories ====================

    /**
     * 获取所有分类
     */
    @GET("api/v1/categories")
    suspend fun getCategories(): ApiResponse<List<CategoryDto>>

    // ==================== Auth ====================

    /**
     * 获取登录 Nonce
     */
    @POST("api/v1/auth/nonce")
    suspend fun getNonce(@Body request: WalletNonceRequest): ApiResponse<NonceResponseDto>

    /**
     * 钱包登录
     */
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: WalletLoginRequest): ApiResponse<AuthResponseDto>

    /**
     * Google 登录
     */
    @POST("api/v1/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): ApiResponse<AuthResponseDto>

    /**
     * 获取当前用户信息
     */
    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): ApiResponse<UserDto>

    /**
     * 登出
     */
    @POST("api/v1/auth/logout")
    suspend fun logout(): ApiResponse<Unit>

    // ==================== Health ====================

    /**
     * 健康检查
     */
    @GET("api/v1/health")
    suspend fun healthCheck(): Map<String, Any>
}
