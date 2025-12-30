package com.web3store.ui.wallet

import androidx.compose.animation.*
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.web3store.ui.theme.DIColors

data class WalletToken(
    val symbol: String,
    val name: String,
    val balance: String,
    val value: String,
    val change: Float,
    val chainColor: Color
)

data class WalletNFT(
    val id: String,
    val name: String,
    val collection: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onConnectWallet: () -> Unit,
    onTokenClick: (String) -> Unit,
    onNFTClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isWalletConnected by remember { mutableStateOf(true) } // Demo: wallet connected
    var selectedTab by remember { mutableStateOf(0) }

    val walletAddress = remember { "0x1234...5678" }
    val totalBalance = remember { "$12,345.67" }

    val tokens = remember {
        listOf(
            WalletToken("ETH", "Ethereum", "2.5", "$4,125.00", 2.5f, DIColors.ChainEthereum),
            WalletToken("SOL", "Solana", "45.2", "$3,842.00", -1.2f, DIColors.ChainSolana),
            WalletToken("BNB", "BNB Chain", "8.3", "$2,490.00", 0.8f, DIColors.ChainBSC),
            WalletToken("MATIC", "Polygon", "1,234", "$987.20", 3.2f, DIColors.ChainPolygon),
            WalletToken("AVAX", "Avalanche", "28.5", "$901.47", -0.5f, DIColors.ChainAvalanche)
        )
    }

    val nfts = remember {
        listOf(
            WalletNFT("1", "Bored Ape #1234", "BAYC", "https://via.placeholder.com/150"),
            WalletNFT("2", "CryptoPunk #5678", "CryptoPunks", "https://via.placeholder.com/150"),
            WalletNFT("3", "Azuki #9012", "Azuki", "https://via.placeholder.com/150"),
            WalletNFT("4", "Doodle #3456", "Doodles", "https://via.placeholder.com/150")
        )
    }

    Scaffold(
        modifier = modifier.background(DIColors.Background),
        containerColor = DIColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wallet",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DIColors.Primary
                    )
                },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
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
        if (!isWalletConnected) {
            // Connect Wallet State
            ConnectWalletView(
                onConnectClick = {
                    isWalletConnected = true
                    onConnectWallet()
                },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // Wallet Connected State
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Balance Card
                item {
                    BalanceCard(
                        address = walletAddress,
                        balance = totalBalance
                    )
                }

                // Quick Actions
                item {
                    QuickActionsRow()
                }

                // Tabs
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DIColors.Background,
                        contentColor = DIColors.Primary,
                        indicator = { tabPositions ->
                            Box(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTab])
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(DIColors.Primary)
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = "Tokens",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selectedContentColor = DIColors.Primary,
                            unselectedContentColor = DIColors.TextSecondary
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = "NFTs",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selectedContentColor = DIColors.Primary,
                            unselectedContentColor = DIColors.TextSecondary
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = {
                                Text(
                                    text = "Activity",
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selectedContentColor = DIColors.Primary,
                            unselectedContentColor = DIColors.TextSecondary
                        )
                    }
                }

                // Tab Content
                when (selectedTab) {
                    0 -> {
                        items(tokens) { token ->
                            TokenItem(
                                token = token,
                                onClick = { onTokenClick(token.symbol) }
                            )
                        }
                    }
                    1 -> {
                        item {
                            NFTGrid(
                                nfts = nfts,
                                onNFTClick = onNFTClick
                            )
                        }
                    }
                    2 -> {
                        item {
                            ActivityPlaceholder()
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
private fun ConnectWalletView(
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Wallet Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(DIColors.Primary, DIColors.Secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = DIColors.Background,
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                text = "Connect Your Wallet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DIColors.TextPrimary
            )

            Text(
                text = "Connect your wallet to view your assets, NFTs, and interact with dApps",
                style = MaterialTheme.typography.bodyMedium,
                color = DIColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onConnectClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DIColors.Primary,
                    contentColor = DIColors.Background
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Connect Wallet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(
    address: String,
    balance: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(DIColors.Primary, DIColors.Secondary)
                    )
                )
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Address
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DIColors.Background.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = DIColors.Background,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DIColors.Background.copy(alpha = 0.8f)
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = DIColors.Background.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Balance
                Column {
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = DIColors.Background.copy(alpha = 0.7f)
                    )
                    Text(
                        text = balance,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = DIColors.Background
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = Icons.Default.ArrowUpward,
            label = "Send",
            onClick = { }
        )
        QuickActionButton(
            icon = Icons.Default.ArrowDownward,
            label = "Receive",
            onClick = { }
        )
        QuickActionButton(
            icon = Icons.Default.SwapHoriz,
            label = "Swap",
            onClick = { }
        )
        QuickActionButton(
            icon = Icons.Default.QrCode,
            label = "Scan",
            onClick = { }
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = DIColors.TextSecondary
        )
    }
}

@Composable
private fun TokenItem(
    token: WalletToken,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DIColors.Card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Token Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(token.chainColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = token.symbol.take(2),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Token Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = token.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary
                )
                Text(
                    text = token.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = DIColors.TextSecondary
                )
            }

            // Balance & Value
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${token.balance} ${token.symbol}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = DIColors.TextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = token.value,
                        style = MaterialTheme.typography.bodySmall,
                        color = DIColors.TextSecondary
                    )
                    Text(
                        text = "${if (token.change >= 0) "+" else ""}${token.change}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (token.change >= 0) DIColors.Success else DIColors.Error
                    )
                }
            }
        }
    }
}

@Composable
private fun NFTGrid(
    nfts: List<WalletNFT>,
    onNFTClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        nfts.chunked(2).forEach { rowNfts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowNfts.forEach { nft ->
                    NFTCard(
                        nft = nft,
                        onClick = { onNFTClick(nft.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowNfts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun NFTCard(
    nft: WalletNFT,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DIColors.Card)
    ) {
        Column {
            // NFT Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DIColors.Surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = DIColors.TextSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // NFT Info
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = nft.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = nft.collection,
                    style = MaterialTheme.typography.bodySmall,
                    color = DIColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ActivityPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = DIColors.TextSecondary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No recent activity",
                style = MaterialTheme.typography.titleMedium,
                color = DIColors.TextPrimary
            )
            Text(
                text = "Your transactions will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = DIColors.TextSecondary
            )
        }
    }
}
