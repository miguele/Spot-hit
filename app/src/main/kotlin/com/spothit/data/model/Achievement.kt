package com.spothit.data.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlockedAtEpochMillis: Long
)
