package com.jammit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jammit.data.SessionManager
import com.jammit.navigation.MainNavigation
import com.jammit.navigation.NavRoute
import com.jammit.ui.theme.JammitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startDestination =
            if (
                SessionManager.getToken(this).isNullOrBlank().not() &&
                    SessionManager.getUserId(this).isNullOrBlank().not()
            ) {
                NavRoute.Profile.route
            } else {
                NavRoute.Login.route
            }

        setContent {
            JammitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(startDestination = startDestination)
                }
            }
        }
    }
}

