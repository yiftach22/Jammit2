package com.jammit.navigation

sealed class NavRoute(val route: String) {
    // Authentication
    object Login : NavRoute("login")
    object Register : NavRoute("register")

    // Main tabs
    object Profile : NavRoute("profile")
    object Explore : NavRoute("explore")
    object Chats : NavRoute("chats")

    // Detail screens
    data class UserProfile(val userId: String) : NavRoute("user_profile/{userId}") {
        companion object {
            fun createRoute(userId: String) = "user_profile/$userId"
        }
    }

    data class ChatDetail(val chatId: String) : NavRoute("chat_detail/{chatId}") {
        companion object {
            fun createRoute(chatId: String) = "chat_detail/$chatId"
        }
    }
}

