package com.jammit.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jammit.ui.auth.LoginScreen
import com.jammit.ui.auth.RegisterScreen
import com.jammit.ui.chatdetail.ChatDetailScreen
import com.jammit.ui.chats.ChatsScreen
import com.jammit.ui.explore.ExploreScreen
import com.jammit.ui.profile.ProfileScreen
import com.jammit.ui.userprofile.UserProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    startDestination: String = NavRoute.Login.route
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isMainScreen = currentRoute in listOf(
        NavRoute.Profile.route,
        NavRoute.Explore.route,
        NavRoute.Chats.route
    )

    Scaffold(
        bottomBar = {
            if (isMainScreen) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Authentication
            composable(NavRoute.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NavRoute.Profile.route) {
                            popUpTo(NavRoute.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(NavRoute.Register.route)
                    }
                )
            }

            composable(NavRoute.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(NavRoute.Profile.route) {
                            popUpTo(NavRoute.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // Main Tabs
            composable(NavRoute.Profile.route) {
                ProfileScreen()
            }

            composable(NavRoute.Explore.route) {
                ExploreScreen(
                    onUserClick = { userId ->
                        navController.navigate(NavRoute.UserProfile.createRoute(userId))
                    }
                )
            }

            composable(NavRoute.Chats.route) {
                ChatsScreen(
                    onChatClick = { chatId ->
                        navController.navigate(NavRoute.ChatDetail.createRoute(chatId))
                    }
                )
            }

            // Detail Screens
            composable(
                route = "user_profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                UserProfileScreen(
                    userId = userId,
                    onChatClick = { targetUserId ->
                        // Navigate to chat - in a real app, you'd need to find or create a chat ID
                        navController.navigate(NavRoute.ChatDetail.createRoute("chat_$targetUserId"))
                    }
                )
            }

            composable(
                route = "chat_detail/{chatId}",
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                ChatDetailScreen(chatId = chatId)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: androidx.navigation.NavController) {
    val items = listOf(
        BottomNavItem(NavRoute.Profile.route, "Profile", Icons.Default.Person),
        BottomNavItem(NavRoute.Explore.route, "Explore", Icons.Default.Search),
        BottomNavItem(NavRoute.Chats.route, "Chats", Icons.Default.Chat)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

