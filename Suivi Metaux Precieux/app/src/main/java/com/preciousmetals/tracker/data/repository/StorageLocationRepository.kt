package com.preciousmetals.tracker.data.repository

import com.preciousmetals.tracker.data.local.dao.StorageLocationDao
import com.preciousmetals.tracker.domain.model.StorageLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StorageLocationRepository(private val dao: StorageLocationDao) {
    fun observeAll(): Flow<List<StorageLocation>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun upsert(location: StorageLocation): Long = dao.upsert(location.toEntity())

    suspend fun delete(location: StorageLocation) = dao.delete(location.toEntity())
}
