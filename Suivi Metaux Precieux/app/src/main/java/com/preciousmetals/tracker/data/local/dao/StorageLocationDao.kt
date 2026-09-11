package com.preciousmetals.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.preciousmetals.tracker.data.local.entity.StorageLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageLocationDao {
    @Query("SELECT * FROM storage_locations ORDER BY name")
    fun observeAll(): Flow<List<StorageLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(location: StorageLocationEntity): Long

    @Update
    suspend fun update(location: StorageLocationEntity)

    @Delete
    suspend fun delete(location: StorageLocationEntity)
}
