package com.web3store

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.web3store.ui.components.BottomNavItem
import com.web3store.ui.components.DIBottomNavigation
import com.web3store.ui.detail.AppDetailScreen
import com.web3store.ui.explore.ExploreScreen
import com.web3store.ui.home.HomeScreen
import com.web3store.ui.profile.ProfileScreen
import com.web3store.ui.search.SearchScreen
import com.web3store.ui.wallet.WalletScreen
import com.web3store.ui.theme.DIColors
import com.web3store.ui.theme.Web3StoreTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Web3StoreTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Determine if bottom nav should be shown
    val showBottomNav = remember(currentRoute) {
        currentRoute in listOf(
            BottomNavItem.Home.route,
            BottomNavItem.Explore.route,
            BottomNavItem.Search.route,
            BottomNavItem.Wallet.route,
            BottomNavItem.Profile.route
        )
    }

    // Map route to BottomNavItem
    val selectedNavItem = remember(currentRoute) {
        when (currentRoute) {
            BottomNavItem.Home.route -> BottomNavItem.Home
            BottomNavItem.Explore.route -> BottomNavItem.Explore
            BottomNavItem.Search.route -> BottomNavItem.Search
            BottomNavItem.Wallet.route -> BottomNavItem.Wallet
            BottomNavItem.Profile.route -> BottomNavItem.Profile
            else -> BottomNavItem.Home
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DIColors.Background),
        containerColor = DIColors.Background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                DIBottomNavigation(
                    selectedItem = selectedNavItem,
                    onItemSelected = { item ->
                        navController.navigate(item.route) {
                            popUpTo(BottomNavItem.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(
                bottom = if (showBottomNav) paddingValues.calculateBottomPadding() else 0.dp
            )
        ) {
            // Home
            composable(
                route = BottomNavItem.Home.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                HomeScreen(
                    onAppClick = { appId ->
                        navController.navigate("app/$appId")
                    },
                    onCategoryClick = { category ->
                        navController.navigate("category/$category")
                    },
                    onWalletClick = {
                        navController.navigate(BottomNavItem.Wallet.route)
                    }
                )
            }

            // Explore
            composable(
                route = BottomNavItem.Explore.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                ExploreScreen(
                    onAppClick = { appId ->
                        navController.navigate("app/$appId")
                    },
                    onCategoryClick = { category ->
                        navController.navigate("category/$category")
                    }
                )
            }

            // Search
            composable(
                route = BottomNavItem.Search.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                SearchScreen(
                    onAppClick = { appId ->
                        navController.navigate("app/$appId")
                    }
                )
            }

            // Wallet
            composable(
                route = BottomNavItem.Wallet.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                WalletScreen(
                    onConnectWallet = { /* Handle wallet connection */ },
                    onTokenClick = { /* Handle token click */ },
                    onNFTClick = { /* Handle NFT click */ }
                )
            }

            // Profile
            composable(
                route = BottomNavItem.Profile.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                ProfileScreen(
                    onSettingsClick = { /* Handle settings */ },
                    onMyAppsClick = { /* Handle my apps */ },
                    onSecurityClick = { /* Handle security */ },
                    onAboutClick = { /* Handle about */ }
                )
            }

            // App Detail
            composable(
                route = "app/{appId}",
                arguments = listOf(navArgument("appId") { type = NavType.StringType }),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) { backStackEntry ->
                val appId = backStackEntry.arguments?.getString("appId") ?: ""
                AppDetailScreen(
                    appId = appId,
                    onBackClick = { navController.popBackStack() },
                    onInstallClick = { /* Handle install */ }
                )
            }

            // Category
            composable(
                route = "category/{category}",
                arguments = listOf(navArgument("category") { type = NavType.StringType }),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                // TODO: Implement CategoryScreen
                PlaceholderScreen(title = "Category: $category")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DIColors.Background),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = DIColors.TextPrimary
        )
    }
}
