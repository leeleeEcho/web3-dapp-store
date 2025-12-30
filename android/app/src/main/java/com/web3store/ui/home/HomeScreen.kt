package com.web3store.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.web3store.ui.components.*
import com.web3store.ui.theme.DIColors

// Data classes
data class FeaturedApp(
    val id: String,
    val name: String,
    val tagline: String,
    val imageUrl: String,
    val chains: List<String>
)

data class PopularApp(
    val id: String,
    val name: String,
    val iconUrl: String,
    val rating: Float,
    val downloads: String,
    val chains: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAppClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onWalletClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "DeFi", "NFT", "GameFi", "Social", "DAO", "Tools")

    // Sample data
    val featuredApps = remember {
        listOf(
            FeaturedApp("1", "UniSwap", "Leading decentralized exchange", "https://via.placeholder.com/280x160", listOf("ETH")),
            FeaturedApp("2", "OpenSea", "World's largest NFT marketplace", "https://via.placeholder.com/280x160", listOf("ETH", "SOL")),
            FeaturedApp("3", "Axie Infinity", "Play to earn blockchain game", "https://via.placeholder.com/280x160", listOf("ETH"))
        )
    }

    val popularApps = remember {
        listOf(
            PopularApp("1", "Aave", "https://via.placeholder.com/56", 4.8f, "2.5M", listOf("ETH", "SOL")),
            PopularApp("2", "Curve Finance", "https://via.placeholder.com/56", 4.7f, "1.8M", listOf("ETH")),
            PopularApp("3", "Rarible", "https://via.placeholder.com/56", 4.6f, "1.2M", listOf("ETH", "SOL")),
            PopularApp("4", "PancakeSwap", "https://via.placeholder.com/56", 4.9f, "3.1M", listOf("BSC")),
            PopularApp("5", "MetaMask", "https://via.placeholder.com/56", 4.5f, "5.2M", listOf("ETH"))
        )
    }

    Scaffold(
        modifier = modifier.background(DIColors.Background),
        topBar = {
            HomeTopBar(
                walletAddress = "0x1a2b...3c4d",
                onWalletClick = onWalletClick
            )
        },
        containerColor = DIColors.Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Search Bar
            item {
                DISearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { /* Handle search */ },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Featured Section
            item {
                Column {
                    Text(
                        text = "Featured",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DIColors.TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(featuredApps) { app ->
                            FeaturedAppCard(
                                name = app.name,
                                tagline = app.tagline,
                                imageUrl = app.imageUrl,
                                chains = app.chains,
                                onClick = { onAppClick(app.id) }
                            )
                        }
                    }
                }
            }

            // Category Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        CategoryChip(
                            text = category,
                            selected = category == selectedCategory,
                            onClick = {
                                selectedCategory = category
                                if (category != "All") {
                                    onCategoryClick(category.lowercase())
                                }
                            }
                        )
                    }
                }
            }

            // Popular Apps Section
            item {
                Text(
                    text = "Popular Apps",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            itemsIndexed(popularApps) { index, app ->
                AppListCard(
                    name = app.name,
                    iconUrl = app.iconUrl,
                    rating = app.rating,
                    downloads = app.downloads,
                    chains = app.chains,
                    onClick = { onAppClick(app.id) },
                    onGetClick = { /* Handle install */ },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateEnterExit(
                            enter = fadeIn() + slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = index * 50
                                )
                            )
                        )
                )
            }

            // Bottom spacing for navigation bar
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    walletAddress: String,
    onWalletClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DIColors.Background.copy(alpha = 0.95f),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = "https://www.di.xyz/images/di-logo.png",
                    contentDescription = "DI Store Logo",
                    modifier = Modifier.height(40.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Wallet Button
            OutlinedButton(
                onClick = onWalletClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DIColors.Primary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    DIColors.Primary.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = walletAddress,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
