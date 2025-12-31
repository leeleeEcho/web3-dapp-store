package com.web3store.data.repository

import com.web3store.data.remote.api.DAppStoreApi
import com.web3store.data.remote.mapper.AppMapper.toDomain
import com.web3store.domain.model.AppDetail
import com.web3store.domain.model.AppListItem
import com.web3store.domain.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App 数据仓库
 */
@Singleton
class AppRepository @Inject constructor(
    private val api: DAppStoreApi
) {

    /**
     * 获取应用列表
     */
    suspend fun getApps(
        page: Int = 0,
        size: Int = 20,
        categoryId: Long? = null
    ): Result<List<AppListItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getApps(page, size, categoryId)
            if (response.success && response.data != null) {
                Result.success(response.data.content.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message ?: "获取应用列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取应用详情
     */
    suspend fun getAppById(id: Long): Result<AppDetail> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAppById(id)
            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.message ?: "获取应用详情失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 根据包名获取应用
     */
    suspend fun getAppByPackageName(packageName: String): Result<AppDetail> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAppByPackageName(packageName)
            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(Exception(response.message ?: "获取应用失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 搜索应用
     */
    suspend fun searchApps(keyword: String): Result<List<AppListItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchApps(keyword)
            if (response.success && response.data != null) {
                Result.success(response.data.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message ?: "搜索失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取推荐应用
     */
    suspend fun getFeaturedApps(): Result<List<AppListItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFeaturedApps()
            if (response.success && response.data != null) {
                Result.success(response.data.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message ?: "获取推荐应用失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取热门下载
     */
    suspend fun getTopDownloads(limit: Int = 20): Result<List<AppListItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTopDownloads(limit)
            if (response.success && response.data != null) {
                Result.success(response.data.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message ?: "获取热门下载失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取高分应用
     */
    suspend fun getTopRated(limit: Int = 20): Result<List<AppListItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTopRated(limit)
            if (response.success && response.data != null) {
                Result.success(response.data.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message ?: "获取高分应用失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取最新应用
     */
    suspend fun getLatestApps(limit: Int = 20): Result<List<AppListItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLatestApps(limit)
            if (response.success && response.data != null) {
                Result.success(response.data.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message ?: "获取最新应用失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 记录下载
     */
    suspend fun recordDownload(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.recordDownload(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "记录下载失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取所有分类
     */
    suspend fun getCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCategories()
            if (response.success && response.data != null) {
                Result.success(response.data.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message ?: "获取分类失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
