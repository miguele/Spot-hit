package com.spothit.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.spothit.data.local.SpotHitDatabase
import com.spothit.data.model.Achievement
import com.spothit.data.model.MatchHistory
import com.spothit.data.model.UserProfile
import com.spothit.data.remote.AchievementRemoteDataSource
import com.spothit.data.remote.MatchHistoryRemoteDataSource
import com.spothit.data.remote.ProfileRemoteDataSource
import com.spothit.data.repository.AchievementRepository
import com.spothit.data.repository.MatchHistoryRepository
import com.spothit.data.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RepositoryIntegrationTest {
    private lateinit var database: SpotHitDatabase
    private lateinit var profileRepository: ProfileRepository
    private lateinit var matchHistoryRepository: MatchHistoryRepository
    private lateinit var achievementRepository: AchievementRepository

    private val profileRemoteDataSource = object : ProfileRemoteDataSource {
        override suspend fun fetchProfile(userId: String): UserProfile {
            return UserProfile(
                id = userId,
                displayName = "Remote User",
                avatarUrl = "https://avatar.example.com/$userId",
                bio = "Remote bio",
                lastUpdatedEpochMillis = 100L
            )
        }
    }

    private val matchHistoryRemoteDataSource = object : MatchHistoryRemoteDataSource {
        override suspend fun fetchMatchHistory(userId: String): List<MatchHistory> {
            return listOf(
                MatchHistory(
                    id = 10,
                    userId = userId,
                    opponentName = "Challenger",
                    result = "WIN",
                    score = 15,
                    playedAtEpochMillis = 200L
                )
            )
        }
    }

    private val achievementRemoteDataSource = object : AchievementRemoteDataSource {
        override suspend fun fetchAchievements(userId: String): List<Achievement> {
            return listOf(
                Achievement(
                    id = "achv-remote",
                    title = "Remote Achievement",
                    description = "Fetched from remote",
                    unlockedAtEpochMillis = 150L
                )
            )
        }
    }

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, SpotHitDatabase::class.java)
            .addMigrations(SpotHitDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        profileRepository = ProfileRepository(profileRemoteDataSource, database.userProfileDao())
        matchHistoryRepository = MatchHistoryRepository(matchHistoryRemoteDataSource, database.matchHistoryDao())
        achievementRepository = AchievementRepository(achievementRemoteDataSource, database.achievementDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun refreshProfile_updates_local_cache() = runTest {
        val userId = "user-123"
        profileRepository.refreshProfile(userId)

        val stored = profileRepository.observeProfile(userId).first()
        val expected = profileRemoteDataSource.fetchProfile(userId)
        assertEquals(expected, stored)
    }

    @Test
    fun syncHistory_replaces_local_entries() = runTest {
        val userId = "user-abc"
        matchHistoryRepository.syncHistory(userId)

        val stored = matchHistoryRepository.observeHistory(userId).first()
        assertEquals(matchHistoryRemoteDataSource.fetchMatchHistory(userId), stored)
    }

    @Test
    fun syncAchievements_replaces_local_entries() = runTest {
        val userId = "user-achievements"
        achievementRepository.syncAchievements(userId)

        val stored = achievementRepository.observeAchievements().first()
        assertEquals(achievementRemoteDataSource.fetchAchievements(userId), stored)
    }
}
