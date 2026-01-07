package com.web3store.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.web3store.domain.model.AppListItem
import com.web3store.domain.model.Category
import com.web3store.ui.theme.DIColors
import com.web3store.ui.viewmodel.HomeUiState
import com.web3store.ui.viewmodel.HomeViewModel

data class FeaturedApp(
    val id: String,
    val name: String,
    val tagline: String,
    val imageUrl: String,
    val gradient: List<Color>
)

data class AppItem(
    val id: String,
    val name: String,
    val developer: String,
    val iconUrl: String,
    val rating: Float,
    val size: String,
    val category: String
)

/**
 * 将 AppListItem 转换为 UI 使用的 AppItem
 */
private fun AppListItem.toAppItem(): AppItem {
    return AppItem(
        id = id.toString(),
        name = name,
        developer = developerName ?: "Unknown",
        iconUrl = iconUrl ?: "",
        rating = ratingAverage.toFloat(),
        size = formattedSize,
        category = categoryName ?: ""
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAppClick: (String) -> Unit,
    onCategoryClick: (Long, String) -> Unit,
    onSearchClick: () -> Unit,
    onWalletClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 从 API 数据转换为 UI 模型
    val featuredApps = remember(uiState.featuredApps) {
        if (uiState.featuredApps.isNotEmpty()) {
            uiState.featuredApps.take(5).mapIndexed { index, app ->
                val gradients = listOf(
                    listOf(Color(0xFFFF007A), Color(0xFF9B00FF)),
                    listOf(Color(0xFF2081E2), Color(0xFF1868B7)),
                    listOf(Color(0xFF00B4D8), Color(0xFF0077B6)),
                    listOf(Color(0xFF14F195), Color(0xFF9945FF)),
                    listOf(Color(0xFFFF6B9D), Color(0xFFFF8C42))
                )
                FeaturedApp(
                    id = app.id.toString(),
                    name = app.name,
                    tagline = app.shortDescription ?: "",
                    imageUrl = app.iconUrl ?: "",
                    gradient = gradients[index % gradients.size]
                )
            }
        } else {
            listOf(
                FeaturedApp("1", "Uniswap", "去中心化交易所", "", listOf(Color(0xFFFF007A), Color(0xFF9B00FF))),
                FeaturedApp("2", "OpenSea", "全球最大NFT市场", "", listOf(Color(0xFF2081E2), Color(0xFF1868B7))),
                FeaturedApp("3", "Axie Infinity", "边玩边赚GameFi", "", listOf(Color(0xFF00B4D8), Color(0xFF0077B6)))
            )
        }
    }

    val recommendedApps = remember(uiState.featuredApps) {
        uiState.featuredApps.map { it.toAppItem() }.ifEmpty {
            listOf(
                AppItem("1", "MetaMask", "ConsenSys", "", 4.5f, "32 MB", "工具"),
                AppItem("2", "Uniswap", "Uniswap Labs", "", 4.8f, "28 MB", "金融")
            )
        }
    }

    val topCharts = remember(uiState.topDownloads) {
        uiState.topDownloads.map { it.toAppItem() }.ifEmpty {
            listOf(
                AppItem("6", "PancakeSwap", "PancakeSwap", "", 4.7f, "25 MB", "金融"),
                AppItem("7", "Raydium", "Raydium", "", 4.5f, "30 MB", "金融")
            )
        }
    }

    val latestApps = remember(uiState.latestApps) {
        uiState.latestApps.map { it.toAppItem() }
    }

    val categories = remember(uiState.categories) {
        uiState.categories
    }

    Scaffold(
        modifier = modifier,
        containerColor = DIColors.Background,
        topBar = {
            HomeTopBar(
                onSearchClick = onSearchClick,
                onWalletClick = onWalletClick,
                onNotificationClick = onNotificationClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Featured Banner Carousel
            item {
                val pagerState = rememberPagerState(pageCount = { featuredApps.size })

                Column {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        pageSpacing = 12.dp
                    ) { page ->
                        FeaturedBanner(
                            app = featuredApps[page],
                            onClick = { onAppClick(featuredApps[page].id) }
                        )
                    }

                    // Page indicators
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(featuredApps.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == index) DIColors.Primary
                                        else DIColors.TextSecondary.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }
                }
            }

            // Recommended for you
            item {
                SectionHeader(title = "为你推荐", onSeeAllClick = { })
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommendedApps) { app ->
                        AppCard(
                            app = app,
                            onClick = { onAppClick(app.id) }
                        )
                    }
                }
            }

            // Top Charts
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "热门排行", onSeeAllClick = { })
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    topCharts.take(5).forEachIndexed { index, app ->
                        RankedAppRow(
                            rank = index + 1,
                            app = app,
                            onClick = { onAppClick(app.id) }
                        )
                        if (index < 4) {
                            Divider(
                                color = DIColors.Border,
                                modifier = Modifier.padding(start = 56.dp)
                            )
                        }
                    }
                }
            }

            // New & Updated
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "新上架", onSeeAllClick = { })
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val apps = latestApps.ifEmpty { recommendedApps.reversed() }
                    items(apps) { app ->
                        AppCard(
                            app = app,
                            onClick = { onAppClick(app.id) }
                        )
                    }
                }
            }

            // Categories quick access
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "分类浏览", onSeeAllClick = { })
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val categoryColors = listOf(
                        DIColors.Primary,
                        Color(0xFF9945FF),
                        Color(0xFF14F195),
                        Color(0xFFFF6B9D),
                        Color(0xFF00D4FF),
                        Color(0xFFFF8C42),
                        Color(0xFF00B4D8),
                        Color(0xFFFF007A)
                    )
                    if (categories.isNotEmpty()) {
                        items(categories.size) { index ->
                            val cat = categories[index]
                            val color = categoryColors[index % categoryColors.size]
                            CategoryChip(
                                name = cat.displayName,
                                color = color,
                                onClick = { onCategoryClick(cat.id, cat.displayName) }
                            )
                        }
                    } else {
                        // Fallback placeholder categories
                        val placeholders = listOf(
                            "DeFi" to DIColors.Primary,
                            "NFT" to Color(0xFF9945FF),
                            "游戏" to Color(0xFF14F195),
                            "社交" to Color(0xFFFF6B9D),
                            "工具" to Color(0xFF00D4FF)
                        )
                        items(placeholders.size) { index ->
                            val (name, color) = placeholders[index]
                            CategoryChip(
                                name = name,
                                color = color,
                                onClick = { }
                            )
                        }
                    }
                }
            }

            // 加载中状态
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DIColors.Primary)
                    }
                }
            }

            // 错误提示
            uiState.error?.let { error ->
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = Color(0xFFFF5252).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error,
                                color = Color(0xFFFF5252),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.refresh() }) {
                                Text("重试", color = DIColors.Primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onSearchClick: () -> Unit,
    onWalletClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    TopAppBar(
        title = {
            // Search bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
                    .clickable(onClick = onSearchClick),
                shape = RoundedCornerShape(24.dp),
                color = DIColors.Card
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = DIColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "搜索应用和游戏",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DIColors.TextSecondary
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onWalletClick) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "钱包",
                    tint = DIColors.Primary
                )
            }
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "通知",
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
private fun FeaturedBanner(
    app: FeaturedApp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(app.gradient))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = app.tagline,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "安装",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DIColors.TextPrimary
        )
        TextButton(onClick = onSeeAllClick) {
            Text(
                text = "查看全部",
                color = DIColors.Primary,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun AppCard(
    app: AppItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DIColors.Card),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.name.take(2),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DIColors.Primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = app.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = DIColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = app.rating.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = DIColors.TextSecondary
            )
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = DIColors.TextSecondary,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Composable
private fun RankedAppRow(
    rank: Int,
    app: AppItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank number
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (rank <= 3) DIColors.Primary else DIColors.TextSecondary,
            modifier = Modifier.width(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // App Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DIColors.Card),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.name.take(2),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DIColors.Primary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // App info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = DIColors.TextPrimary
            )
            Text(
                text = app.developer,
                style = MaterialTheme.typography.bodySmall,
                color = DIColors.TextSecondary
            )
        }

        // Rating
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = app.rating.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = DIColors.TextSecondary
            )
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = DIColors.Primary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun CategoryChip(
    name: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
