@file:OptIn(ExperimentalMaterial3Api::class)

package com.jammit.ui.instruments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jammit.data.SessionManager
import com.jammit.data.model.Instrument
import com.jammit.data.model.MusicianLevel

@Composable
fun EditInstrumentsScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userId = remember { SessionManager.getUserId(context) }
    if (userId.isNullOrBlank()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("You must be logged in.")
        }
        return
    }

    val vm: EditInstrumentsViewModel =
        viewModel(
            factory =
                object : ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EditInstrumentsViewModel(userId) as T
                    }
                },
        )

    val state by vm.uiState.collectAsState()
    val selectedIds = remember(state.selected) { state.selected.map { it.instrumentId }.toSet() }

    var pendingAddInstrument by remember { mutableStateOf<Instrument?>(null) }

    val unselected =
        remember(state.catalog, selectedIds, state.searchQuery) {
            state.catalog
                .filter { it.id !in selectedIds }
                .filter { it.name.contains(state.searchQuery, ignoreCase = true) }
                .sortedBy { it.name }
        }

    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Instruments", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // match mock: compact filled "Save"
                    Button(
                        onClick = { vm.save(onSaved) },
                        enabled = !state.isSaving && !state.isLoading,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            state.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Your Instruments", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))

            val catalogById = remember(state.catalog) { state.catalog.associateBy { it.id } }

            // match mock: clean list area, subtle container
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    if (state.selected.isEmpty()) {
                        Text(
                            "No instruments yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    } else {
                        state.selected.forEachIndexed { idx, item ->
                            UserInstrumentRow(
                                instrumentName = catalogById[item.instrumentId]?.name ?: "Unknown",
                                level = item.level,
                                onLevelChange = { newLevel -> vm.changeLevel(item.instrumentId, newLevel) },
                                onRemove = { vm.removeInstrument(item.instrumentId) },
                            )
                            if (idx != state.selected.lastIndex) {
                                Divider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Add More", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = vm::updateSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search instruments…") },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // match mock: simple list with right-aligned "Add" button; avoid heavy cards
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                items(unselected, key = { it.id }) { instrument ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            instrument.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Button(
                            onClick = { pendingAddInstrument = instrument },
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text("Add", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }

    pendingAddInstrument?.let { inst ->
        LevelPickerDialog(
            title = "Select level for ${inst.name}",
            levels = MusicianLevel.values().toList(),
            onSelect = { level ->
                vm.addInstrument(inst.id, level)
                pendingAddInstrument = null
            },
            onDismiss = { pendingAddInstrument = null },
        )
    }
}
