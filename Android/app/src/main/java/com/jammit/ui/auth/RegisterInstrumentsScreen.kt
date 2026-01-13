@file:OptIn(ExperimentalMaterial3Api::class)

package com.jammit.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jammit.data.model.Instrument
import com.jammit.data.model.InstrumentWithLevel
import com.jammit.data.model.MusicianLevel

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
    val availableInstruments by viewModel.availableInstruments.collectAsState()
    val instrumentRows by viewModel.instrumentRows.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Add Your Instruments",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tell us what instruments you play and your skill level",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Instrument rows
        instrumentRows.forEachIndexed { index, row ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Instrument dropdown
                    var expandedInstrument by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = expandedInstrument,
                            onExpandedChange = { expandedInstrument = it }
                        ) {
                            OutlinedTextField(
                                value = row.instrument?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInstrument) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                placeholder = { Text("Instrument") },
                                singleLine = true,
                                label = { Text("Instrument") }
                            )
                            ExposedDropdownMenu(
                                expanded = expandedInstrument,
                                onDismissRequest = { expandedInstrument = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (availableInstruments.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                "No instruments available",
                                                style = MaterialTheme.typography.bodyMedium
                                            ) 
                                        },
                                        onClick = {},
                                        enabled = false
                                    )
                                } else {
                                    availableInstruments.forEach { instrument ->
                                        DropdownMenuItem(
                                            text = { 
                                                Text(
                                                    instrument.name,
                                                    style = MaterialTheme.typography.bodyLarge
                                                ) 
                                            },
                                            onClick = {
                                                viewModel.updateInstrument(index, instrument)
                                                expandedInstrument = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Level dropdown
                    var expandedLevel by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = expandedLevel,
                            onExpandedChange = { expandedLevel = it }
                        ) {
                            OutlinedTextField(
                                value = row.level?.name?.replaceFirstChar { it.uppercase() } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLevel) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                placeholder = { Text("Level") },
                                enabled = row.instrument != null,
                                singleLine = true,
                                label = { Text("Level") }
                            )
                            ExposedDropdownMenu(
                                expanded = expandedLevel,
                                onDismissRequest = { expandedLevel = false }
                            ) {
                                MusicianLevel.values().forEach { level ->
                                    DropdownMenuItem(
                                        text = { Text(level.name.replaceFirstChar { it.uppercase() }) },
                                        onClick = {
                                            viewModel.updateLevel(index, level)
                                            expandedLevel = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Remove button
                    IconButton(
                        onClick = { viewModel.removeRow(index) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Add button
        Button(
            onClick = { viewModel.addRow() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Instrument",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Instrument")
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

        // Complete button
        // Enabled if all rows are either completely empty or completely filled
        val isValid = instrumentRows.all { row ->
            (row.instrument == null && row.level == null) || 
            (row.instrument != null && row.level != null)
        }
        
            Button(
                onClick = {
                    viewModel.completeRegistration { userId ->
                        onComplete(userId)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && isValid
            ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Complete Registration")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
