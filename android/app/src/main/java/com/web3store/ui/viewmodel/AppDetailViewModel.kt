package com.web3store.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.web3store.data.repository.AppRepository
import com.web3store.domain.model.AppDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 应用详情 UI 状态
 */
data class AppDetailUiState(
    val isLoading: Boolean = true,
    val appDetail: AppDetail? = null,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val error: String? = null
)

/**
 * 应用详情 ViewModel
 */
@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val appRepository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 从导航参数获取 appId (支持 String 和 Long 类型)
    private val appId: Long = savedStateHandle.get<String>("appId")?.toLongOrNull()
        ?: savedStateHandle.get<Long>("appId")
        ?: 0L

    private val _uiState = MutableStateFlow(AppDetailUiState())
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    init {
        if (appId > 0) {
            loadAppDetail(appId)
        }
    }

    /**
     * 通过字符串 ID 加载应用详情
     */
    fun loadAppDetailByStringId(id: String) {
        id.toLongOrNull()?.let { loadAppDetail(it) }
    }

    /**
     * 加载应用详情
     */
    fun loadAppDetail(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            appRepository.getAppById(id).fold(
                onSuccess = { appDetail ->
                    _uiState.update { it.copy(isLoading = false, appDetail = appDetail) }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "加载失败")
                    }
                }
            )
        }
    }

    /**
     * 开始下载
     */
    fun startDownload() {
        val appDetail = _uiState.value.appDetail ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true, downloadProgress = 0f) }

            // 记录下载
            appRepository.recordDownload(appDetail.id)

            // TODO: 实际下载逻辑
            // 这里模拟下载进度
            for (i in 1..100) {
                kotlinx.coroutines.delay(50)
                _uiState.update { it.copy(downloadProgress = i / 100f) }
            }

            _uiState.update { it.copy(isDownloading = false, downloadProgress = 1f) }
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
