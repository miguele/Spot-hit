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
    private val createGame: CreateGameUseCase,
    private val joinGame: JoinGameUseCase,
    private val startRound: StartRoundUseCase,
    private val submitGuess: SubmitGuessUseCase,
    private val finishGame: FinishGameUseCase,
    private val resetGame: ResetGameUseCase,
    private val updatePlaylist: UpdatePlaylistUseCase,
    private val getSession: GetSessionUseCase
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
                val session = createGame(host, totalRounds, mode)
                _uiState.value = _uiState.value.copy(session = session)
            }
        }
    }

    fun joinSession(playerName: String) {
        viewModelScope.launch {
            runAction {
                val player = Player(id = playerName.lowercase(), name = playerName)
                val session = joinGame(player)
                _uiState.value = _uiState.value.copy(session = session)
            }
        }
    }

    fun selectPlaylist(playlist: Playlist) {
        viewModelScope.launch { runAction { _uiState.value = _uiState.value.copy(session = updatePlaylist(playlist)) } }
    }

    fun startRound() {
        viewModelScope.launch { runAction { _uiState.value = _uiState.value.copy(session = startRound()) } }
    }

    fun submitGuess(playerId: String, yearGuess: Int) {
        viewModelScope.launch { runAction { _uiState.value = _uiState.value.copy(session = submitGuess(playerId, yearGuess)) } }
    }

    fun completeGame() {
        viewModelScope.launch { runAction { _uiState.value = _uiState.value.copy(session = finishGame()) } }
    }

    fun reset() {
        viewModelScope.launch { runAction { _uiState.value = _uiState.value.copy(session = resetGame()) } }
    }

    private suspend fun refreshSession() {
        _uiState.value = _uiState.value.copy(session = getSession())
    }

    private suspend fun runAction(block: suspend () -> Unit) {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            block()
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
