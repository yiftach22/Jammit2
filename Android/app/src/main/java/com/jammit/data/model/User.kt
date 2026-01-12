package com.jammit.data.model

data class User(
    val id: String,
    val username: String,
    val profilePictureUrl: String? = null,
    val instruments: List<InstrumentWithLevel>,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

