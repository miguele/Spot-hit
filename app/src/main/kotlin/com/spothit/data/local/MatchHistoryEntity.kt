package com.spothit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val opponentName: String,
    val result: String,
    val score: Int,
    val playedAtEpochMillis: Long
)
