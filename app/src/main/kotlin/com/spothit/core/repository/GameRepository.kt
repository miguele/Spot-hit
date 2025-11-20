package com.spothit.core.repository

import com.spothit.core.model.GameMode
import com.spothit.core.model.GameSession
import com.spothit.core.model.Playlist
import com.spothit.core.model.Player
import com.spothit.core.model.PlayerRole

interface GameRepository {
    suspend fun createGame(host: Player, totalRounds: Int, mode: GameMode): GameSession
    suspend fun joinGame(player: Player, lobbyCode: String, role: PlayerRole): GameSession
    suspend fun updatePlaylist(playlist: Playlist): GameSession
    suspend fun startNextRound(): GameSession
    suspend fun submitGuess(playerId: String, yearGuess: Int): GameSession
    suspend fun finishGame(): GameSession
    suspend fun reset(): GameSession
    suspend fun currentSession(): GameSession
}
