package com.web3store.data.remote

import com.web3store.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp 拦截器，自动添加 Authorization 头
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val TOKEN_TYPE = "Bearer"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 如果请求已经有 Authorization 头，则不添加
        if (originalRequest.header(HEADER_AUTHORIZATION) != null) {
            return chain.proceed(originalRequest)
        }

        // 从 TokenManager 获取 token
        val token = runBlocking { tokenManager.getToken() }

        val request = if (token != null) {
            originalRequest.newBuilder()
                .header(HEADER_AUTHORIZATION, "$TOKEN_TYPE $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
