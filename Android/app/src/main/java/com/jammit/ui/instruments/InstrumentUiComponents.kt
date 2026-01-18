@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.jammit.ui.instruments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jammit.data.model.Instrument
import com.jammit.data.model.MusicianLevel

@Composable
fun InstrumentChipRow(
    selected: List<UserInstrument>,
    catalog: List<Instrument>,
    onRemove: (String) -> Unit,
    showRemove: Boolean = true,
) {
    val byId = remember(catalog) { catalog.associateBy { it.id } }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        selected.forEach { item ->
            val name = byId[item.instrumentId]?.name ?: "Unknown"
            val levelLabel = item.level.name.lowercase().replaceFirstChar { it.uppercase() }

            InputChip(
                selected = false,
                onClick = {},
                label = {
                    Text(
                        "$name \u2022 $levelLabel",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                trailingIcon = if (showRemove) {
                    {
                        // keep remove action but don't use an icon (still minimal)
                        TextButton(
                            onClick = { onRemove(item.instrumentId) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) { Text("×") }
                    }
                } else null,
                colors = InputChipDefaults.inputChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun LevelPickerDialog(
    title: String,
    levels: List<MusicianLevel>,
    onSelect: (MusicianLevel) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf<MusicianLevel?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                levels.forEach { level ->
                    val label = level.name.lowercase().replaceFirstChar { it.uppercase() }

                    Surface(
                        tonalElevation = 0.dp,
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = level }
                            .padding(vertical = 2.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = selected == level,
                                onClick = { selected = level }
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val level = selected ?: return@Button
                    onSelect(level)
                },
                enabled = selected != null
            ) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun UserInstrumentRow(
    instrumentName: String,
    level: MusicianLevel,
    onLevelChange: (MusicianLevel) -> Unit,
    onRemove: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val levelLabel = level.name.lowercase().replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), // more breathing room like mock
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = instrumentName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            // Bigger dropdown field like the mock
            OutlinedTextField(
                value = levelLabel,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .width(150.dp)      // wider than before
                    .height(45.dp) // taller field
                    .menuAnchor(),
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                MusicianLevel.values().forEach { option ->
                    val optionLabel = option.name.lowercase().replaceFirstChar { it.uppercase() }
                    DropdownMenuItem(
                        text = { Text(optionLabel, style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            onLevelChange(option)
                            expanded = false
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // No icons: use small text button like mock minimal style
        TextButton(
            onClick = onRemove,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) { Text("Remove") }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun Preview_UserInstrumentRow() {
    var level by remember { mutableStateOf(MusicianLevel.INTERMEDIATE) }

    MaterialTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                UserInstrumentRow(
                    instrumentName = "Guitar",
                    level = level,
                    onLevelChange = { level = it },
                    onRemove = {},
                )
            }
        }
    }
}

@Composable
fun InstrumentPickerBottomSheet(
    catalog: List<Instrument>,
    selectedIds: Set<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    onPick: (String, MusicianLevel) -> Unit,
    onDismiss: () -> Unit,
) {
    var pendingInstrument by remember { mutableStateOf<Instrument?>(null) }

    val filtered =
        remember(catalog, selectedIds, query) {
            catalog
                .filter { it.id !in selectedIds }
                .filter { it.name.contains(query, ignoreCase = true) }
                .sortedBy { it.name }
        }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text("Add Instruments", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search instruments…") },
            )

            Spacer(modifier = Modifier.height(10.dp))


            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 420.dp),
            ) {
                items(filtered.size) { idx ->
                    val instrument = filtered[idx]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pendingInstrument = instrument }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            instrument.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Button(
                            onClick = { pendingInstrument = instrument },
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text("Add", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    pendingInstrument?.let { inst ->
        LevelPickerDialog(
            title = "Select level for ${inst.name}",
            levels = MusicianLevel.values().toList(),
            onSelect = { level ->
                onPick(inst.id, level)
                pendingInstrument = null // keep sheet open
            },
            onDismiss = { pendingInstrument = null },
        )
    }
}
