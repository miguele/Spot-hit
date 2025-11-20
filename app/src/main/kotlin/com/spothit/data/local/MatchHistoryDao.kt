package com.spothit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchHistoryDao {
    @Query("SELECT * FROM match_history WHERE userId = :userId ORDER BY playedAtEpochMillis DESC")
    fun observeHistory(userId: String): Flow<List<MatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(history: List<MatchHistoryEntity>): List<Long>

    @Query("DELETE FROM match_history WHERE userId = :userId")
    suspend fun clearForUser(userId: String)
}
