package com.preciousmetals.tracker.data.remote

import com.preciousmetals.tracker.data.remote.dto.YahooChartResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Free, keyless historical daily-close prices for gold/silver/platinum/palladium futures
 * (used as a proxy for spot price). This is Yahoo Finance's public, undocumented chart
 * endpoint — widely relied on by open-source finance tools, no signup required — used only
 * to backfill [com.preciousmetals.tracker.data.local.entity.PriceHistoryEntity] once so that
 * "calcul automatique" can value purchases made before this app was installed.
 */
interface YahooFinanceApiService {
    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol") symbol: String,
        @Query("range") range: String = "5y",
        @Query("interval") interval: String = "1d",
    ): YahooChartResponseDto

    companion object {
        const val BASE_URL = "https://query1.finance.yahoo.com/"
    }
}
