package com.preciousmetals.tracker.ui.addholding

import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.ObjectType
import java.time.LocalDate

enum class ValuationMode { MANUAL, AUTO }

data class AddEditHoldingUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val metal: Metal = Metal.GOLD,
    val objectType: ObjectType = ObjectType.BAR,
    val gramsText: String = "",
    val purchaseDate: LocalDate = LocalDate.now(),
    val label: String = "",
    val notes: String = "",
    val photoUri: String? = null,
    val storageLocationId: Long? = null,
    val valuationMode: ValuationMode = ValuationMode.MANUAL,
    /** Price paid, entered in EUR. */
    val pricePaidEurText: String = "",
    /** Preview of the auto-computed cost basis (EUR), null while unknown/unavailable. */
    val historicalPreviewEur: Double? = null,
    val historicalPreviewUnavailable: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val deleted: Boolean = false,
)
