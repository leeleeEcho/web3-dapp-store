package com.web3store.ui.detail

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Security
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
import com.web3store.ui.components.ChainBadge
import com.web3store.ui.theme.DIColors

data class AppDetail(
    val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val iconUrl: String,
    val rating: Float,
    val downloads: String,
    val chains: List<String>,
    val screenshots: List<String>,
    val features: List<String>,
    val security: String,
    val website: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    appId: String,
    onBackClick: () -> Unit,
    onInstallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sample data - in real app, fetch from ViewModel
    val app = remember {
        AppDetail(
            id = appId,
            name = "Aave",
            tagline = "Decentralized Lending Protocol",
            description = "Aave is an open-source and non-custodial liquidity protocol for earning interest on deposits and borrowing assets. The protocol features Flash Loans, the first uncollateralized loan in DeFi.",
            iconUrl = "https://via.placeholder.com/96",
            rating = 4.8f,
            downloads = "2.5M",
            chains = listOf("Ethereum", "Polygon", "Avalanche", "Optimism"),
            screenshots = listOf(
                "https://via.placeholder.com/300x200",
                "https://via.placeholder.com/300x200",
                "https://via.placeholder.com/300x200"
            ),
            features = listOf("Lending & Borrowing", "Flash Loans", "Rate Switching", "Collateral Management"),
            security = "Audited by Trail of Bits, OpenZeppelin, and Consensys Diligence",
            website = "https://aave.com"
        )
    }

    Scaffold(
        modifier = modifier.background(DIColors.Background),
        topBar = {
            DetailTopBar(
                onBackClick = onBackClick,
                onShareClick = { /* Handle share */ }
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
            // App Header
            item {
                AppHeader(
                    app = app,
                    onInstallClick = onInstallClick
                )
            }

            // Screenshots
            item {
                ScreenshotsSection(screenshots = app.screenshots)
            }

            // Description
            item {
                DescriptionSection(description = app.description)
            }

            // Features
            item {
                FeaturesSection(features = app.features)
            }

            // Supported Chains
            item {
                ChainsSection(chains = app.chains)
            }

            // Security
            item {
                SecuritySection(security = app.security)
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopBar(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DIColors.Primary
                )
            }
        },
        actions = {
            OutlinedButton(
                onClick = onShareClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DIColors.Primary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    DIColors.Primary.copy(alpha = 0.5f)
                )
            ) {
                Text("Share")
            }
            Spacer(modifier = Modifier.width(16.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DIColors.Background.copy(alpha = 0.95f)
        ),
        modifier = modifier
    )
}

@Composable
private fun AppHeader(
    app: AppDetail,
    onInstallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Icon
            AsyncImage(
                model = app.iconUrl,
                contentDescription = app.name,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            // App Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = app.tagline,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DIColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = DIColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = app.rating.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DIColors.Primary
                        )
                    }

                    // Downloads
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = null,
                            tint = DIColors.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = app.downloads,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DIColors.TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Install Button
        Button(
            onClick = onInstallClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DIColors.Primary,
                contentColor = DIColors.Background
            )
        ) {
            Text(
                text = "Install App",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ScreenshotsSection(
    screenshots: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Screenshots",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DIColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(screenshots) { screenshot ->
                AsyncImage(
                    model = screenshot,
                    contentDescription = "Screenshot",
                    modifier = Modifier
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            DIColors.Primary.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun DescriptionSection(
    description: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "About",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DIColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = DIColors.TextSecondary,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
        )
    }
}

@Composable
private fun FeaturesSection(
    features: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Features",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DIColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        val rows = features.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEach { rowFeatures ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowFeatures.forEach { feature ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = DIColors.Card
                            )
                        ) {
                            Text(
                                text = feature,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = DIColors.TextPrimary
                            )
                        }
                    }
                    // Fill remaining space if odd number
                    if (rowFeatures.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChainsSection(
    chains: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Supported Chains",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DIColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            chains.forEach { chain ->
                ChainBadge(chain = chain)
            }
        }
    }
}

@Composable
private fun SecuritySection(
    security: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DIColors.Card
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = DIColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "Security",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = security,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DIColors.TextSecondary
                )
            }
        }
    }
}
