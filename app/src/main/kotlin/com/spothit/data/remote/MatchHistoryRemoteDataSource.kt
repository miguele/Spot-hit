package com.spothit.data.remote

import com.spothit.data.model.MatchHistory

interface MatchHistoryRemoteDataSource {
    suspend fun fetchMatchHistory(userId: String): List<MatchHistory>
}
