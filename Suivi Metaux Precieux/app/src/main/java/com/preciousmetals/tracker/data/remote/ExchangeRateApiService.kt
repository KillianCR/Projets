package com.preciousmetals.tracker.data.remote

import com.preciousmetals.tracker.data.remote.dto.ExchangeRateDto
import retrofit2.http.GET
import retrofit2.http.Query

/** Free, keyless USD/EUR conversion feed (European Central Bank reference rates). */
interface ExchangeRateApiService {
    @GET("latest")
    suspend fun getLatest(
        @Query("from") from: String = "USD",
        @Query("to") to: String = "EUR",
    ): ExchangeRateDto

    companion object {
        const val BASE_URL = "https://api.frankfurter.app/"
    }
}
