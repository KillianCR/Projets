package com.preciousmetals.tracker.ui.addholding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preciousmetals.tracker.data.repository.HoldingDocumentRepository
import com.preciousmetals.tracker.data.repository.HoldingRepository
import com.preciousmetals.tracker.data.repository.PriceRepository
import com.preciousmetals.tracker.data.repository.StorageLocationRepository
import com.preciousmetals.tracker.domain.model.Holding
import com.preciousmetals.tracker.domain.model.HoldingDocument
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.domain.model.ObjectType
import com.preciousmetals.tracker.domain.model.StorageLocation
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddEditHoldingViewModel(
    private val holdingId: Long?,
    private val holdingRepository: HoldingRepository,
    private val priceRepository: PriceRepository,
    private val storageLocationRepository: StorageLocationRepository,
    private val holdingDocumentRepository: HoldingDocumentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditHoldingUiState(isEditing = holdingId != null))
    val uiState: StateFlow<AddEditHoldingUiState> = _uiState.asStateFlow()

    val storageLocations: StateFlow<List<StorageLocation>> = storageLocationRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val documents: StateFlow<List<HoldingDocument>> = (
        if (holdingId != null) holdingDocumentRepository.observeForHolding(holdingId) else flowOf(emptyList())
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (holdingId != null) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val holding = holdingRepository.getById(holdingId)
                if (holding != null) {
                    val rate = priceRepository.usdToEurRate.first()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        metal = holding.metal,
                        objectType = holding.objectType,
                        gramsText = trimNumber(holding.grams),
                        purchaseDate = holding.purchaseDate,
                        label = holding.label,
                        notes = holding.notes,
                        photoUri = holding.photoUri,
                        storageLocationId = holding.storageLocationId,
                        valuationMode = if (holding.pricePaidUsd != null) ValuationMode.MANUAL else ValuationMode.AUTO,
                        pricePaidEurText = holding.pricePaidUsd?.let { trimNumber(it * rate) } ?: "",
                    )
                    refreshHistoricalPreview()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Avoir introuvable.")
                }
            }
        }
    }

    private fun trimNumber(value: Double): String {
        val rounded = kotlin.math.round(value * 100) / 100
        return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
    }

    fun setMetal(metal: Metal) {
        _uiState.value = _uiState.value.copy(metal = metal)
        refreshHistoricalPreview()
    }

    fun setObjectType(objectType: ObjectType) {
        _uiState.value = _uiState.value.copy(objectType = objectType)
    }

    fun setGramsText(text: String) {
        _uiState.value = _uiState.value.copy(gramsText = text)
        refreshHistoricalPreview()
    }

    fun setPurchaseDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(purchaseDate = date)
        refreshHistoricalPreview()
    }

    fun setLabel(text: String) {
        _uiState.value = _uiState.value.copy(label = text)
    }

    fun setNotes(text: String) {
        _uiState.value = _uiState.value.copy(notes = text)
    }

    fun setPhotoUri(uri: String?) {
        _uiState.value = _uiState.value.copy(photoUri = uri)
    }

    fun setStorageLocation(id: Long?) {
        _uiState.value = _uiState.value.copy(storageLocationId = id)
    }

    fun addDocument(uri: String, label: String) {
        val id = holdingId ?: return
        viewModelScope.launch {
            holdingDocumentRepository.add(
                HoldingDocument(
                    holdingId = id,
                    uri = uri,
                    label = label,
                    addedAtEpochMillis = System.currentTimeMillis(),
                )
            )
        }
    }

    fun deleteDocument(document: HoldingDocument) {
        viewModelScope.launch { holdingDocumentRepository.delete(document) }
    }

    fun setValuationMode(mode: ValuationMode) {
        _uiState.value = _uiState.value.copy(valuationMode = mode)
        if (mode == ValuationMode.AUTO) refreshHistoricalPreview()
    }

    fun setPricePaidEurText(text: String) {
        _uiState.value = _uiState.value.copy(pricePaidEurText = text)
    }

    private fun refreshHistoricalPreview() {
        val state = _uiState.value
        if (state.valuationMode != ValuationMode.AUTO) return
        val grams = state.gramsText.toDoubleOrNull()
        viewModelScope.launch {
            val pricePerGramUsd = priceRepository.getNearestHistoricalPriceUsdPerGram(state.metal, state.purchaseDate)
            val rate = priceRepository.usdToEurRate.first()
            _uiState.value = _uiState.value.copy(
                historicalPreviewUnavailable = pricePerGramUsd == null,
                historicalPreviewEur = if (pricePerGramUsd != null && grams != null) {
                    pricePerGramUsd * grams * rate
                } else null,
            )
        }
    }

    fun save() {
        val state = _uiState.value
        val grams = state.gramsText.replace(',', '.').toDoubleOrNull()
        if (grams == null || grams <= 0.0) {
            _uiState.value = state.copy(error = "Indiquez un poids en grammes valide.")
            return
        }
        if (state.valuationMode == ValuationMode.MANUAL) {
            val price = state.pricePaidEurText.replace(',', '.').toDoubleOrNull()
            if (price == null || price <= 0.0) {
                _uiState.value = state.copy(
                    error = "Indiquez le prix payé, ou choisissez le calcul automatique."
                )
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            val rate = priceRepository.usdToEurRate.first()
            val pricePaidUsd = if (state.valuationMode == ValuationMode.MANUAL) {
                state.pricePaidEurText.replace(',', '.').toDouble() / rate
            } else {
                null
            }
            val holding = Holding(
                id = holdingId ?: 0,
                metal = state.metal,
                objectType = state.objectType,
                grams = grams,
                purchaseDate = state.purchaseDate,
                label = state.label.ifBlank { "${state.objectType.displayNameFr} ${state.metal.displayNameFr}" },
                pricePaidUsd = pricePaidUsd,
                photoUri = state.photoUri,
                notes = state.notes,
                storageLocationId = state.storageLocationId,
            )
            holdingRepository.upsert(holding)
            _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
        }
    }

    fun delete() {
        val id = holdingId ?: return
        viewModelScope.launch {
            val holding = holdingRepository.getById(id) ?: return@launch
            holdingRepository.delete(holding)
            _uiState.value = _uiState.value.copy(deleted = true)
        }
    }
}
