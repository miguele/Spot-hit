package com.spothit.data.remote

import com.spothit.data.model.Achievement

interface AchievementRemoteDataSource {
    suspend fun fetchAchievements(userId: String): List<Achievement>
}
