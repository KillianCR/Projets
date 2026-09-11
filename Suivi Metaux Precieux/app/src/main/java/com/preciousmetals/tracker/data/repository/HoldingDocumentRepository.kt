package com.preciousmetals.tracker.data.repository

import com.preciousmetals.tracker.data.local.dao.HoldingDocumentDao
import com.preciousmetals.tracker.domain.model.HoldingDocument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HoldingDocumentRepository(private val dao: HoldingDocumentDao) {
    fun observeForHolding(holdingId: Long): Flow<List<HoldingDocument>> =
        dao.observeForHolding(holdingId).map { list -> list.map { it.toDomain() } }

    suspend fun add(document: HoldingDocument): Long = dao.insert(document.toEntity())

    suspend fun delete(document: HoldingDocument) = dao.delete(document.toEntity())
}
