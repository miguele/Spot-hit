package com.spothit.core.usecase

import com.spothit.core.model.GameMode
import com.spothit.core.model.GameSession
import com.spothit.core.model.Playlist
import com.spothit.core.model.Player
import com.spothit.core.model.PlayerRole
import com.spothit.core.repository.GameRepository

class CreateGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(host: Player, totalRounds: Int, mode: GameMode): GameSession =
        repository.createGame(host, totalRounds, mode)
}

class JoinGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(
        player: Player,
        lobbyCode: String,
        role: PlayerRole = PlayerRole.GUEST
    ): GameSession = repository.joinGame(player, lobbyCode, role)
}

class UpdatePlaylistUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(playlist: Playlist): GameSession = repository.updatePlaylist(playlist)
}

class StartRoundUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(): GameSession = repository.startNextRound()
}

class SubmitGuessUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(playerId: String, yearGuess: Int): GameSession =
        repository.submitGuess(playerId, yearGuess)
}

class FinishGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(): GameSession = repository.finishGame()
}

class ResetGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(): GameSession = repository.reset()
}

class GetSessionUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(): GameSession = repository.currentSession()
}
