package com.preciousmetals.tracker.data.repository

import com.preciousmetals.tracker.data.local.dao.PriceAlertDao
import com.preciousmetals.tracker.domain.model.PriceAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlertRepository(private val priceAlertDao: PriceAlertDao) {

    fun observeAll(): Flow<List<PriceAlert>> = priceAlertDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getEnabledOnce(): List<PriceAlert> = priceAlertDao.getEnabled().map { it.toDomain() }

    suspend fun upsert(alert: PriceAlert): Long = priceAlertDao.upsert(alert.toEntity())

    suspend fun delete(alert: PriceAlert) = priceAlertDao.delete(alert.toEntity())

    suspend fun markTriggered(alert: PriceAlert, whenEpochMillis: Long) {
        priceAlertDao.update(alert.toEntity().copy(lastTriggeredAtEpochMillis = whenEpochMillis, enabled = false))
    }
}
