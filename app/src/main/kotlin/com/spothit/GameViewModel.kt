package com.spothit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spothit.core.model.GameMode
import com.spothit.core.model.GameSession
import com.spothit.core.model.Playlist
import com.spothit.core.model.Player
import com.spothit.core.model.SessionState
import com.spothit.core.usecase.CreateGameUseCase
import com.spothit.core.usecase.FinishGameUseCase
import com.spothit.core.usecase.GetSessionUseCase
import com.spothit.core.usecase.JoinGameUseCase
import com.spothit.core.usecase.ResetGameUseCase
import com.spothit.core.usecase.StartRoundUseCase
import com.spothit.core.usecase.SubmitGuessUseCase
import com.spothit.core.usecase.UpdatePlaylistUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val createGameUseCase: CreateGameUseCase,
    private val joinGameUseCase: JoinGameUseCase,
    private val startRoundUseCase: StartRoundUseCase,
    private val submitGuessUseCase: SubmitGuessUseCase,
    private val finishGameUseCase: FinishGameUseCase,
    private val resetGameUseCase: ResetGameUseCase,
    private val updatePlaylistUseCase: UpdatePlaylistUseCase,
    private val getSessionUseCase: GetSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { refreshSession() }
    }

    fun createSession(hostName: String, totalRounds: Int, mode: GameMode = GameMode.GUESS_THE_YEAR) {
        viewModelScope.launch {
            runAction {
                val host = Player(id = hostName.lowercase(), name = hostName)
                createGameUseCase(host, totalRounds, mode)
            }
        }
    }

    fun joinSession(playerName: String) {
        viewModelScope.launch {
            runAction {
                val player = Player(id = playerName.lowercase(), name = playerName)
                joinGameUseCase(player)
            }
        }
    }

    fun selectPlaylist(playlist: Playlist) {
        viewModelScope.launch { runAction { updatePlaylistUseCase(playlist) } }
    }

    fun startRound() {
        viewModelScope.launch { runAction { startRoundUseCase() } }
    }

    fun submitGuess(playerId: String, yearGuess: Int) {
        viewModelScope.launch { runAction { submitGuessUseCase(playerId, yearGuess) } }
    }

    fun completeGame() {
        viewModelScope.launch { runAction { finishGameUseCase() } }
    }

    fun reset() {
        viewModelScope.launch { runAction { resetGameUseCase() } }
    }

    private suspend fun refreshSession() {
        _uiState.value = _uiState.value.copy(session = getSessionUseCase())
    }

    private suspend fun runAction(block: suspend () -> GameSession?) {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val session = block()
            _uiState.value = _uiState.value.copy(session = session)
        } catch (throwable: Throwable) {
            _uiState.value = _uiState.value.copy(error = throwable.message)
        } finally {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}

data class GameUiState(
    val session: GameSession? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isLobby: Boolean get() = session?.state == SessionState.WAITING
    val isPlaying: Boolean get() = session?.state == SessionState.IN_PROGRESS
    val isFinished: Boolean get() = session?.state == SessionState.FINISHED
}
