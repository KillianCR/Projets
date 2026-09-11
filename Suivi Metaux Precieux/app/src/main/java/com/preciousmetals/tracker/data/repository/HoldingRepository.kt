package com.preciousmetals.tracker.data.repository

import com.preciousmetals.tracker.data.local.dao.HoldingDao
import com.preciousmetals.tracker.domain.model.Holding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HoldingRepository(private val holdingDao: HoldingDao) {

    fun observeAll(): Flow<List<Holding>> = holdingDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): Holding? = holdingDao.getById(id)?.toDomain()

    suspend fun getAllOnce(): List<Holding> = holdingDao.getAllOnce().map { it.toDomain() }

    suspend fun upsert(holding: Holding): Long = holdingDao.upsert(holding.toEntity())

    suspend fun delete(holding: Holding) = holdingDao.delete(holding.toEntity())
}
