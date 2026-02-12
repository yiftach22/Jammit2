package com.jammit.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
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
import com.jammit.ui.auth.RegisterUsernameScreen
import com.jammit.ui.auth.RegisterInstrumentsScreen
import com.jammit.ui.chatdetail.ChatDetailScreen
import com.jammit.ui.chats.ChatsScreen
import com.jammit.ui.explore.ExploreScreen
import com.jammit.ui.instruments.EditInstrumentsScreen
import com.jammit.ui.profile.ProfileScreen
import com.jammit.ui.userprofile.UserProfileScreen
import com.jammit.data.UnreadStore
import com.jammit.network.ChatNotificationManager
import com.jammit.network.RetrofitClient
import com.jammit.repository.UserRepository
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import com.jammit.data.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

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
    val context = LocalContext.current

    LaunchedEffect(currentRoute) {
        val uid = SessionManager.getUserId(context)
        if (!uid.isNullOrBlank() && currentRoute != NavRoute.Login.route) {
            ChatNotificationManager.connect(context, uid)
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                if (token.isNotBlank()) {
                    UserRepository(RetrofitClient.apiService).updateFcmToken(uid, token)
                }
            } catch (_: Exception) { }
        } else if (currentRoute == NavRoute.Login.route) {
            ChatNotificationManager.disconnect()
        }
    }

    LaunchedEffect(Unit) {
        val activity = context as? ComponentActivity
        if (SessionManager.getUserId(context).isNullOrBlank()) return@LaunchedEffect
        val chatId = activity?.intent?.getStringExtra("navigate_to_chat")
            ?: activity?.intent?.getStringExtra("chatId")
        if (chatId != null) {
            activity?.intent?.removeExtra("navigate_to_chat")
            activity?.intent?.removeExtra("chatId")
            navController.navigate(NavRoute.ChatDetail.createRoute(chatId))
        }
    }

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
                    onLoginSuccess = { userId ->
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
                    onRegisterSuccess = { registrationData ->
                        navController.navigate("register_username?data=${java.net.URLEncoder.encode(registrationData, "UTF-8")}")
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "register_username?data={data}",
                arguments = listOf(navArgument("data") { type = NavType.StringType })
            ) { backStackEntry ->
                val registrationData = backStackEntry.arguments?.getString("data") ?: return@composable
                RegisterUsernameScreen(
                    registrationData = java.net.URLDecoder.decode(registrationData, "UTF-8"),
                    onNext = { updatedData ->
                        navController.navigate("register_instruments?data=${java.net.URLEncoder.encode(updatedData, "UTF-8")}")
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "register_instruments?data={data}",
                arguments = listOf(navArgument("data") { type = NavType.StringType })
            ) { backStackEntry ->
                val registrationData = backStackEntry.arguments?.getString("data") ?: return@composable
                RegisterInstrumentsScreen(
                    registrationData = java.net.URLDecoder.decode(registrationData, "UTF-8"),
                    onComplete = { userId ->
                        navController.navigate(NavRoute.Profile.route) {
                            popUpTo(NavRoute.Login.route) { inclusive = true }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Main Tabs
            composable(NavRoute.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(NavRoute.Login.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    }
                    ,
                    onEditInstruments = {
                        navController.navigate(NavRoute.EditInstruments.route)
                    }
                )
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

            composable(NavRoute.EditInstruments.route) {
                EditInstrumentsScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
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
                    onChatClick = { chatId ->
                        navController.navigate(NavRoute.ChatDetail.createRoute(chatId))
                    }
                )
            }

            composable(
                route = "chat_detail/{chatId}",
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                val ctx = androidx.compose.ui.platform.LocalContext.current
                val currentUserId = remember { com.jammit.data.SessionManager.getUserId(ctx) } ?: ""
                if (currentUserId.isBlank()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("You must be logged in to view the chat.")
                    }
                } else {
                    ChatDetailScreen(chatId = chatId, currentUserId = currentUserId)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(navController: androidx.navigation.NavController) {
    val items = listOf(
        BottomNavItem(NavRoute.Profile.route, "Profile", Icons.Default.Person),
        BottomNavItem(NavRoute.Explore.route, "Explore", Icons.Default.Search),
        BottomNavItem(NavRoute.Chats.route, "Chats", Icons.Default.Chat)
    )
    val unread by UnreadStore.unreadCount.collectAsState(initial = 0)

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val showBadge = item.route == NavRoute.Chats.route && unread > 0
            NavigationBarItem(
                icon = {
                    if (showBadge) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(
                                        text = minOf(unread, 99).toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
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

