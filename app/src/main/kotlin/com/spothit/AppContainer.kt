package com.spothit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spothit.core.repository.GameRepository
import com.spothit.core.repository.InMemoryGameRepository
import com.spothit.core.usecase.CreateGameUseCase
import com.spothit.core.usecase.FinishGameUseCase
import com.spothit.core.usecase.GetSessionUseCase
import com.spothit.core.usecase.JoinGameUseCase
import com.spothit.core.usecase.ResetGameUseCase
import com.spothit.core.usecase.StartRoundUseCase
import com.spothit.core.usecase.SubmitGuessUseCase
import com.spothit.core.usecase.UpdatePlaylistUseCase

class AppContainer(
    private val repository: GameRepository = InMemoryGameRepository()
) {
    val viewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(
                    createGame = CreateGameUseCase(repository),
                    joinGame = JoinGameUseCase(repository),
                    startRound = StartRoundUseCase(repository),
                    submitGuess = SubmitGuessUseCase(repository),
                    finishGame = FinishGameUseCase(repository),
                    resetGame = ResetGameUseCase(repository),
                    updatePlaylist = UpdatePlaylistUseCase(repository),
                    getSession = GetSessionUseCase(repository)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
