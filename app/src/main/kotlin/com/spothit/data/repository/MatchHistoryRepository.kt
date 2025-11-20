package com.spothit.data.repository

import com.spothit.data.local.MatchHistoryDao
import com.spothit.data.local.MatchHistoryEntity
import com.spothit.data.model.MatchHistory
import com.spothit.data.remote.MatchHistoryRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MatchHistoryRepository(
    private val remoteDataSource: MatchHistoryRemoteDataSource,
    private val matchHistoryDao: MatchHistoryDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun observeHistory(userId: String): Flow<List<MatchHistory>> {
        return matchHistoryDao.observeHistory(userId).map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun syncHistory(userId: String): List<MatchHistory> = withContext(dispatcher) {
        val remoteHistory = remoteDataSource.fetchMatchHistory(userId)
        matchHistoryDao.clearForUser(userId)
        matchHistoryDao.insertAll(remoteHistory.map { it.toEntity() })
        remoteHistory
    }

    suspend fun addMatch(matchHistory: MatchHistory) = withContext(dispatcher) {
        matchHistoryDao.insertAll(listOf(matchHistory.toEntity()))
    }
}

private fun MatchHistoryEntity.toModel(): MatchHistory =
    MatchHistory(
        id = id,
        userId = userId,
        opponentName = opponentName,
        result = result,
        score = score,
        playedAtEpochMillis = playedAtEpochMillis
    )

private fun MatchHistory.toEntity(): MatchHistoryEntity =
    MatchHistoryEntity(
        id = id,
        userId = userId,
        opponentName = opponentName,
        result = result,
        score = score,
        playedAtEpochMillis = playedAtEpochMillis
    )
