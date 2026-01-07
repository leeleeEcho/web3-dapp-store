package com.web3store.ui.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.web3store.domain.model.AppListItem
import com.web3store.ui.theme.DIColors
import com.web3store.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = modifier.background(DIColors.Background),
        containerColor = DIColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DIColors.Card)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = DIColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        textStyle = TextStyle(
                            color = DIColors.TextPrimary,
                            fontSize = 16.sp
                        ),
                        cursorBrush = SolidColor(DIColors.Primary),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search dApps, categories...",
                                        color = DIColors.TextSecondary,
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(
                            onClick = { viewModel.clearSearch() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = DIColors.TextSecondary
                            )
                        }
                    }
                }
            }

            // Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!uiState.hasSearched) {
                    // Recent Searches
                    if (uiState.recentSearches.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Searches",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DIColors.TextPrimary
                                )
                                TextButton(onClick = { viewModel.clearSearchHistory() }) {
                                    Text(
                                        text = "Clear",
                                        color = DIColors.Primary
                                    )
                                }
                            }
                        }

                        items(uiState.recentSearches) { search ->
                            RecentSearchItem(
                                query = search,
                                onClick = { viewModel.searchByKeyword(search) },
                                onRemove = { viewModel.removeFromHistory(search) }
                            )
                        }
                    }

                    // Trending Searches
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Trending Searches",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DIColors.TextPrimary
                        )
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.trendingSearches) { trend ->
                                TrendingChip(
                                    text = trend,
                                    onClick = { viewModel.searchByKeyword(trend) }
                                )
                            }
                        }
                    }

                    // Popular Categories
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Popular Categories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DIColors.TextPrimary
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "DeFi" to "234 apps",
                                "NFT" to "189 apps",
                                "GameFi" to "156 apps",
                                "DEX" to "98 apps",
                                "Lending" to "67 apps"
                            ).forEach { (category, count) ->
                                CategorySearchItem(
                                    category = category,
                                    count = count,
                                    onClick = { viewModel.searchByKeyword(category) }
                                )
                            }
                        }
                    }
                } else {
                    // Loading indicator
                    if (uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = DIColors.Primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Error message
                    uiState.error?.let { error ->
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Search failed",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DIColors.Error
                                    )
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = DIColors.TextSecondary
                                    )
                                    TextButton(onClick = { viewModel.performSearch(searchQuery) }) {
                                        Text("Retry", color = DIColors.Primary)
                                    }
                                }
                            }
                        }
                    }

                    // Search Results
                    if (!uiState.isLoading && uiState.error == null) {
                        item {
                            Text(
                                text = "${uiState.searchResults.size} results for \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DIColors.TextSecondary
                            )
                        }

                        items(uiState.searchResults) { app ->
                            SearchResultCard(
                                app = app,
                                onClick = { onAppClick(app.id.toString()) }
                            )
                        }

                        if (uiState.searchResults.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = DIColors.TextSecondary,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "No results found",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = DIColors.TextPrimary
                                        )
                                        Text(
                                            text = "Try searching for something else",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = DIColors.TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    app: AppListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DIColors.Card)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App icon
        AsyncImage(
            model = app.iconUrl,
            contentDescription = app.name,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DIColors.CardElevated)
        )

        // App info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = DIColors.TextPrimary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                app.categoryName?.let { category ->
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodySmall,
                        color = DIColors.Primary
                    )
                }
                Text(
                    text = "★ ${String.format("%.1f", app.ratingAverage)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DIColors.TextSecondary
                )
                Text(
                    text = app.formattedDownloads,
                    style = MaterialTheme.typography.bodySmall,
                    color = DIColors.TextSecondary
                )
            }
            // Blockchain tags
            if (app.chains.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    app.chains.take(3).forEach { chain ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DIColors.CardElevated
                        ) {
                            Text(
                                text = chain,
                                style = MaterialTheme.typography.labelSmall,
                                color = DIColors.TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentSearchItem(
    query: String,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = DIColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = query,
            style = MaterialTheme.typography.bodyLarge,
            color = DIColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = DIColors.TextSecondary
            )
        }
    }
}

@Composable
private fun TrendingChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = DIColors.Card
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = DIColors.Primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = DIColors.TextPrimary
            )
        }
    }
}

@Composable
private fun CategorySearchItem(
    category: String,
    count: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DIColors.Card)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = DIColors.TextPrimary
        )
        Text(
            text = count,
            style = MaterialTheme.typography.bodyMedium,
            color = DIColors.TextSecondary
        )
    }
}
