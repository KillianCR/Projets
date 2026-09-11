package com.preciousmetals.tracker.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Builds the Retrofit clients used by [com.preciousmetals.tracker.data.repository.PriceRepository]. */
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            // Yahoo Finance's public chart endpoint rejects requests without a browser-like
            // User-Agent (HTTP 429); harmless to send to the other, simpler feeds too.
            val request = chain.request().newBuilder()
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/120.0.0.0 Mobile Safari/537.36",
                )
                .build()
            chain.proceed(request)
        }
        .addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        )
        .build()

    private val jsonConverterFactory = json.asConverterFactory("application/json".toMediaType())

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient())
        .addConverterFactory(jsonConverterFactory)
        .build()

    val goldApiService: GoldApiService by lazy {
        retrofit(GoldApiService.BASE_URL).create()
    }

    val exchangeRateApiService: ExchangeRateApiService by lazy {
        retrofit(ExchangeRateApiService.BASE_URL).create()
    }

    val yahooFinanceApiService: YahooFinanceApiService by lazy {
        retrofit(YahooFinanceApiService.BASE_URL).create()
    }
}
