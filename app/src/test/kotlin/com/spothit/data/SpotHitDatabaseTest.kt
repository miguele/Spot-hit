package com.spothit.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.spothit.data.local.AchievementDao
import com.spothit.data.local.MatchHistoryDao
import com.spothit.data.local.SpotHitDatabase
import com.spothit.data.local.UserProfileDao
import com.spothit.data.local.UserProfileEntity
import com.spothit.data.local.MatchHistoryEntity
import com.spothit.data.local.AchievementEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SpotHitDatabaseTest {
    private lateinit var database: SpotHitDatabase
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var matchHistoryDao: MatchHistoryDao
    private lateinit var achievementDao: AchievementDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, SpotHitDatabase::class.java)
            .addMigrations(SpotHitDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()
        userProfileDao = database.userProfileDao()
        matchHistoryDao = database.matchHistoryDao()
        achievementDao = database.achievementDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun userProfile_upsert_and_observe_succeeds() = runTest {
        val profile = UserProfileEntity(
            id = "user-1",
            displayName = "Test User",
            avatarUrl = null,
            bio = "Bio",
            lastUpdatedEpochMillis = 42L
        )

        userProfileDao.upsert(profile)

        val stored = userProfileDao.observeProfile(profile.id).first()
        assertEquals(profile, stored)
    }

    @Test
    fun matchHistory_insert_and_ordered_observation() = runTest {
        val userId = "user-1"
        val older = MatchHistoryEntity(
            id = 1,
            userId = userId,
            opponentName = "Opponent A",
            result = "WIN",
            score = 10,
            playedAtEpochMillis = 1L
        )
        val newer = MatchHistoryEntity(
            id = 2,
            userId = userId,
            opponentName = "Opponent B",
            result = "LOSS",
            score = 5,
            playedAtEpochMillis = 2L
        )

        matchHistoryDao.insertAll(listOf(older, newer))

        val history = matchHistoryDao.observeHistory(userId).first()
        assertEquals(listOf(newer, older), history)
    }

    @Test
    fun achievements_clear_and_insert_all() = runTest {
        val first = AchievementEntity(
            id = "achv-1",
            title = "First",
            description = "First achievement",
            unlockedAtEpochMillis = 10L
        )
        val second = AchievementEntity(
            id = "achv-2",
            title = "Second",
            description = "Second achievement",
            unlockedAtEpochMillis = 20L
        )

        achievementDao.insertAll(listOf(first, second))
        val stored = achievementDao.observeAchievements().first()
        assertEquals(listOf(second, first), stored)

        achievementDao.clear()
        val cleared = achievementDao.observeAchievements().first()
        assertTrue(cleared.isEmpty())
    }
}
