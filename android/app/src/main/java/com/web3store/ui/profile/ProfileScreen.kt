package com.web3store.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.web3store.ui.theme.DIColors

data class InstalledApp(
    val id: String,
    val name: String,
    val lastUsed: String,
    val color: Color = DIColors.Primary
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onWalletClick: () -> Unit,
    onAppClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val installedApps = remember {
        listOf(
            InstalledApp("1", "MetaMask", "刚刚使用", Color(0xFFF6851B)),
            InstalledApp("2", "Uniswap", "1小时前", Color(0xFFFF007A)),
            InstalledApp("3", "OpenSea", "昨天", Color(0xFF2081E2)),
            InstalledApp("4", "Aave", "3天前", Color(0xFF2EBAC6))
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = DIColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "我的",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DIColors.Primary
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "设置",
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
            // Wallet Card
            item {
                WalletCard(
                    address = "0x1234...5678",
                    balance = "$12,345.67",
                    onClick = onWalletClick
                )
            }

            // Quick Actions
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickAction(
                        icon = Icons.Outlined.Download,
                        label = "已安装",
                        count = "12",
                        onClick = { }
                    )
                    QuickAction(
                        icon = Icons.Outlined.Favorite,
                        label = "收藏",
                        count = "8",
                        onClick = { }
                    )
                    QuickAction(
                        icon = Icons.Outlined.History,
                        label = "历史",
                        count = "",
                        onClick = { }
                    )
                    QuickAction(
                        icon = Icons.Outlined.Star,
                        label = "评价",
                        count = "3",
                        onClick = { }
                    )
                }
            }

            // Recently Used Apps
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "最近使用",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DIColors.TextPrimary
                    )
                    TextButton(onClick = { }) {
                        Text("管理", color = DIColors.Primary)
                    }
                }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(installedApps) { app ->
                        RecentAppCard(
                            app = app,
                            onClick = { onAppClick(app.id) }
                        )
                    }
                }
            }

            // Menu Items
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "账户与设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MenuItemRow(
                        icon = Icons.Outlined.AccountBalanceWallet,
                        title = "钱包管理",
                        subtitle = "查看资产、交易记录",
                        onClick = onWalletClick
                    )
                    MenuItemRow(
                        icon = Icons.Outlined.Notifications,
                        title = "通知设置",
                        subtitle = "应用更新、交易提醒",
                        onClick = { }
                    )
                    MenuItemRow(
                        icon = Icons.Outlined.Security,
                        title = "安全中心",
                        subtitle = "密码、生物识别",
                        onClick = { }
                    )
                    MenuItemRow(
                        icon = Icons.Outlined.Language,
                        title = "语言",
                        subtitle = "简体中文",
                        onClick = { }
                    )
                    MenuItemRow(
                        icon = Icons.Outlined.Info,
                        title = "关于",
                        subtitle = "版本 1.0.0",
                        onClick = { }
                    )
                    MenuItemRow(
                        icon = Icons.Outlined.Help,
                        title = "帮助与反馈",
                        subtitle = "",
                        onClick = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletCard(
    address: String,
    balance: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(DIColors.Primary, DIColors.Secondary)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = balance,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "总资产",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    count: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(DIColors.Card),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = DIColors.Primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = DIColors.TextPrimary
        )
        if (count.isNotEmpty()) {
            Text(
                text = count,
                style = MaterialTheme.typography.labelSmall,
                color = DIColors.TextSecondary
            )
        }
    }
}

@Composable
private fun RecentAppCard(
    app: InstalledApp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DIColors.Card)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = DIColors.TextPrimary
            )
            Text(
                text = app.lastUsed,
                style = MaterialTheme.typography.labelSmall,
                color = DIColors.TextSecondary
            )
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DIColors.Card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DIColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = DIColors.TextPrimary
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = DIColors.TextSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = DIColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
