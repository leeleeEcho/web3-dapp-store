package com.web3store.ui.auth

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.web3store.BuildConfig
import com.web3store.data.repository.AuthState
import com.web3store.ui.theme.DIColors
import com.web3store.ui.viewmodel.AuthViewModel
import com.web3store.ui.viewmodel.LoginEvent
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSkip: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 使用传统 Google Sign-In API - 更可靠，支持直接选择已登录账号
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // 处理登录结果
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google Sign-In 结果: resultCode = ${result.resultCode}")
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            Log.d(TAG, "Google Sign-In 成功，idToken 长度 = ${idToken?.length ?: 0}")
            if (idToken != null) {
                authViewModel.loginWithGoogle(idToken)
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("获取 ID Token 失败")
                }
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In 失败: ${e.statusCode} - ${e.message}", e)
            val errorMessage = when (e.statusCode) {
                12501 -> "登录已取消"
                12502 -> "登录流程已中断"
                else -> "Google 登录失败: ${e.statusCode}"
            }
            coroutineScope.launch {
                snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }

    // 监听登录事件
    LaunchedEffect(Unit) {
        authViewModel.loginEvent.collect { event ->
            when (event) {
                is LoginEvent.Success -> onLoginSuccess()
                is LoginEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // 如果已经登录，直接跳转
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DIColors.Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo 区域
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(DIColors.Primary, DIColors.Secondary)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DI",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Web3 DApp Store",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DIColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "发现、下载和管理优质 Web3 应用",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DIColors.TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Google 登录按钮
                GoogleSignInButton(
                    enabled = !isLoading,
                    isLoading = isLoading,
                    onClick = {
                        // 先尝试静默登录（使用已登录的账号）
                        Log.d(TAG, "尝试静默登录...")
                        googleSignInClient.silentSignIn()
                            .addOnSuccessListener { account ->
                                Log.d(TAG, "静默登录成功！idToken 长度 = ${account.idToken?.length ?: 0}")
                                account.idToken?.let { idToken ->
                                    authViewModel.loginWithGoogle(idToken)
                                } ?: run {
                                    // 静默登录成功但没有 idToken，需要交互式登录
                                    Log.d(TAG, "静默登录成功但没有 idToken，启动交互式登录")
                                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.d(TAG, "静默登录失败: ${e.message}，启动交互式登录")
                                // 静默登录失败，显示登录界面
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 显示错误信息
                if (authState is AuthState.Error) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 跳过登录
                TextButton(
                    onClick = onSkip,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "暂不登录，继续浏览",
                        color = DIColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = Color(0xFFDB4437),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "使用 Google 账号登录",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private const val TAG = "LoginScreen"
