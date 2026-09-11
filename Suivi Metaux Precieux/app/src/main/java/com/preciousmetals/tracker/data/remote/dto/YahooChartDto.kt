package com.preciousmetals.tracker.data.remote.dto

import kotlinx.serialization.Serializable

/** Response shape of https://query1.finance.yahoo.com/v8/finance/chart/{symbol} (no API key). */
@Serializable
data class YahooChartResponseDto(val chart: YahooChartDto)

@Serializable
data class YahooChartDto(val result: List<YahooChartResultDto>? = null)

@Serializable
data class YahooChartResultDto(
    val timestamp: List<Long> = emptyList(),
    val indicators: YahooIndicatorsDto,
)

@Serializable
data class YahooIndicatorsDto(val quote: List<YahooQuoteDto> = emptyList())

@Serializable
data class YahooQuoteDto(val close: List<Double?> = emptyList())
