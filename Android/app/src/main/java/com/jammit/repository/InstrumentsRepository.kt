package com.jammit.repository

import com.jammit.data.model.Instrument
import com.jammit.network.ApiService

class InstrumentsRepository(private val apiService: ApiService) {
    suspend fun getInstruments(): Result<List<Instrument>> {
        return try {
            val response = apiService.getInstruments()
            if (response.isSuccessful) {
                val instruments = response.body()?.map {
                    Instrument(it.id, it.name)
                } ?: emptyList()
                Result.success(instruments)
            } else {
                Result.failure(Exception("Failed to fetch instruments: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInstrument(id: String): Result<Instrument> {
        return try {
            val response = apiService.getInstrument(id)
            if (response.isSuccessful && response.body() != null) {
                val instrumentResponse = response.body()!!
                Result.success(Instrument(instrumentResponse.id, instrumentResponse.name))
            } else {
                Result.failure(Exception("Instrument not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
