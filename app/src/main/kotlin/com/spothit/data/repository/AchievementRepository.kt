package com.spothit.data.repository

import com.spothit.data.local.AchievementDao
import com.spothit.data.local.AchievementEntity
import com.spothit.data.model.Achievement
import com.spothit.data.remote.AchievementRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AchievementRepository(
    private val remoteDataSource: AchievementRemoteDataSource,
    private val achievementDao: AchievementDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun observeAchievements(): Flow<List<Achievement>> {
        return achievementDao.observeAchievements().map { achievements ->
            achievements.map { it.toModel() }
        }
    }

    suspend fun syncAchievements(userId: String): List<Achievement> = withContext(dispatcher) {
        val achievements = remoteDataSource.fetchAchievements(userId)
        achievementDao.clear()
        achievementDao.insertAll(achievements.map { it.toEntity() })
        achievements
    }
}

private fun AchievementEntity.toModel(): Achievement =
    Achievement(
        id = id,
        title = title,
        description = description,
        unlockedAtEpochMillis = unlockedAtEpochMillis
    )

private fun Achievement.toEntity(): AchievementEntity =
    AchievementEntity(
        id = id,
        title = title,
        description = description,
        unlockedAtEpochMillis = unlockedAtEpochMillis
    )
