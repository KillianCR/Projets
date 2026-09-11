package com.preciousmetals.tracker.data.remote.dto

import kotlinx.serialization.Serializable

/** Response from https://api.gold-api.com/price/{symbol} (no API key required). */
@Serializable
data class SpotPriceDto(
    val symbol: String,
    val price: Double,
    val updatedAt: String,
)
