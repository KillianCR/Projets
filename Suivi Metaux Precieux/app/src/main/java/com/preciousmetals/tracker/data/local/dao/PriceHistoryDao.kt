package com.preciousmetals.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.preciousmetals.tracker.data.local.entity.PriceHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PriceHistoryEntity): Long

    @Query(
        "SELECT * FROM price_history WHERE metal = :metal " +
            "ORDER BY timestampEpochMillis DESC LIMIT 1"
    )
    fun observeLatest(metal: String): Flow<PriceHistoryEntity?>

    @Query(
        "SELECT * FROM price_history WHERE metal = :metal " +
            "ORDER BY timestampEpochMillis DESC LIMIT 1"
    )
    suspend fun getLatestOnce(metal: String): PriceHistoryEntity?

    @Query(
        "SELECT * FROM price_history WHERE metal = :metal AND dateEpochDay >= :sinceEpochDay " +
            "ORDER BY timestampEpochMillis ASC"
    )
    fun observeHistorySince(metal: String, sinceEpochDay: Long): Flow<List<PriceHistoryEntity>>

    /** Nearest cached sample at or before [dateEpochDay], used to value a holding bought in the past. */
    @Query(
        "SELECT * FROM price_history WHERE metal = :metal AND dateEpochDay <= :dateEpochDay " +
            "ORDER BY dateEpochDay DESC LIMIT 1"
    )
    suspend fun getNearestOnOrBefore(metal: String, dateEpochDay: Long): PriceHistoryEntity?

    @Query("DELETE FROM price_history WHERE timestampEpochMillis < :beforeEpochMillis")
    suspend fun deleteOlderThan(beforeEpochMillis: Long)
}
