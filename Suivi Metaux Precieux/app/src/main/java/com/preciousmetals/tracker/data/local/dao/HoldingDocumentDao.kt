package com.preciousmetals.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.preciousmetals.tracker.data.local.entity.HoldingDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HoldingDocumentDao {
    @Query("SELECT * FROM holding_documents WHERE holdingId = :holdingId ORDER BY addedAtEpochMillis DESC")
    fun observeForHolding(holdingId: Long): Flow<List<HoldingDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: HoldingDocumentEntity): Long

    @Delete
    suspend fun delete(document: HoldingDocumentEntity)
}
