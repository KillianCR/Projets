package com.preciousmetals.tracker.data.repository

import com.preciousmetals.tracker.data.local.entity.HoldingEntity
import com.preciousmetals.tracker.data.local.entity.PriceAlertEntity
import com.preciousmetals.tracker.domain.model.AlertDirection
import com.preciousmetals.tracker.domain.model.Holding
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.ObjectType
import com.preciousmetals.tracker.domain.model.PriceAlert
import java.time.LocalDate

fun HoldingEntity.toDomain(): Holding = Holding(
    id = id,
    metal = Metal.valueOf(metal),
    objectType = ObjectType.valueOf(objectType),
    grams = grams,
    purchaseDate = LocalDate.ofEpochDay(purchaseDateEpochDay),
    label = label,
    pricePaidUsd = pricePaidUsd,
    photoUri = photoUri,
    notes = notes,
)

fun Holding.toEntity(): HoldingEntity = HoldingEntity(
    id = id,
    metal = metal.name,
    objectType = objectType.name,
    grams = grams,
    purchaseDateEpochDay = purchaseDate.toEpochDay(),
    label = label,
    pricePaidUsd = pricePaidUsd,
    photoUri = photoUri,
    notes = notes,
)

fun PriceAlertEntity.toDomain(): PriceAlert = PriceAlert(
    id = id,
    metal = Metal.valueOf(metal),
    direction = AlertDirection.valueOf(direction),
    thresholdUsdPerGram = thresholdUsdPerGram,
    enabled = enabled,
    createdAtEpochMillis = createdAtEpochMillis,
    lastTriggeredAtEpochMillis = lastTriggeredAtEpochMillis,
)

fun PriceAlert.toEntity(): PriceAlertEntity = PriceAlertEntity(
    id = id,
    metal = metal.name,
    direction = direction.name,
    thresholdUsdPerGram = thresholdUsdPerGram,
    enabled = enabled,
    createdAtEpochMillis = createdAtEpochMillis,
    lastTriggeredAtEpochMillis = lastTriggeredAtEpochMillis,
)
