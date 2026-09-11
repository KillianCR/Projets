package com.preciousmetals.tracker.data.remote

import com.preciousmetals.tracker.data.remote.dto.SpotPriceDto
import retrofit2.http.GET
import retrofit2.http.Path

/** Free, keyless live spot-price feed for gold, silver, platinum and palladium. */
interface GoldApiService {
    @GET("price/{symbol}")
    suspend fun getSpotPrice(@Path("symbol") symbol: String): SpotPriceDto

    companion object {
        const val BASE_URL = "https://api.gold-api.com/"
    }
}
