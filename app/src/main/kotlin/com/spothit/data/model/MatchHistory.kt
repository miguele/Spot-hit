package com.spothit.data.model

data class MatchHistory(
    val id: Long = 0,
    val userId: String,
    val opponentName: String,
    val result: String,
    val score: Int,
    val playedAtEpochMillis: Long
)
