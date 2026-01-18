@file:OptIn(ExperimentalMaterial3Api::class)

package com.jammit.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jammit.ui.instruments.InstrumentChipRow
import com.jammit.ui.instruments.InstrumentPickerBottomSheet

@Composable
fun RegisterInstrumentsScreen(
    registrationData: String, // Format: "email|password|username"
    onComplete: (String) -> Unit, // Passes userId
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val parts = remember(registrationData) { registrationData.split("|") }
    val viewModel: RegisterInstrumentsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RegisterInstrumentsViewModel(parts[0], parts[1], parts[2], context) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(text = "Your Instruments", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select at least one instrument and your level for each.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.openPicker() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
        ) {
            Text("Add Instruments")
        }

        Spacer(modifier = Modifier.height(12.dp))

        InstrumentChipRow(
            selected = uiState.selected,
            catalog = uiState.catalog,
            onRemove = { id -> viewModel.removeInstrument(id) },
            showRemove = true,
        )

        if (uiState.showValidationError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please select at least 1 instrument to continue.",
                color = MaterialTheme.colorScheme.error,
            )
        }

        uiState.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.completeRegistration(onComplete) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Complete Registration")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }

    if (uiState.showPicker) {
        InstrumentPickerBottomSheet(
            catalog = uiState.catalog,
            selectedIds = uiState.selected.map { it.instrumentId }.toSet(),
            query = uiState.searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onPick = { instrumentId, level ->
                viewModel.addInstrument(instrumentId, level)
            },
            onDismiss = { viewModel.closePicker() },
        )
    }
}
