package com.preciousmetals.tracker.data.remote.dto

import kotlinx.serialization.Serializable

/** Response shape of https://query1.finance.yahoo.com/v8/finance/chart/{symbol} (no API key). */
@Serializable
data class YahooChartResponseDto(val chart: YahooChartDto)

@Serializable
data class YahooChartDto(val result: List<YahooChartResultDto>? = null)

@Serializable
data class YahooChartResultDto(
    val meta: YahooChartMetaDto? = null,
    val timestamp: List<Long> = emptyList(),
    val indicators: YahooIndicatorsDto,
)

/**
 * [regularMarketPrice] is Yahoo's own "current best price" for the symbol — populated from the
 * last trade regardless of whether the requested intraday granularity ([YahooQuoteDto.close]) has
 * any bars yet, which is exactly the case outside trading hours (nights, weekends): the 1-minute
 * quote array can come back empty while this still holds the last traded price. [marketState]
 * (e.g. "REGULAR", "CLOSED", "PRE", "POST") says whether that price is live or stale.
 */
@Serializable
data class YahooChartMetaDto(
    val regularMarketPrice: Double? = null,
    val previousClose: Double? = null,
    val chartPreviousClose: Double? = null,
    val marketState: String? = null,
)

@Serializable
data class YahooIndicatorsDto(val quote: List<YahooQuoteDto> = emptyList())

@Serializable
data class YahooQuoteDto(val close: List<Double?> = emptyList())
