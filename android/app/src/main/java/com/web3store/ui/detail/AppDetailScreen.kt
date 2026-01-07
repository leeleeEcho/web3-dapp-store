package com.web3store.ui.detail

import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.web3store.domain.model.AppDetail
import com.web3store.ui.components.ChainBadge
import com.web3store.ui.theme.DIColors
import com.web3store.ui.viewmodel.ActionButtonState
import com.web3store.ui.viewmodel.AppDetailUiState
import com.web3store.ui.viewmodel.AppDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    appId: String,
    onBackClick: () -> Unit,
    onInstallClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 如果 ViewModel 没有自动加载（appId 从导航参数获取失败），手动触发加载
    LaunchedEffect(appId) {
        if (uiState.appDetail == null && !uiState.isLoading && uiState.error == null) {
            viewModel.loadAppDetailByStringId(appId)
        }
    }

    // 安装权限对话框
    if (uiState.needsInstallPermission) {
        AlertDialog(
            onDismissRequest = { /* Do nothing, require user action */ },
            title = {
                Text(
                    text = "需要安装权限",
                    color = DIColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = "安装应用需要允许「安装未知来源应用」权限。点击「去设置」打开权限设置。",
                    color = DIColors.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        (context as? Activity)?.let { activity ->
                            viewModel.requestInstallPermission(activity)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DIColors.Primary)
                ) {
                    Text("去设置")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onPermissionResult()
                    }
                ) {
                    Text("取消", color = DIColors.TextSecondary)
                }
            },
            containerColor = DIColors.Card
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
        when {
            uiState.isLoading -> {
                // 加载中
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DIColors.Primary)
                }
            }
            uiState.error != null -> {
                // 错误状态
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "加载失败",
                            color = Color(0xFFFF5252),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = { viewModel.loadAppDetailByStringId(appId) },
                            colors = ButtonDefaults.buttonColors(containerColor = DIColors.Primary)
                        ) {
                            Text("重试")
                        }
                    }
                }
            }
            uiState.appDetail != null -> {
                val app = uiState.appDetail!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // App Header
                    item {
                        AppHeaderSection(
                            app = app,
                            buttonState = uiState.buttonState,
                            onButtonClick = {
                                Log.i("AppDetailScreen", "onButtonClick called from LazyColumn item, buttonState=${uiState.buttonState}")
                                Toast.makeText(context, "Button clicked: ${uiState.buttonState}", Toast.LENGTH_SHORT).show()
                                viewModel.onPrimaryButtonClick()
                            },
                            onCancelClick = { viewModel.cancelDownload() }
                        )
                    }

                    // Screenshots
                    if (app.screenshots.isNotEmpty()) {
                        item {
                            ScreenshotsSection(screenshots = app.screenshots.map { it.imageUrl })
                        }
                    }

                    // Description
                    app.description?.let { description ->
                        item {
                            DescriptionSection(description = description)
                        }
                    }

                    // App Info
                    item {
                        AppInfoSection(app = app)
                    }

                    // Supported Chains
                    if (app.chains.isNotEmpty()) {
                        item {
                            ChainsSection(chains = app.chains)
                        }
                    }

                    // Developer Info
                    item {
                        DeveloperSection(app = app)
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
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
private fun AppHeaderSection(
    app: AppDetail,
    buttonState: ActionButtonState,
    onButtonClick: () -> Unit,
    onCancelClick: () -> Unit,
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
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DIColors.Card),
                contentAlignment = Alignment.Center
            ) {
                if (app.iconUrl != null) {
                    AsyncImage(
                        model = app.iconUrl,
                        contentDescription = app.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = app.name.take(2),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = DIColors.Primary
                    )
                }
            }

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
                    text = app.shortDescription ?: "",
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
                            text = String.format("%.1f", app.ratingAverage),
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
                            text = app.formattedDownloads,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DIColors.TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Button based on state
        when (buttonState) {
            is ActionButtonState.Downloading -> {
                Column {
                    @Suppress("DEPRECATION")
                    LinearProgressIndicator(
                        progress = buttonState.progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = DIColors.Primary,
                        trackColor = DIColors.Card
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "下载中 ${(buttonState.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DIColors.TextSecondary
                        )
                        TextButton(onClick = onCancelClick) {
                            Text(
                                text = "取消",
                                color = DIColors.Primary
                            )
                        }
                    }
                }
            }
            is ActionButtonState.Installing -> {
                Column {
                    @Suppress("DEPRECATION")
                    LinearProgressIndicator(
                        progress = buttonState.progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = DIColors.Primary,
                        trackColor = DIColors.Card
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "安装中...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DIColors.TextSecondary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
            else -> {
                val (buttonText, buttonColor) = when (buttonState) {
                    is ActionButtonState.Download -> "下载" to DIColors.Primary
                    is ActionButtonState.Install -> "安装" to DIColors.Primary
                    is ActionButtonState.Open -> "打开" to Color(0xFF4CAF50)
                    is ActionButtonState.Update -> "更新" to DIColors.Primary
                    else -> "下载" to DIColors.Primary
                }

                Log.i("AppDetailScreen", "Rendering button: text=$buttonText, state=$buttonState")

                Button(
                    onClick = {
                        Log.i("AppDetailScreen", "Button clicked! buttonState=$buttonState")
                        onButtonClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = DIColors.Background
                    )
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
private fun AppInfoSection(
    app: AppDetail,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "应用信息",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DIColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DIColors.Card)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(label = "版本", value = app.versionName)
                InfoRow(label = "大小", value = app.formattedSize)
                InfoRow(label = "包名", value = app.packageName)
                if (app.isWeb3) {
                    InfoRow(label = "类型", value = "Web3 DApp")
                }
                app.websiteUrl?.let {
                    InfoRow(label = "网站", value = it)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = DIColors.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = DIColors.TextPrimary
        )
    }
}

@Composable
private fun DeveloperSection(
    app: AppDetail,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "开发者",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DIColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DIColors.Card)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Developer Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DIColors.Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (app.developer.companyName ?: "D").take(1),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DIColors.Primary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = app.developer.companyName ?: "Unknown Developer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DIColors.TextPrimary
                        )
                        if (app.developer.isVerified) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = "已验证",
                                tint = DIColors.Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = "${app.developer.totalApps} 款应用",
                        style = MaterialTheme.typography.bodySmall,
                        color = DIColors.TextSecondary
                    )
                }
            }
        }
    }
}
