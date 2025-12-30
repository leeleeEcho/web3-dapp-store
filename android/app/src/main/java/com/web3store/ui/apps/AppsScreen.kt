package com.web3store.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.web3store.ui.theme.DIColors

data class AppListItem(
    val id: String,
    val name: String,
    val developer: String,
    val rating: Float,
    val size: String,
    val category: String,
    val color: Color = DIColors.Primary
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    onAppClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember {
        listOf("全部", "DeFi", "NFT", "钱包", "工具", "社交", "DAO")
    }
    var selectedCategory by remember { mutableStateOf(0) }

    val essentialApps = remember {
        listOf(
            AppListItem("1", "MetaMask", "ConsenSys", 4.5f, "32 MB", "钱包", Color(0xFFF6851B)),
            AppListItem("2", "Trust Wallet", "Trust Wallet", 4.6f, "45 MB", "钱包", Color(0xFF3375BB)),
            AppListItem("3", "Phantom", "Phantom", 4.7f, "28 MB", "钱包", Color(0xFF9945FF))
        )
    }

    val defiApps = remember {
        listOf(
            AppListItem("4", "Uniswap", "Uniswap Labs", 4.8f, "28 MB", "DeFi", Color(0xFFFF007A)),
            AppListItem("5", "Aave", "Aave Labs", 4.6f, "22 MB", "DeFi", Color(0xFF2EBAC6)),
            AppListItem("6", "Compound", "Compound Labs", 4.4f, "18 MB", "DeFi", Color(0xFF00D395)),
            AppListItem("7", "Lido", "Lido DAO", 4.5f, "15 MB", "DeFi", Color(0xFF00A3FF)),
            AppListItem("8", "1inch", "1inch Network", 4.3f, "25 MB", "DeFi", Color(0xFFD82122))
        )
    }

    val nftApps = remember {
        listOf(
            AppListItem("9", "OpenSea", "OpenSea", 4.3f, "45 MB", "NFT", Color(0xFF2081E2)),
            AppListItem("10", "Blur", "Blur Inc", 4.4f, "38 MB", "NFT", Color(0xFFFF6B00)),
            AppListItem("11", "Magic Eden", "Magic Eden", 4.6f, "42 MB", "NFT", Color(0xFFE42575)),
            AppListItem("12", "Rarible", "Rarible", 4.2f, "35 MB", "NFT", Color(0xFFFEDA03))
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = DIColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "应用",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DIColors.Primary
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索",
                            tint = DIColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DIColors.Background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Category tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedCategory,
                    containerColor = DIColors.Background,
                    contentColor = DIColors.Primary,
                    edgePadding = 16.dp,
                    indicator = {},
                    divider = {}
                ) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = selectedCategory == index,
                            onClick = { selectedCategory = index },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (selectedCategory == index) DIColors.Primary else DIColors.Card
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = if (selectedCategory == index) DIColors.Background else DIColors.TextSecondary,
                                    fontWeight = if (selectedCategory == index) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Essential Apps
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("必备应用")
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(essentialApps) { app ->
                        EssentialAppCard(
                            app = app,
                            onClick = { onAppClick(app.id) }
                        )
                    }
                }
            }

            // DeFi Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("DeFi 金融")
            }

            items(defiApps) { app ->
                AppRowItem(
                    app = app,
                    onClick = { onAppClick(app.id) }
                )
            }

            // NFT Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("NFT 市场")
            }

            items(nftApps) { app ->
                AppRowItem(
                    app = app,
                    onClick = { onAppClick(app.id) }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DIColors.TextPrimary
        )
        TextButton(onClick = { }) {
            Text(
                text = "更多",
                color = DIColors.Primary
            )
        }
    }
}

@Composable
private fun EssentialAppCard(
    app: AppListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DIColors.Card)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(app.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.name.take(2),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = DIColors.TextPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = DIColors.Primary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = app.rating.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = DIColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DIColors.Primary),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Text(
                    text = "安装",
                    style = MaterialTheme.typography.labelMedium,
                    color = DIColors.Background
                )
            }
        }
    }
}

@Composable
private fun AppRowItem(
    app: AppListItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(app.color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.name.take(2),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // App Info
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = DIColors.Primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = app.rating.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = DIColors.TextSecondary
                    )
                }
                Text("•", color = DIColors.TextSecondary, style = MaterialTheme.typography.labelSmall)
                Text(
                    text = app.size,
                    style = MaterialTheme.typography.labelSmall,
                    color = DIColors.TextSecondary
                )
            }
        }

        // Install button
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DIColors.Primary),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "安装",
                fontWeight = FontWeight.Medium
            )
        }
    }
}
