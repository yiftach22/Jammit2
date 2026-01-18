@file:OptIn(ExperimentalMaterial3Api::class)

package com.jammit.ui.explore

import android.Manifest
import android.content.Intent
import android.location.LocationManager
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.jammit.data.model.Instrument
import com.jammit.data.model.MusicianLevel
import com.jammit.data.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onUserClick: (String) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userId = remember { com.jammit.data.SessionManager.getUserId(context) }
    if (userId.isNullOrBlank()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("You must be logged in to explore.")
        }
        return
    }

    val exploreViewModel = remember(userId) { ExploreViewModel(currentUserId = userId) }
    val filteredUsers by exploreViewModel.filteredUsers.collectAsState()
    val filters by exploreViewModel.filters.collectAsState()
    val isLoading by exploreViewModel.isLoading.collectAsState()
    val error by exploreViewModel.error.collectAsState()
    val availableInstruments by exploreViewModel.availableInstruments.collectAsState()
    
    var showFilters by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember {
        mutableStateOf(
            hasLocationPermission(context),
        )
    }
    var requestedOnce by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isLocationEnabled by remember { mutableStateOf(isSystemLocationEnabled(context)) }

    val openLocationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        // Re-check after returning from Settings
        isLocationEnabled = isSystemLocationEnabled(context)
        if (!isLocationEnabled) {
            locationError = "Location is turned off. Please enable it to explore nearby musicians."
        } else {
            locationError = null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted =
            (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        locationPermissionGranted = granted
        locationError =
            if (granted) null else "Location permission is required to explore nearby musicians."
    }

    // Ask for permission when entering Explore
    LaunchedEffect(Unit) {
        isLocationEnabled = isSystemLocationEnabled(context)
        if (!locationPermissionGranted) {
            // Small delay to avoid launching before Activity is ready (prevents some device crashes)
            delay(150)
            if (!requestedOnce) {
                requestedOnce = true
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            }
        }
    }

    // Once permission is granted, fetch & save location (one-shot on entry)
    LaunchedEffect(locationPermissionGranted) {
        if (!locationPermissionGranted) return@LaunchedEffect
        isLocationEnabled = isSystemLocationEnabled(context)
        if (!isLocationEnabled) {
            locationError = "Location is turned off. Please enable it to explore nearby musicians."
            return@LaunchedEffect
        }
        val loc = withTimeoutOrNull(6000) { getCurrentLocationOrNull(context) }
        if (loc == null) {
            locationError = "Couldn't get your location. Please try again."
        } else {
            exploreViewModel.updateMyLocation(loc.first, loc.second)
        }
    }

    if (!locationPermissionGranted) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp),
            ) {
                Text(
                    text = "Location permission is needed to explore nearby musicians.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    },
                ) {
                    Text("Allow location")
                }
            }
        }
        return
    }

    if (!isLocationEnabled) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp),
            ) {
                Text(
                    text = "Location is turned off. Please enable it to explore nearby musicians.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        openLocationSettingsLauncher.launch(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        )
                    },
                ) {
                    Text("Turn on location")
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        locationError?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(it, color = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                ),
                            )
                        },
                    ) {
                        Text("Try again")
                    }
                }
            }
        }

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
                            exploreViewModel.updateFilters(filters.copy(searchRadiusKm = radius))
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
                                onClick = { exploreViewModel.updateFilters(filters.copy(selectedLevel = null)) },
                            label = { Text("All") }
                        )
                        MusicianLevel.values().forEach { level ->
                            FilterChip(
                                selected = filters.selectedLevel == level,
                                    onClick = { exploreViewModel.updateFilters(filters.copy(selectedLevel = level)) },
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
                    availableInstruments.forEach { instrument ->
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
                                        exploreViewModel.updateFilters(filters.copy(selectedInstruments = newSelection))
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
                                    exploreViewModel.updateFilters(filters.copy(selectedInstruments = newSelection))
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

        // Loading/Error/User List
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error loading users")
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            else -> {
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
    }
}

private fun hasLocationPermission(context: android.content.Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
}

private fun isSystemLocationEnabled(context: android.content.Context): Boolean {
    val lm = context.getSystemService<LocationManager>() ?: return false
    return try {
        lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    } catch (e: Throwable) {
        false
    }
}

private suspend fun getCurrentLocationOrNull(context: android.content.Context): Pair<Double, Double>? {
    val client =
        try {
            LocationServices.getFusedLocationProviderClient(context)
        } catch (e: Throwable) {
            return null
        }

    // Try last known location first
    val last =
        try {
            suspendCancellableCoroutine<android.location.Location?> { cont ->
                client.lastLocation
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
            }
        } catch (e: SecurityException) {
            null
        } catch (e: Throwable) {
            null
        }
    if (last != null) return Pair(last.latitude, last.longitude)

    // Fallback to current location
    val cts = CancellationTokenSource()
    val current =
        try {
            suspendCancellableCoroutine<android.location.Location?> { cont ->
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
            }
        } catch (e: SecurityException) {
            null
        } catch (e: Throwable) {
            null
        }
    return current?.let { Pair(it.latitude, it.longitude) }
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

