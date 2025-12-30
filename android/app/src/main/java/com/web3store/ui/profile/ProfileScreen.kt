package com.web3store.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.web3store.ui.theme.DIColors

data class MenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val showBadge: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit,
    onMyAppsClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val installedAppsCount = remember { 12 }
    val connectedChains = remember { 5 }

    val menuItems = remember {
        listOf(
            MenuItem(Icons.Outlined.Apps, "My dApps", "$installedAppsCount installed"),
            MenuItem(Icons.Outlined.Favorite, "Favorites", "23 saved"),
            MenuItem(Icons.Outlined.History, "Recently Used", "View history"),
            MenuItem(Icons.Outlined.Link, "Connected Sites", "$connectedChains active"),
        )
    }

    val settingsItems = remember {
        listOf(
            MenuItem(Icons.Outlined.Security, "Security", "Biometric, PIN"),
            MenuItem(Icons.Outlined.Notifications, "Notifications", showBadge = true),
            MenuItem(Icons.Outlined.Language, "Language", "English"),
            MenuItem(Icons.Outlined.DarkMode, "Appearance", "Dark"),
            MenuItem(Icons.Outlined.Storage, "Network & Cache"),
            MenuItem(Icons.Outlined.Info, "About", "Version 1.0.0"),
        )
    }

    Scaffold(
        modifier = modifier.background(DIColors.Background),
        containerColor = DIColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DIColors.Primary
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = DIColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DIColors.Background.copy(alpha = 0.95f)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            item {
                ProfileHeader()
            }

            // Stats Row
            item {
                StatsRow(
                    installedApps = installedAppsCount,
                    connectedChains = connectedChains
                )
            }

            // My dApps Section
            item {
                Text(
                    text = "My dApps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(menuItems) { item ->
                MenuListItem(
                    item = item,
                    onClick = {
                        when (item.title) {
                            "My dApps" -> onMyAppsClick()
                            else -> { }
                        }
                    }
                )
            }

            // Settings Section
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            items(settingsItems) { item ->
                MenuListItem(
                    item = item,
                    onClick = {
                        when (item.title) {
                            "Security" -> onSecurityClick()
                            "About" -> onAboutClick()
                            else -> { }
                        }
                    }
                )
            }

            // Sign Out Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { /* Handle sign out */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DIColors.Error
                    ),
                    border = BorderStroke(1.dp, DIColors.Error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Disconnect Wallet")
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DIColors.Card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(DIColors.Primary, DIColors.Secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = DIColors.Background,
                    modifier = Modifier.size(32.dp)
                )
            }

            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "0x1234...5678",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(DIColors.Success)
                    )
                    Text(
                        text = "Connected",
                        style = MaterialTheme.typography.bodySmall,
                        color = DIColors.TextSecondary
                    )
                }
            }

            // Copy & QR buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { /* Copy address */ },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DIColors.Surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = DIColors.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { /* Show QR */ },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DIColors.Surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Code",
                        tint = DIColors.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(
    installedApps: Int,
    connectedChains: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = installedApps.toString(),
            label = "Installed",
            icon = Icons.Outlined.Apps,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = connectedChains.toString(),
            label = "Chains",
            icon = Icons.Outlined.Link,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "23",
            label = "Favorites",
            icon = Icons.Outlined.Favorite,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DIColors.Card)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DIColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DIColors.TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = DIColors.TextSecondary
            )
        }
    }
}

@Composable
private fun MenuListItem(
    item: MenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DIColors.Card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DIColors.Surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = DIColors.Primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Title & Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = DIColors.TextPrimary
                    )
                    if (item.showBadge) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(DIColors.Primary)
                        )
                    }
                }
                item.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = DIColors.TextSecondary
                    )
                }
            }

            // Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = DIColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
