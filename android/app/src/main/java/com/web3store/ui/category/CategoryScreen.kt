package com.web3store.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.web3store.R
import com.web3store.domain.model.AppListItem
import com.web3store.ui.theme.DIColors
import com.web3store.ui.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onAppClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Detect scroll to bottom for pagination
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 3 && !uiState.isLoadingMore && uiState.hasMore
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState.apps.isNotEmpty()) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        modifier = modifier.background(DIColors.Background),
        containerColor = DIColors.Background,
        topBar = {
            CategoryTopBar(
                title = uiState.categoryName,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = DIColors.Primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                uiState.error != null && uiState.apps.isEmpty() -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.category_load_failed),
                            style = MaterialTheme.typography.titleMedium,
                            color = DIColors.Error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DIColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.refresh() }) {
                            Text(stringResource(R.string.action_retry), color = DIColors.Primary)
                        }
                    }
                }
                uiState.apps.isEmpty() -> {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = DIColors.TextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.category_empty),
                            style = MaterialTheme.typography.titleMedium,
                            color = DIColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.category_empty_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DIColors.TextSecondary
                        )
                    }
                }
                else -> {
                    // App list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Results count
                        item {
                            Text(
                                text = stringResource(R.string.category_app_count, uiState.apps.size),
                                style = MaterialTheme.typography.bodyMedium,
                                color = DIColors.TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(uiState.apps) { app ->
                            CategoryAppCard(
                                app = app,
                                onClick = { onAppClick(app.id.toString()) }
                            )
                        }

                        // Loading more indicator
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = DIColors.Primary,
                                        modifier = Modifier.size(24.dp)
                                    )
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DIColors.TextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.category_back),
                    tint = DIColors.TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DIColors.Background
        )
    )
}

@Composable
private fun CategoryAppCard(
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

            Text(
                text = app.shortDescription ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = DIColors.TextSecondary,
                maxLines = 1
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "★ ${String.format("%.1f", app.ratingAverage)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DIColors.Primary
                )
                Text(
                    text = app.formattedDownloads,
                    style = MaterialTheme.typography.bodySmall,
                    color = DIColors.TextSecondary
                )
                if (app.chains.isNotEmpty()) {
                    Text(
                        text = app.chains.take(2).joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = DIColors.TextSecondary
                    )
                }
            }
        }
    }
}
