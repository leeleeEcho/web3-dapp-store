package com.web3store.ui.updates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.web3store.ui.theme.DIColors

data class UpdateItem(
    val id: String,
    val name: String,
    val developer: String,
    val currentVersion: String,
    val newVersion: String,
    val updateSize: String,
    val changelog: String,
    val color: Color = DIColors.Primary
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen(
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingUpdates = remember {
        listOf(
            UpdateItem(
                "1", "MetaMask", "ConsenSys",
                "7.15.0", "7.16.2", "8 MB",
                "• 修复安全漏洞\n• 优化交易速度\n• 新增多链支持",
                Color(0xFFF6851B)
            ),
            UpdateItem(
                "2", "Uniswap", "Uniswap Labs",
                "1.28", "1.30", "5 MB",
                "• 新增限价单功能\n• 改进滑点设置\n• 修复UI问题",
                Color(0xFFFF007A)
            ),
            UpdateItem(
                "3", "OpenSea", "OpenSea",
                "2.5.0", "2.6.1", "12 MB",
                "• 支持更多NFT标准\n• 改进搜索功能\n• 优化加载速度",
                Color(0xFF2081E2)
            )
        )
    }

    val recentlyUpdated = remember {
        listOf(
            UpdateItem(
                "4", "Phantom", "Phantom",
                "24.1.0", "24.1.0", "",
                "已是最新版本",
                Color(0xFF9945FF)
            ),
            UpdateItem(
                "5", "Aave", "Aave Labs",
                "3.2.1", "3.2.1", "",
                "已是最新版本",
                Color(0xFF2EBAC6)
            )
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = DIColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "更新",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DIColors.Primary
                    )
                },
                actions = {
                    IconButton(onClick = { /* Refresh */ }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
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
            // Update All Button
            if (pendingUpdates.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${pendingUpdates.size} 个应用可更新",
                            style = MaterialTheme.typography.bodyLarge,
                            color = DIColors.TextPrimary
                        )
                        Button(
                            onClick = { /* Update all */ },
                            colors = ButtonDefaults.buttonColors(containerColor = DIColors.Primary),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("全部更新", color = DIColors.Background)
                        }
                    }
                }

                // Pending Updates
                item {
                    Text(
                        text = "待更新",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DIColors.TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(pendingUpdates) { update ->
                    UpdateListItem(
                        update = update,
                        isPending = true,
                        onClick = { onAppClick(update.id) },
                        onUpdateClick = { /* Handle update */ }
                    )
                }
            }

            // Recently Updated
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "最近更新",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(recentlyUpdated) { update ->
                UpdateListItem(
                    update = update,
                    isPending = false,
                    onClick = { onAppClick(update.id) },
                    onUpdateClick = { }
                )
            }

            // Empty state placeholder
            if (pendingUpdates.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DIColors.Success,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "所有应用已是最新版本",
                                style = MaterialTheme.typography.titleMedium,
                                color = DIColors.TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateListItem(
    update: UpdateItem,
    isPending: Boolean,
    onClick: () -> Unit,
    onUpdateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DIColors.Card)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // App Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(update.color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = update.name.take(2),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // App Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = update.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = DIColors.TextPrimary
                    )
                    Text(
                        text = update.developer,
                        style = MaterialTheme.typography.bodySmall,
                        color = DIColors.TextSecondary
                    )
                    if (isPending) {
                        Text(
                            text = "${update.currentVersion} → ${update.newVersion} • ${update.updateSize}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DIColors.Primary
                        )
                    } else {
                        Text(
                            text = "版本 ${update.currentVersion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DIColors.TextSecondary
                        )
                    }
                }

                // Update/Open button
                if (isPending) {
                    Button(
                        onClick = onUpdateClick,
                        colors = ButtonDefaults.buttonColors(containerColor = DIColors.Primary),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("更新", color = DIColors.Background)
                    }
                } else {
                    OutlinedButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DIColors.TextSecondary)
                    ) {
                        Text("打开")
                    }
                }
            }

            // Changelog
            if (isPending && update.changelog.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "更新内容:",
                    style = MaterialTheme.typography.labelMedium,
                    color = DIColors.TextSecondary
                )
                Text(
                    text = update.changelog,
                    style = MaterialTheme.typography.bodySmall,
                    color = DIColors.TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
