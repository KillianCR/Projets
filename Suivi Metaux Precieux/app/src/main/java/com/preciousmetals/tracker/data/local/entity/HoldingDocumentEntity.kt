package com.preciousmetals.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A document (invoice, certificate of authenticity, insurance attestation…) attached to a holding. */
@Entity(tableName = "holding_documents")
data class HoldingDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val holdingId: Long,
    /** Persisted content:// URI (permission taken at add time), as a string. */
    val uri: String,
    val label: String,
    val addedAtEpochMillis: Long,
)
