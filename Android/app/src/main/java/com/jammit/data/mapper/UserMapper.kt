package com.jammit.data.mapper

import com.jammit.data.model.*
import com.jammit.network.*

object UserMapper {
    fun toUser(userResponse: UserResponse): User {
        return User(
            id = userResponse.id,
            username = userResponse.username,
            profilePictureUrl = userResponse.profilePictureUrl,
            instruments = userResponse.instruments.map { iwl ->
                InstrumentWithLevel(
                    instrument = Instrument(iwl.instrument.id, iwl.instrument.name),
                    level = MusicianLevel.valueOf(iwl.level)
                )
            },
            latitude = userResponse.latitude ?: 0.0,
            longitude = userResponse.longitude ?: 0.0
        )
    }

    fun toCreateUserRequest(user: User): CreateUserRequest {
        return CreateUserRequest(
            username = user.username,
            email = null, // Email is handled separately during auth
            instruments = user.instruments.map { iwl ->
                InstrumentWithLevelRequest(
                    instrumentId = iwl.instrument.id,
                    level = iwl.level.name
                )
            },
            latitude = user.latitude,
            longitude = user.longitude
        )
    }

    fun toUpdateUserRequest(user: User): UpdateUserRequest {
        return UpdateUserRequest(
            username = user.username,
            instruments = user.instruments.map { iwl ->
                InstrumentWithLevelRequest(
                    instrumentId = iwl.instrument.id,
                    level = iwl.level.name
                )
            },
            latitude = user.latitude,
            longitude = user.longitude
        )
    }
}
