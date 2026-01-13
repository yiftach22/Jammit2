package com.jammit.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUsernameScreen(
    registrationData: String, // Format: "email|password"
    onNext: (String) -> Unit, // Passes: "email|password|username"
    onBack: () -> Unit,
    viewModel: RegisterUsernameViewModel = viewModel {
        val parts = registrationData.split("|")
        RegisterUsernameViewModel(parts[0], parts[1])
    }
) {
    var username by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val usernameAvailable by viewModel.usernameAvailable.collectAsState()
    
    // Check username availability as user types (debounced)
    LaunchedEffect(username) {
        if (username.length >= 3) {
            kotlinx.coroutines.delay(500) // Debounce
            viewModel.checkUsername(username)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose a Username",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This is how other musicians will see you",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true,
            supportingText = {
                when {
                    username.length < 3 && username.isNotEmpty() -> Text("Username must be at least 3 characters")
                    usernameAvailable == true -> Text("Username is available", color = androidx.compose.ui.graphics.Color(0xFF4CAF50))
                    usernameAvailable == false -> Text("Username is taken", color = MaterialTheme.colorScheme.error)
                    else -> null
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error message
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                viewModel.validateAndProceed(username) { data ->
                    onNext(data)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && username.isNotBlank() && username.length >= 3 && usernameAvailable == true
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Next")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}
