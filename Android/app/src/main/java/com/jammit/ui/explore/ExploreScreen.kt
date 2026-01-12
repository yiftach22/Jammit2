@file:OptIn(ExperimentalMaterial3Api::class)

package com.jammit.ui.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jammit.data.model.Instrument
import com.jammit.data.model.MusicianLevel
import com.jammit.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onUserClick: (String) -> Unit,
    viewModel: ExploreViewModel = viewModel()
) {
    val filteredUsers by viewModel.filteredUsers.collectAsState()
    val filters by viewModel.filters.collectAsState()
    
    var showFilters by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Explore Musicians",
                style = MaterialTheme.typography.headlineMedium
            )
            TextButton(onClick = { showFilters = !showFilters }) {
                Text("Filters")
            }
        }

        // Filters Section
        if (showFilters) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Search Radius
                    Text(
                        text = "Search Radius: ${filters.searchRadiusKm.toInt()} km",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = filters.searchRadiusKm,
                        onValueChange = { radius ->
                            viewModel.updateFilters(filters.copy(searchRadiusKm = radius))
                        },
                        valueRange = 1f..50f,
                        steps = 48
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Musician Level Filter
                    Text(
                        text = "Musician Level",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filters.selectedLevel == null,
                            onClick = { viewModel.updateFilters(filters.copy(selectedLevel = null)) },
                            label = { Text("All") }
                        )
                        MusicianLevel.values().forEach { level ->
                            FilterChip(
                                selected = filters.selectedLevel == level,
                                onClick = { viewModel.updateFilters(filters.copy(selectedLevel = level)) },
                                label = { Text(level.name.take(3)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instrument Filter
                    Text(
                        text = "Instruments",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Instrument.ALL_INSTRUMENTS.forEach { instrument ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = instrument in filters.selectedInstruments,
                                    onClick = {
                                        val newSelection = if (instrument in filters.selectedInstruments) {
                                            filters.selectedInstruments - instrument
                                        } else {
                                            filters.selectedInstruments + instrument
                                        }
                                        viewModel.updateFilters(filters.copy(selectedInstruments = newSelection))
                                    }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = instrument in filters.selectedInstruments,
                                onCheckedChange = { checked ->
                                    val newSelection = if (checked) {
                                        filters.selectedInstruments + instrument
                                    } else {
                                        filters.selectedInstruments - instrument
                                    }
                                    viewModel.updateFilters(filters.copy(selectedInstruments = newSelection))
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = instrument.name)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // User List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredUsers, key = { it.first.id }) { (user, distance) ->
                UserListItem(
                    user = user,
                    distance = distance,
                    onClick = { onUserClick(user.id) }
                )
            }
        }
    }
}

@Composable
fun UserListItem(
    user: User,
    distance: Double,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.instruments.joinToString(", ") { 
                        "${it.instrument.name} (${it.level.name.replaceFirstChar { char -> char.lowercase() }})"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%.1f km away", distance),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

