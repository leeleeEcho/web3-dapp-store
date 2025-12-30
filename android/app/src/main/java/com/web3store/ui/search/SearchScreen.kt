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
import com.web3store.ui.components.AppListCard
import com.web3store.ui.theme.DIColors

data class SearchResult(
    val id: String,
    val name: String,
    val category: String,
    val rating: Float,
    val iconUrl: String,
    val chains: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val recentSearches = remember {
        mutableStateListOf("Uniswap", "OpenSea", "Aave", "MetaMask")
    }

    val trendingSearches = remember {
        listOf("DeFi", "NFT Marketplace", "DEX", "Lending", "GameFi", "DAO")
    }

    val searchResults = remember(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            listOf(
                SearchResult("1", "Uniswap", "DeFi", 4.8f, "https://via.placeholder.com/56", listOf("ETH", "ARB", "OP")),
                SearchResult("2", "SushiSwap", "DeFi", 4.5f, "https://via.placeholder.com/56", listOf("ETH", "MATIC")),
                SearchResult("3", "PancakeSwap", "DeFi", 4.6f, "https://via.placeholder.com/56", listOf("BSC")),
                SearchResult("4", "1inch", "DeFi", 4.4f, "https://via.placeholder.com/56", listOf("ETH", "BSC", "MATIC"))
            ).filter { it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
        } else {
            emptyList()
        }
    }

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
                        onValueChange = {
                            searchQuery = it
                            isSearching = it.isNotEmpty()
                        },
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
                            onClick = { searchQuery = "" },
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
                if (searchQuery.isEmpty()) {
                    // Recent Searches
                    if (recentSearches.isNotEmpty()) {
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
                                TextButton(onClick = { recentSearches.clear() }) {
                                    Text(
                                        text = "Clear",
                                        color = DIColors.Primary
                                    )
                                }
                            }
                        }

                        items(recentSearches) { search ->
                            RecentSearchItem(
                                query = search,
                                onClick = { searchQuery = search },
                                onRemove = { recentSearches.remove(search) }
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
                            items(trendingSearches) { trend ->
                                TrendingChip(
                                    text = trend,
                                    onClick = { searchQuery = trend }
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
                                    onClick = { searchQuery = category }
                                )
                            }
                        }
                    }
                } else {
                    // Search Results
                    item {
                        Text(
                            text = "${searchResults.size} results for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DIColors.TextSecondary
                        )
                    }

                    items(searchResults) { result ->
                        AppListCard(
                            name = result.name,
                            iconUrl = result.iconUrl,
                            rating = result.rating,
                            downloads = "1M+",
                            chains = result.chains,
                            onClick = { onAppClick(result.id) },
                            onGetClick = { /* Handle install */ }
                        )
                    }

                    if (searchResults.isEmpty()) {
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

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
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
