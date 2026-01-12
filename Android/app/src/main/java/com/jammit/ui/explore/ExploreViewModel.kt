package com.jammit.ui.explore

import com.jammit.data.model.Instrument
import com.jammit.data.model.InstrumentWithLevel
import com.jammit.data.model.MusicianLevel
import com.jammit.data.model.User
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

data class ExploreFilters(
    val selectedInstruments: Set<Instrument> = emptySet(),
    val selectedLevel: MusicianLevel? = null,
    val searchRadiusKm: Float = 10f
)

class ExploreViewModel : ViewModel() {
    // Mock nearby users
    private val mockUsers = listOf(
        User(
            id = "user1",
            username = "Alice Johnson",
            instruments = listOf(
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[0], MusicianLevel.ADVANCED), // Guitar
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[4], MusicianLevel.INTERMEDIATE) // Violin
            ),
            latitude = 40.7282,
            longitude = -73.9942
        ),
        User(
            id = "user2",
            username = "Bob Smith",
            instruments = listOf(
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[1], MusicianLevel.PROFESSIONAL), // Piano
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[3], MusicianLevel.ADVANCED) // Bass
            ),
            latitude = 40.7589,
            longitude = -73.9851
        ),
        User(
            id = "user3",
            username = "Charlie Brown",
            instruments = listOf(
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[2], MusicianLevel.BEGINNER) // Drums
            ),
            latitude = 40.6892,
            longitude = -74.0445
        ),
        User(
            id = "user4",
            username = "Diana Prince",
            instruments = listOf(
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[5], MusicianLevel.INTERMEDIATE), // Saxophone
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[6], MusicianLevel.ADVANCED) // Trumpet
            ),
            latitude = 40.7614,
            longitude = -73.9776
        ),
        User(
            id = "user5",
            username = "Eve Wilson",
            instruments = listOf(
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[8], MusicianLevel.PROFESSIONAL), // Voice
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[9], MusicianLevel.BEGINNER) // Keyboard
            ),
            latitude = 40.7489,
            longitude = -73.9680
        )
    )

    private val currentUserLocation = Pair(40.7128, -74.0060) // NYC coordinates

    private val _filters = MutableStateFlow(ExploreFilters())
    val filters: StateFlow<ExploreFilters> = _filters.asStateFlow()

    private val _filteredUsers = MutableStateFlow<List<Pair<User, Double>>>(emptyList())
    val filteredUsers: StateFlow<List<Pair<User, Double>>> = _filteredUsers.asStateFlow()

    init {
        updateFilteredUsers()
    }

    fun updateFilters(filters: ExploreFilters) {
        _filters.value = filters
        updateFilteredUsers()
    }

    private fun updateFilteredUsers() {
        val filtered = mockUsers
            .map { user ->
                val distance = calculateDistance(
                    currentUserLocation.first,
                    currentUserLocation.second,
                    user.latitude,
                    user.longitude
                )
                Pair(user, distance)
            }
            .filter { (user, distance) ->
                // Filter by radius
                distance <= _filters.value.searchRadiusKm &&
                // Filter by instruments
                (_filters.value.selectedInstruments.isEmpty() || 
                 user.instruments.any { it.instrument in _filters.value.selectedInstruments }) &&
                // Filter by level (if specified, at least one instrument must match)
                (_filters.value.selectedLevel == null || 
                 user.instruments.any { it.level == _filters.value.selectedLevel })
            }
            .sortedBy { it.second }
        _filteredUsers.value = filtered
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0
        val lat1Rad = lat1 * PI / 180.0
        val lat2Rad = lat2 * PI / 180.0
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}

