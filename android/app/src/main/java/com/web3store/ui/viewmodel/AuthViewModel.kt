package com.web3store.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.web3store.data.remote.dto.UserDto
import com.web3store.data.repository.AuthRepository
import com.web3store.data.repository.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 登录事件
 */
sealed class LoginEvent {
    object Success : LoginEvent()
    data class Error(val message: String) : LoginEvent()
}

/**
 * 认证 ViewModel
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent: SharedFlow<LoginEvent> = _loginEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeAuthState()
    }

    /**
     * 监听认证状态变化
     */
    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { state ->
                _authState.value = state
            }
        }
    }

    /**
     * 使用 Google 登录
     */
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading

            authRepository.loginWithGoogle(idToken)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                    _loginEvent.emit(LoginEvent.Success)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "登录失败")
                    _loginEvent.emit(LoginEvent.Error(error.message ?: "登录失败"))
                }

            _isLoading.value = false
        }
    }

    /**
     * 登出
     */
    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.logout()
            _authState.value = AuthState.Unauthenticated
            _isLoading.value = false
        }
    }

    /**
     * 刷新用户信息
     */
    fun refreshUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { error ->
                    // 如果获取失败，可能 token 已过期
                    if (error.message?.contains("401") == true ||
                        error.message?.contains("Unauthorized") == true) {
                        logout()
                    }
                }
        }
    }

    /**
     * 获取当前用户
     */
    fun getCurrentUser(): UserDto? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.user
            else -> null
        }
    }

    /**
     * 是否已登录
     */
    fun isLoggedIn(): Boolean {
        return _authState.value is AuthState.Authenticated
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}
