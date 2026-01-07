package com.web3store.data.repository

import com.google.gson.Gson
import com.web3store.data.local.TokenManager
import com.web3store.data.remote.api.DAppStoreApi
import com.web3store.data.remote.dto.GoogleLoginRequest
import com.web3store.data.remote.dto.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证状态
 */
sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: UserDto) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * 认证仓库
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: DAppStoreApi,
    private val tokenManager: TokenManager,
    private val gson: Gson
) {

    /**
     * 认证状态 Flow
     */
    val authState: Flow<AuthState> = tokenManager.tokenFlow.map { token ->
        if (token != null) {
            val userJson = tokenManager.getUserJson()
            if (userJson != null) {
                try {
                    val user = gson.fromJson(userJson, UserDto::class.java)
                    AuthState.Authenticated(user)
                } catch (e: Exception) {
                    AuthState.Unauthenticated
                }
            } else {
                AuthState.Unauthenticated
            }
        } else {
            AuthState.Unauthenticated
        }
    }

    /**
     * 是否已登录
     */
    val isLoggedIn: Flow<Boolean> = tokenManager.isLoggedInFlow

    /**
     * Google 登录
     */
    suspend fun loginWithGoogle(idToken: String): Result<UserDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.loginWithGoogle(GoogleLoginRequest(idToken))
            if (response.success && response.data != null) {
                val authResponse = response.data
                val userJson = gson.toJson(authResponse.user)
                tokenManager.saveAuth(
                    token = authResponse.token,
                    expiresIn = authResponse.expiresIn,
                    userJson = userJson
                )
                Result.success(authResponse.user)
            } else {
                Result.failure(Exception(response.message ?: "Google 登录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取当前用户信息
     */
    suspend fun getCurrentUser(): Result<UserDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCurrentUser()
            if (response.success && response.data != null) {
                // 更新本地缓存的用户信息
                val userJson = gson.toJson(response.data)
                val token = tokenManager.getToken()
                if (token != null) {
                    // 保持原有 token，只更新用户信息
                    tokenManager.saveAuth(
                        token = token,
                        expiresIn = 86400, // 默认 24 小时
                        userJson = userJson
                    )
                }
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "获取用户信息失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 登出
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 调用后端登出 API（可选，主要是清除本地 token）
            try {
                api.logout()
            } catch (e: Exception) {
                // 忽略网络错误，仍然清除本地 token
            }
            tokenManager.clearAuth()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 检查是否已登录
     */
    suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    /**
     * 获取缓存的用户信息
     */
    suspend fun getCachedUser(): UserDto? {
        val userJson = tokenManager.getUserJson() ?: return null
        return try {
            gson.fromJson(userJson, UserDto::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
