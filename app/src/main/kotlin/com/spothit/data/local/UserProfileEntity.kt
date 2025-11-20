package com.spothit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val avatarUrl: String?,
    val bio: String?,
    val lastUpdatedEpochMillis: Long
)
