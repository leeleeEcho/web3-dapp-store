package com.web3store.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.web3store.data.repository.AppRepository
import com.web3store.domain.model.AppListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Search UI state
 */
data class SearchUiState(
    val isLoading: Boolean = false,
    val searchResults: List<AppListItem> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val trendingSearches: List<String> = listOf("DeFi", "NFT", "DEX", "GameFi", "Wallet", "DAO"),
    val error: String? = null,
    val hasSearched: Boolean = false
)

/**
 * ViewModel for search functionality
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 本地搜索历史 (简单实现，后续可换成 Room)
    private val _recentSearches = mutableListOf<String>()

    init {
        // 防抖搜索
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collectLatest { query ->
                    performSearch(query)
                }
        }

        // 初始化搜索历史
        loadRecentSearches()
    }

    /**
     * Update search query
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query

        // 如果清空了搜索词，重置状态
        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    searchResults = emptyList(),
                    hasSearched = false,
                    error = null
                )
            }
        }
    }

    /**
     * Perform search with the given query
     */
    fun performSearch(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, hasSearched = true) }

            appRepository.searchApps(query).fold(
                onSuccess = { results ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            searchResults = results,
                            error = null
                        )
                    }
                    // 保存到搜索历史
                    addToRecentSearches(query)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            searchResults = emptyList(),
                            error = error.message ?: "Search failed"
                        )
                    }
                }
            )
        }
    }

    /**
     * Search by clicking on a suggestion/history item
     */
    fun searchByKeyword(keyword: String) {
        _searchQuery.value = keyword
        performSearch(keyword)
    }

    /**
     * Clear search
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _uiState.update {
            it.copy(
                searchResults = emptyList(),
                hasSearched = false,
                error = null
            )
        }
    }

    /**
     * Clear search history
     */
    fun clearSearchHistory() {
        _recentSearches.clear()
        _uiState.update { it.copy(recentSearches = emptyList()) }
    }

    /**
     * Remove a single item from search history
     */
    fun removeFromHistory(query: String) {
        _recentSearches.remove(query)
        _uiState.update { it.copy(recentSearches = _recentSearches.toList()) }
    }

    private fun addToRecentSearches(query: String) {
        // 移除已存在的相同搜索词
        _recentSearches.remove(query)
        // 添加到开头
        _recentSearches.add(0, query)
        // 限制最多 10 条
        while (_recentSearches.size > 10) {
            _recentSearches.removeAt(_recentSearches.lastIndex)
        }
        // 更新 UI
        _uiState.update { it.copy(recentSearches = _recentSearches.toList()) }
    }

    private fun loadRecentSearches() {
        // 简单实现：使用内存存储
        // TODO: 后续可以使用 Room 或 DataStore 持久化
        _uiState.update { it.copy(recentSearches = _recentSearches.toList()) }
    }
}
