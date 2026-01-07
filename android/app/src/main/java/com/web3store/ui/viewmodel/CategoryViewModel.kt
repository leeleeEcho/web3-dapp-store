package com.web3store.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.web3store.data.repository.AppRepository
import com.web3store.domain.model.AppListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Category UI state
 */
data class CategoryUiState(
    val categoryName: String = "",
    val apps: List<AppListItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val currentPage: Int = 0
)

/**
 * ViewModel for category browsing
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val appRepository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: Long = savedStateHandle["categoryId"] ?: 0L
    private val categoryName: String = savedStateHandle["categoryName"] ?: ""

    private val _uiState = MutableStateFlow(CategoryUiState(categoryName = categoryName))
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private val pageSize = 20

    init {
        loadApps()
    }

    /**
     * Load apps for the category
     */
    fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            appRepository.getApps(
                page = 0,
                size = pageSize,
                categoryId = categoryId
            ).fold(
                onSuccess = { apps ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            apps = apps,
                            currentPage = 0,
                            hasMore = apps.size >= pageSize,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "加载失败"
                        )
                    }
                }
            )
        }
    }

    /**
     * Load more apps (pagination)
     */
    fun loadMore() {
        val currentState = _uiState.value
        if (currentState.isLoadingMore || !currentState.hasMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val nextPage = currentState.currentPage + 1
            appRepository.getApps(
                page = nextPage,
                size = pageSize,
                categoryId = categoryId
            ).fold(
                onSuccess = { apps ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            apps = it.apps + apps,
                            currentPage = nextPage,
                            hasMore = apps.size >= pageSize
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            error = error.message ?: "加载更多失败"
                        )
                    }
                }
            )
        }
    }

    /**
     * Refresh apps list
     */
    fun refresh() {
        loadApps()
    }
}
