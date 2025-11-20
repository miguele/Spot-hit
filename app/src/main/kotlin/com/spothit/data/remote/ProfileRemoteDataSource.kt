package com.spothit.data.remote

import com.spothit.data.model.UserProfile

interface ProfileRemoteDataSource {
    suspend fun fetchProfile(userId: String): UserProfile
}
