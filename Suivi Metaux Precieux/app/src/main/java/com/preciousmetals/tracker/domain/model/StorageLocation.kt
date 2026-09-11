package com.preciousmetals.tracker.domain.model

import java.time.LocalDate

data class StorageLocation(
    val id: Long = 0,
    val name: String,
    val notes: String,
    val insuranceReminderDate: LocalDate?,
)
