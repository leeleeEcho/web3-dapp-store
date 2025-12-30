package com.web3store.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.web3store.ui.theme.DIColors
import com.web3store.ui.theme.GoldGradient

/**
 * Featured app card for horizontal carousel
 */
@Composable
fun FeaturedAppCard(
    name: String,
    tagline: String,
    imageUrl: String,
    chains: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DIColors.Card
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                DIColors.Background.copy(alpha = 0f),
                                DIColors.Background.copy(alpha = 0.5f),
                                DIColors.Background.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = DIColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chains.forEach { chain ->
                        ChainBadge(chain = chain)
                    }
                }
            }
        }
    }
}

/**
 * Popular app list item card
 */
@Composable
fun AppListCard(
    name: String,
    iconUrl: String,
    rating: Float,
    downloads: String,
    chains: List<String>,
    onClick: () -> Unit,
    onGetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = DIColors.Primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DIColors.Card
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            AsyncImage(
                model = iconUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // App info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DIColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "★",
                            color = DIColors.Primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = rating.toString(),
                            color = DIColors.Primary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Downloads
                    Text(
                        text = "$downloads downloads",
                        color = DIColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    chains.take(3).forEach { chain ->
                        ChainBadge(chain = chain, small = true)
                    }
                }
            }

            // GET button
            GoldButton(
                text = "GET",
                onClick = onGetClick
            )
        }
    }
}

/**
 * Chain badge component
 */
@Composable
fun ChainBadge(
    chain: String,
    small: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (chain.uppercase()) {
        "ETH", "ETHEREUM" -> DIColors.ChainEthereum
        "SOL", "SOLANA" -> DIColors.ChainSolana
        "BSC", "BNB" -> DIColors.ChainBSC
        "POLYGON" -> DIColors.ChainPolygon
        "ARB", "ARBITRUM" -> DIColors.ChainArbitrum
        "OP", "OPTIMISM" -> DIColors.ChainOptimism
        "AVAX", "AVALANCHE" -> DIColors.ChainAvalanche
        else -> DIColors.Primary
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (small) 4.dp else 6.dp),
        color = backgroundColor.copy(alpha = 0.2f),
        border = null
    ) {
        Text(
            text = chain.uppercase().take(3),
            modifier = Modifier.padding(
                horizontal = if (small) 6.dp else 8.dp,
                vertical = if (small) 2.dp else 4.dp
            ),
            style = if (small) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            color = backgroundColor
        )
    }
}

/**
 * Gold gradient button
 */
@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DIColors.Primary,
            contentColor = DIColors.Background
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Category chip component
 */
@Composable
fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) DIColors.Primary else DIColors.Muted,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            DIColors.TextSecondary.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) DIColors.Background else DIColors.TextPrimary
        )
    }
}
