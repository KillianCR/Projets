package com.preciousmetals.tracker.data.remote.dto

import kotlinx.serialization.Serializable

/** Response from https://api.frankfurter.app/latest (no API key required). */
@Serializable
data class ExchangeRateDto(
    val base: String,
    val date: String,
    val rates: Map<String, Double>,
)
