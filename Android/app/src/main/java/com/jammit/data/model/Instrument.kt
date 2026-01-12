package com.jammit.data.model

data class Instrument(
    val id: String,
    val name: String
) {
    companion object {
        val ALL_INSTRUMENTS = listOf(
            Instrument("1", "Guitar"),
            Instrument("2", "Piano"),
            Instrument("3", "Drums"),
            Instrument("4", "Bass"),
            Instrument("5", "Violin"),
            Instrument("6", "Saxophone"),
            Instrument("7", "Trumpet"),
            Instrument("8", "Flute"),
            Instrument("9", "Voice"),
            Instrument("10", "Keyboard"),
            Instrument("11", "Cello"),
            Instrument("12", "Clarinet")
        )
    }
}

