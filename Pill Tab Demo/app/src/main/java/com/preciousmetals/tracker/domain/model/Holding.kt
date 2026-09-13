package com.preciousmetals.tracker.domain.model

import java.time.LocalDate

/**
 * A single purchased quantity of a precious metal.
 *
 * [pricePaidUsd] is the amount actually paid, converted to and stored in USD, when the user
 * chose to enter it manually. When null, the cost basis is derived from the cached historical
 * spot price at [purchaseDate] (see [com.preciousmetals.tracker.data.repository.PriceRepository]).
 */
data class Holding(
    val id: Long = 0,
    val metal: Metal,
    val objectType: ObjectType,
    val grams: Double,
    val purchaseDate: LocalDate,
    val label: String,
    val pricePaidUsd: Double?,
    val photoUri: String?,
    val notes: String,
    val storageLocationId: Long? = null,
)
