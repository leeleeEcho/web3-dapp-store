package com.web3store.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.web3store.data.repository.AppRepository
import com.web3store.domain.model.AppListItem
import com.web3store.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 首页 UI 状态
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredApps: List<AppListItem> = emptyList(),
    val topDownloads: List<AppListItem> = emptyList(),
    val latestApps: List<AppListItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val error: String? = null
)

/**
 * 首页 ViewModel
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    /**
     * 加载首页数据
     */
    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 并行加载所有数据
            val featuredResult = appRepository.getFeaturedApps()
            val topDownloadsResult = appRepository.getTopDownloads(10)
            val latestResult = appRepository.getLatestApps(10)
            val categoriesResult = appRepository.getCategories()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    featuredApps = featuredResult.getOrDefault(emptyList()),
                    topDownloads = topDownloadsResult.getOrDefault(emptyList()),
                    latestApps = latestResult.getOrDefault(emptyList()),
                    categories = categoriesResult.getOrDefault(emptyList()),
                    error = listOfNotNull(
                        featuredResult.exceptionOrNull()?.message,
                        topDownloadsResult.exceptionOrNull()?.message,
                        latestResult.exceptionOrNull()?.message,
                        categoriesResult.exceptionOrNull()?.message
                    ).firstOrNull()
                )
            }
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadHomeData()
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
