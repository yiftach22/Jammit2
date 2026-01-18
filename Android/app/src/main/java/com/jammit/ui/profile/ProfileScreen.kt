package com.jammit.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jammit.data.SessionManager
import com.jammit.ui.instruments.InstrumentChipRow
import com.jammit.ui.instruments.UserInstrument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onEditInstruments: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userId = remember { SessionManager.getUserId(context) }
    if (userId.isNullOrBlank()) {
        // No session -> go to login
        LaunchedEffect(Unit) { onLogout() }
        return
    }
    val profileViewModel = remember(userId) {
        ProfileViewModel(currentUserId = userId)
    }
    val currentUser by profileViewModel.currentUser.collectAsState()
    val isSaving by profileViewModel.isSaving.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()

    if (isLoading && currentUser == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (currentUser == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Failed to load profile")
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Profile Picture
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = currentUser?.username ?: "",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Instruments
        Text(
            text = "Instruments",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        val selected =
            remember(currentUser) {
                currentUser?.instruments?.map { UserInstrument(it.instrument.id, it.level) } ?: emptyList()
            }

        InstrumentChipRow(
            selected = selected,
            catalog = profileViewModel.availableInstruments.collectAsState().value,
            onRemove = {},
            showRemove = false,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onEditInstruments,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && currentUser != null,
        ) {
            Text("Edit")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Error message
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        OutlinedButton(
            onClick = {
                SessionManager.clearSession(context)
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text("Log out")
        }
    }
}
