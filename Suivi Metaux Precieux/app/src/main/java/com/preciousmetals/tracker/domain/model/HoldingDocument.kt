package com.preciousmetals.tracker.domain.model

data class HoldingDocument(
    val id: Long = 0,
    val holdingId: Long,
    val uri: String,
    val label: String,
    val addedAtEpochMillis: Long,
)
