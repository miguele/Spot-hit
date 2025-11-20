package com.spothit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        MatchHistoryEntity::class,
        AchievementEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class SpotHitDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun matchHistoryDao(): MatchHistoryDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS achievements(
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        unlockedAtEpochMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
