package com.preciousmetals.tracker.data.repository

import com.preciousmetals.tracker.data.local.dao.GoalDao
import com.preciousmetals.tracker.domain.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepository(private val dao: GoalDao) {
    fun observeAll(): Flow<List<Goal>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun add(goal: Goal): Long = dao.insert(goal.toEntity())

    suspend fun delete(goal: Goal) = dao.delete(goal.toEntity())
}
