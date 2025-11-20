package com.spothit.data.model

data class UserProfile(
    val id: String,
    val displayName: String,
    val avatarUrl: String?,
    val bio: String?,
    val lastUpdatedEpochMillis: Long
)
