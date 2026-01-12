package com.jammit.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jammit.data.model.Instrument
import com.jammit.data.model.InstrumentWithLevel
import com.jammit.data.model.MusicianLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var editedUsername by remember { mutableStateOf(currentUser.username) }
    var editedInstruments by remember { mutableStateOf(currentUser.instruments.toMutableList()) }
    var showInstrumentDialog by remember { mutableStateOf(false) }
    var editingInstrumentIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(currentUser) {
        editedUsername = currentUser.username
        editedInstruments = currentUser.instruments.toMutableList()
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

        // Username
        OutlinedTextField(
            value = editedUsername,
            onValueChange = { editedUsername = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Instruments
        Text(
            text = "Instruments",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { showInstrumentDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Instrument")
        }
        Spacer(modifier = Modifier.height(8.dp))
        editedInstruments.forEachIndexed { index, instrumentWithLevel ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = instrumentWithLevel.instrument.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = instrumentWithLevel.level.name.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row {
                        TextButton(
                            onClick = { editingInstrumentIndex = index }
                        ) {
                            Text("Edit")
                        }
                        IconButton(
                            onClick = { editedInstruments.removeAt(index) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = {
                viewModel.updateUsername(editedUsername)
                viewModel.updateInstruments(editedInstruments.toList())
                viewModel.saveProfile()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Profile")
            }
        }
    }

    // Instrument Selection Dialog
    if (showInstrumentDialog) {
        InstrumentSelectionDialog(
            existingInstruments = editedInstruments.map { it.instrument }.toSet(),
            onDismiss = { showInstrumentDialog = false },
            onConfirm = { instrument ->
                editedInstruments.add(InstrumentWithLevel(instrument, MusicianLevel.BEGINNER))
                showInstrumentDialog = false
            }
        )
    }

    // Level Selection Dialog
    editingInstrumentIndex?.let { index ->
        LevelSelectionDialog(
            currentLevel = editedInstruments[index].level,
            onDismiss = { editingInstrumentIndex = null },
            onConfirm = { level ->
                editedInstruments[index] = InstrumentWithLevel(
                    editedInstruments[index].instrument,
                    level
                )
                editingInstrumentIndex = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstrumentSelectionDialog(
    existingInstruments: Set<Instrument>,
    onDismiss: () -> Unit,
    onConfirm: (Instrument) -> Unit
) {
    val availableInstruments = Instrument.ALL_INSTRUMENTS.filter { it !in existingInstruments }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Instrument") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (availableInstruments.isEmpty()) {
                    Text("All instruments have been added")
                } else {
                    availableInstruments.forEach { instrument ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onConfirm(instrument) }
                        ) {
                            Text(
                                text = instrument.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LevelSelectionDialog(
    currentLevel: MusicianLevel,
    onDismiss: () -> Unit,
    onConfirm: (MusicianLevel) -> Unit
) {
    var selectedLevel by remember { mutableStateOf(currentLevel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Level") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                MusicianLevel.values().forEach { level ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedLevel == level,
                                onClick = { selectedLevel = level }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLevel == level,
                            onClick = { selectedLevel = level }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = level.name.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedLevel) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

