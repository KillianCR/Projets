package com.preciousmetals.tracker.data.repository

import com.preciousmetals.tracker.data.local.entity.GoalEntity
import com.preciousmetals.tracker.data.local.entity.HoldingDocumentEntity
import com.preciousmetals.tracker.data.local.entity.HoldingEntity
import com.preciousmetals.tracker.data.local.entity.PriceAlertEntity
import com.preciousmetals.tracker.data.local.entity.StorageLocationEntity
import com.preciousmetals.tracker.domain.model.AlertDirection
import com.preciousmetals.tracker.domain.model.Currency
import com.preciousmetals.tracker.domain.model.Goal
import com.preciousmetals.tracker.domain.model.GoalTargetType
import com.preciousmetals.tracker.domain.model.Holding
import com.preciousmetals.tracker.domain.model.HoldingDocument
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.ObjectType
import com.preciousmetals.tracker.domain.model.PriceAlert
import com.preciousmetals.tracker.domain.model.StorageLocation
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
    storageLocationId = storageLocationId,
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
    storageLocationId = storageLocationId,
)

fun StorageLocationEntity.toDomain(): StorageLocation = StorageLocation(
    id = id,
    name = name,
    notes = notes,
    insuranceReminderDate = insuranceReminderEpochDay?.let { LocalDate.ofEpochDay(it) },
)

fun StorageLocation.toEntity(): StorageLocationEntity = StorageLocationEntity(
    id = id,
    name = name,
    notes = notes,
    insuranceReminderEpochDay = insuranceReminderDate?.toEpochDay(),
)

fun HoldingDocumentEntity.toDomain(): HoldingDocument = HoldingDocument(
    id = id,
    holdingId = holdingId,
    uri = uri,
    label = label,
    addedAtEpochMillis = addedAtEpochMillis,
)

fun HoldingDocument.toEntity(): HoldingDocumentEntity = HoldingDocumentEntity(
    id = id,
    holdingId = holdingId,
    uri = uri,
    label = label,
    addedAtEpochMillis = addedAtEpochMillis,
)

fun GoalEntity.toDomain(): Goal = Goal(
    id = id,
    label = label,
    metal = metal?.let { Metal.valueOf(it) },
    targetType = GoalTargetType.valueOf(targetType),
    targetAmount = targetAmount,
    createdAtEpochMillis = createdAtEpochMillis,
)

fun Goal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    label = label,
    metal = metal?.name,
    targetType = targetType.name,
    targetAmount = targetAmount,
    createdAtEpochMillis = createdAtEpochMillis,
)

fun PriceAlertEntity.toDomain(): PriceAlert = PriceAlert(
    id = id,
    metal = Metal.valueOf(metal),
    direction = AlertDirection.valueOf(direction),
    thresholdUsdPerGram = thresholdUsdPerGram,
    currency = Currency.valueOf(currency),
    enabled = enabled,
    createdAtEpochMillis = createdAtEpochMillis,
    lastTriggeredAtEpochMillis = lastTriggeredAtEpochMillis,
)

fun PriceAlert.toEntity(): PriceAlertEntity = PriceAlertEntity(
    id = id,
    metal = metal.name,
    direction = direction.name,
    thresholdUsdPerGram = thresholdUsdPerGram,
    currency = currency.name,
    enabled = enabled,
    createdAtEpochMillis = createdAtEpochMillis,
    lastTriggeredAtEpochMillis = lastTriggeredAtEpochMillis,
)
