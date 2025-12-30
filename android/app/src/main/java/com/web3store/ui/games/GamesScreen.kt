package com.web3store.ui.games

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.web3store.ui.theme.DIColors

data class GameItem(
    val id: String,
    val name: String,
    val developer: String,
    val rating: Float,
    val size: String,
    val genre: String,
    val gradient: List<Color> = listOf(DIColors.Primary, DIColors.Secondary)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onGameClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember {
        listOf("全部", "动作", "策略", "角色扮演", "卡牌", "休闲", "竞技")
    }
    var selectedCategory by remember { mutableStateOf(0) }

    val featuredGames = remember {
        listOf(
            GameItem("1", "Axie Infinity", "Sky Mavis", 4.4f, "120 MB", "策略", listOf(Color(0xFF00B4D8), Color(0xFF0077B6))),
            GameItem("2", "Gods Unchained", "Immutable", 4.3f, "85 MB", "卡牌", listOf(Color(0xFF9945FF), Color(0xFF6B21A8))),
            GameItem("3", "Illuvium", "Illuvium", 4.5f, "200 MB", "角色扮演", listOf(Color(0xFFFF6B9D), Color(0xFFFF1744)))
        )
    }

    val topGames = remember {
        listOf(
            GameItem("4", "The Sandbox", "The Sandbox", 4.6f, "150 MB", "沙盒"),
            GameItem("5", "Decentraland", "Decentraland", 4.2f, "180 MB", "虚拟世界"),
            GameItem("6", "Splinterlands", "Splinterlands", 4.5f, "45 MB", "卡牌"),
            GameItem("7", "Alien Worlds", "Dacoco", 4.1f, "60 MB", "挖矿"),
            GameItem("8", "Star Atlas", "Star Atlas", 4.7f, "250 MB", "太空")
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = DIColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "游戏",
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

            // Featured Games
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "精选游戏",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredGames) { game ->
                        FeaturedGameCard(
                            game = game,
                            onClick = { onGameClick(game.id) }
                        )
                    }
                }
            }

            // Top Games List
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "热门游戏",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            items(topGames) { game ->
                GameListItem(
                    game = game,
                    onClick = { onGameClick(game.id) }
                )
            }
        }
    }
}

@Composable
private fun FeaturedGameCard(
    game: GameItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(game.gradient))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = game.genre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = game.rating.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text("安装", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameListItem(
    game: GameItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Game Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(game.gradient)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = game.name.take(2),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Game Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = game.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = DIColors.TextPrimary
            )
            Text(
                text = game.developer,
                style = MaterialTheme.typography.bodySmall,
                color = DIColors.TextSecondary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = DIColors.Primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = game.rating.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = DIColors.TextSecondary
                    )
                }
                Text(
                    text = "•",
                    color = DIColors.TextSecondary
                )
                Text(
                    text = game.size,
                    style = MaterialTheme.typography.labelSmall,
                    color = DIColors.TextSecondary
                )
            }
        }

        // Install button
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = DIColors.Primary
            ),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "安装",
                color = DIColors.Background,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
