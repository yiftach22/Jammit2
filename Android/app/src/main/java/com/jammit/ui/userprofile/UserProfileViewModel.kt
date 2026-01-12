package com.jammit.ui.userprofile

import com.jammit.data.model.Instrument
import com.jammit.data.model.InstrumentWithLevel
import com.jammit.data.model.MusicianLevel
import com.jammit.data.model.User
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

class UserProfileViewModel(private val userId: String) : ViewModel() {
    private val currentUserLocation = Pair(40.7128, -74.0060) // NYC coordinates

    // Mock user data - in a real app, this would fetch from a repository
    private val mockUsers = mapOf(
        "user1" to User(
            id = "user1",
            username = "Alice Johnson",
            instruments = listOf(
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[0], MusicianLevel.ADVANCED), // Guitar
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[4], MusicianLevel.INTERMEDIATE) // Violin
            ),
            latitude = 40.7282,
            longitude = -73.9942
        ),
        "user2" to User(
            id = "user2",
            username = "Bob Smith",
            instruments = listOf(
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[1], MusicianLevel.PROFESSIONAL), // Piano
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[3], MusicianLevel.ADVANCED) // Bass
            ),
            latitude = 40.7589,
            longitude = -73.9851
        ),
        "user3" to User(
            id = "user3",
            username = "Charlie Brown",
            instruments = listOf(
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[2], MusicianLevel.BEGINNER) // Drums
            ),
            latitude = 40.6892,
            longitude = -74.0445
        ),
        "user4" to User(
            id = "user4",
            username = "Diana Prince",
            instruments = listOf(
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[5], MusicianLevel.INTERMEDIATE), // Saxophone
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[6], MusicianLevel.ADVANCED) // Trumpet
            ),
            latitude = 40.7614,
            longitude = -73.9776
        ),
        "user5" to User(
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

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _distance = MutableStateFlow<Double?>(null)
    val distance: StateFlow<Double?> = _distance.asStateFlow()

    init {
        _user.value = mockUsers[userId]
        _user.value?.let { user ->
            _distance.value = calculateDistance(
                currentUserLocation.first,
                currentUserLocation.second,
                user.latitude,
                user.longitude
            )
        }
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

