package com.web3store.ui.explore

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.web3store.ui.components.AppListCard
import com.web3store.ui.theme.DIColors

data class TrendingCategory(
    val name: String,
    val count: Int,
    val icon: ImageVector,
    val gradient: List<androidx.compose.ui.graphics.Color>
)

data class NewApp(
    val id: String,
    val name: String,
    val category: String,
    val rating: Float,
    val iconUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onAppClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val trendingCategories = remember {
        listOf(
            TrendingCategory(
                "DeFi", 234, Icons.Default.TrendingUp,
                listOf(DIColors.Primary, DIColors.Secondary)
            ),
            TrendingCategory(
                "NFT", 189, Icons.Outlined.NewReleases,
                listOf(DIColors.Secondary, DIColors.Primary)
            ),
            TrendingCategory(
                "GameFi", 156, Icons.Outlined.Explore,
                listOf(DIColors.CategoryGameFi, DIColors.Primary)
            )
        )
    }

    val newApps = remember {
        listOf(
            NewApp("101", "SushiSwap", "DeFi", 4.6f, "https://via.placeholder.com/56"),
            NewApp("102", "Decentraland", "GameFi", 4.4f, "https://via.placeholder.com/56"),
            NewApp("103", "SuperRare", "NFT", 4.7f, "https://via.placeholder.com/56"),
            NewApp("104", "Compound", "DeFi", 4.5f, "https://via.placeholder.com/56")
        )
    }

    Scaffold(
        modifier = modifier.background(DIColors.Background),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Explore dApps",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DIColors.Primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DIColors.Background.copy(alpha = 0.95f)
                )
            )
        },
        containerColor = DIColors.Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Trending Categories Section
            item {
                Text(
                    text = "Trending Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    trendingCategories.forEach { category ->
                        TrendingCategoryCard(
                            category = category,
                            onClick = { onCategoryClick(category.name.lowercase()) }
                        )
                    }
                }
            }

            // New & Noteworthy Section
            item {
                Text(
                    text = "New & Noteworthy",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(newApps) { app ->
                AppListCard(
                    name = app.name,
                    iconUrl = app.iconUrl,
                    rating = app.rating,
                    downloads = "New",
                    chains = listOf("ETH"),
                    onClick = { onAppClick(app.id) },
                    onGetClick = { /* Handle install */ }
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun TrendingCategoryCard(
    category: TrendingCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DIColors.Card
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(category.gradient)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = DIColors.Background,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Category info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary
                )
                Text(
                    text = "${category.count} apps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DIColors.TextSecondary
                )
            }

            // Arrow indicator
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = DIColors.Primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
