package com.spothit.data.repository

import com.spothit.data.local.UserProfileDao
import com.spothit.data.local.UserProfileEntity
import com.spothit.data.model.UserProfile
import com.spothit.data.remote.ProfileRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProfileRepository(
    private val remoteDataSource: ProfileRemoteDataSource,
    private val userProfileDao: UserProfileDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun observeProfile(userId: String): Flow<UserProfile?> {
        return userProfileDao.observeProfile(userId).map { entity -> entity?.toModel() }
    }

    suspend fun refreshProfile(userId: String): UserProfile = withContext(dispatcher) {
        val profile = remoteDataSource.fetchProfile(userId)
        userProfileDao.upsert(profile.toEntity())
        profile
    }

    suspend fun saveProfile(profile: UserProfile) = withContext(dispatcher) {
        userProfileDao.upsert(profile.toEntity())
    }
}

private fun UserProfileEntity.toModel(): UserProfile =
    UserProfile(
        id = id,
        displayName = displayName,
        avatarUrl = avatarUrl,
        bio = bio,
        lastUpdatedEpochMillis = lastUpdatedEpochMillis
    )

private fun UserProfile.toEntity(): UserProfileEntity =
    UserProfileEntity(
        id = id,
        displayName = displayName,
        avatarUrl = avatarUrl,
        bio = bio,
        lastUpdatedEpochMillis = lastUpdatedEpochMillis
    )
