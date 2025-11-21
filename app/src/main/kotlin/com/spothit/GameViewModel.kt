package com.spothit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spothit.auth.SpotifyAuthManager
import com.spothit.auth.TokenStorage
import com.spothit.core.model.GameMode
import com.spothit.core.model.GameSession
import com.spothit.core.model.Playlist
import com.spothit.core.model.Player
import com.spothit.core.model.PlayerRole
import com.spothit.core.model.SessionState
import com.spothit.core.auth.AuthRedirectResult
import com.spothit.core.auth.AuthorizationRequest
import com.spothit.core.auth.PkceParameters
import com.spothit.core.usecase.CreateGameUseCase
import com.spothit.core.usecase.FinishGameUseCase
import com.spothit.core.usecase.GetSessionUseCase
import com.spothit.core.usecase.JoinGameUseCase
import com.spothit.core.usecase.ResetGameUseCase
import com.spothit.core.usecase.StartRoundUseCase
import com.spothit.core.usecase.SubmitGuessUseCase
import com.spothit.core.usecase.UpdatePlaylistUseCase
import com.spothit.auth.SessionProvider
import com.spothit.network.InMemoryTokenProvider
import com.spothit.network.SpotifyApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.IOException

class GameViewModel(
    private val createGameUseCase: CreateGameUseCase,
    private val joinGameUseCase: JoinGameUseCase,
    private val startRoundUseCase: StartRoundUseCase,
    private val submitGuessUseCase: SubmitGuessUseCase,
    private val finishGameUseCase: FinishGameUseCase,
    private val resetGameUseCase: ResetGameUseCase,
    private val updatePlaylistUseCase: UpdatePlaylistUseCase,
    private val getSessionUseCase: GetSessionUseCase,
    private val spotifyAuthManager: SpotifyAuthManager,
    private val sessionProvider: SessionProvider,
    private val spotifyApi: SpotifyApi,
    private val tokenStorage: TokenStorage,
    private val tokenProvider: InMemoryTokenProvider,
    private val pendingAuthStorage: PendingAuthStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var pendingPkce: PkceParameters? = null
    private var pendingCreation: CreationParams? = null

    init {
        restorePendingAuthState()
        viewModelScope.launch { refreshSession() }
        viewModelScope.launch {
            tokenStorage.tokensFlow.collectLatest { tokens ->
                tokenProvider.update(tokens?.accessToken)
                _uiState.value = _uiState.value.copy(hasValidAccessToken = tokens?.isExpired() == false)
            }
        }
    }

    fun createSession(hostName: String, totalRounds: Int, mode: GameMode = GameMode.GUESS_THE_YEAR, avatarUrl: String? = null) {
        viewModelScope.launch {
            runAction(markLobbyActive = true) {
                val host = Player(id = hostName.lowercase(), name = hostName, avatarUrl = avatarUrl, role = PlayerRole.HOST)
                createGameUseCase(host, totalRounds, mode)
            }
        }
    }

    fun joinSession(playerName: String, lobbyCode: String, role: PlayerRole = PlayerRole.GUEST, avatarUrl: String? = null) {
        viewModelScope.launch {
            runAction(markLobbyActive = true) {
                val player = Player(id = playerName.lowercase(), name = playerName, avatarUrl = avatarUrl, role = role)
                joinGameUseCase(player, lobbyCode, role)
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

    fun startAuthorization(hostName: String, totalRounds: Int, avatarUrl: String?) {
        val creationParams = CreationParams(hostName.trim(), totalRounds, avatarUrl)
        pendingCreation = creationParams
        val request = spotifyAuthManager.createAuthorizationIntent(SPOTIFY_SCOPES)
        pendingPkce = request.pkceParameters
        pendingAuthStorage.save(request.pkceParameters, creationParams)
        _uiState.value = _uiState.value.copy(
            authorizationRequest = request,
            authError = null,
            playlistError = null,
            showPlaylistSelection = false,
            playlists = emptyList(),
            selectedPlaylist = null,
            creationCompleted = false
        )
    }

    fun onAuthorizationRequestLaunched() {
        _uiState.value = _uiState.value.copy(authorizationRequest = null)
    }

    fun handleAuthRedirect(result: AuthRedirectResult) {
        val pkce = pendingPkce
        if (pkce == null) {
            _uiState.value = _uiState.value.copy(authError = "No hay solicitud de autenticación en curso")
            pendingAuthStorage.clear()
            return
        }

        if (result.state != pkce.state) {
            _uiState.value = _uiState.value.copy(authError = "Estado de autenticación no coincide")
            clearPendingAuthStorage()
            return
        }

        if (result.error != null) {
            _uiState.value = _uiState.value.copy(authError = "Autorización cancelada o fallida: ${result.error}")
            clearPendingAuthStorage()
            return
        }

        val code = result.code ?: run {
            _uiState.value = _uiState.value.copy(authError = "Código de autorización ausente")
            clearPendingAuthStorage()
            return
        }

        viewModelScope.launch { exchangeCodeForTokens(code, pkce) }
    }

    fun retryPlaylistLoad() {
        viewModelScope.launch { loadPlaylists() }
    }

    fun selectPlaylistForPreview(playlist: Playlist) {
        _uiState.value = _uiState.value.copy(selectedPlaylist = playlist)
    }

    fun selectPlaylistAndCreate(playlist: Playlist) {
        _uiState.value = _uiState.value.copy(selectedPlaylist = playlist)
        val creationParams = pendingCreation ?: return
        viewModelScope.launch {
            runAction(markLobbyActive = true) {
                updatePlaylistUseCase(playlist)
                val host = Player(
                    id = creationParams.hostName.lowercase(),
                    name = creationParams.hostName,
                    avatarUrl = creationParams.avatarUrl,
                    role = PlayerRole.HOST
                )
                createGameUseCase(host, creationParams.totalRounds, GameMode.GUESS_THE_YEAR)
            }
            pendingCreation = null
            _uiState.value = _uiState.value.copy(
                showPlaylistSelection = false,
                creationCompleted = true
            )
        }
    }

    fun consumeCreationCompleted() {
        _uiState.value = _uiState.value.copy(creationCompleted = false)
    }

    private suspend fun exchangeCodeForTokens(code: String, pkce: PkceParameters) {
        _uiState.value = _uiState.value.copy(isAuthorizing = true, authError = null)
        val tokens = spotifyAuthManager.exchangeCodeForTokens(code, pkce)
        if (tokens == null) {
            _uiState.value = _uiState.value.copy(isAuthorizing = false, authError = "No se pudo obtener tokens")
            return
        }
        tokenProvider.update(tokens.accessToken)
        pendingPkce = null
        pendingAuthStorage.clear()
        _uiState.value = _uiState.value.copy(isAuthorizing = false, hasValidAccessToken = true)
        loadPlaylists()
    }

    private suspend fun loadPlaylists() {
        _uiState.value = _uiState.value.copy(isLoadingPlaylists = true, playlistError = null)
        val token = sessionProvider.getAccessToken()
        if (token.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoadingPlaylists = false,
                playlistError = "Necesitas iniciar sesión en Spotify para continuar",
                showPlaylistSelection = false,
                hasValidAccessToken = false
            )
            return
        }

        tokenProvider.update(token)

        try {
            val playlists = withContext(Dispatchers.IO) { spotifyApi.getPlaylists().items }
                .map { apiPlaylist ->
                    Playlist(
                        id = apiPlaylist.id,
                        name = apiPlaylist.name,
                        coverUrl = apiPlaylist.images?.firstOrNull()?.url,
                        trackCount = apiPlaylist.tracks?.total ?: 0
                    )
                }
            _uiState.value = _uiState.value.copy(
                playlists = playlists,
                isLoadingPlaylists = false,
                showPlaylistSelection = true,
                hasValidAccessToken = true
            )
        } catch (io: IOException) {
            _uiState.value = _uiState.value.copy(
                playlistError = "No se pudieron cargar las playlists. Verifica tu conexión.",
                isLoadingPlaylists = false,
                showPlaylistSelection = true
            )
        } catch (t: Throwable) {
            _uiState.value = _uiState.value.copy(
                playlistError = "Error al cargar playlists: ${t.message}",
                isLoadingPlaylists = false,
                showPlaylistSelection = true
            )
        }
    }

    private suspend fun refreshSession() {
        _uiState.value = _uiState.value.copy(session = getSessionUseCase())
    }

    private fun restorePendingAuthState() {
        pendingPkce = pendingAuthStorage.getPkce()
        pendingCreation = pendingAuthStorage.getCreationParams()
    }

    private fun clearPendingAuthStorage() {
        pendingPkce = null
        pendingCreation = null
        pendingAuthStorage.clear()
    }

    private suspend fun runAction(markLobbyActive: Boolean = false, block: suspend () -> GameSession?) {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val session = block()
            val isLobbyActive = when {
                markLobbyActive && session?.state == SessionState.WAITING -> true
                session?.state != SessionState.WAITING -> false
                else -> _uiState.value.isLobbyActive
            }
            _uiState.value = _uiState.value.copy(session = session, isLobbyActive = isLobbyActive)
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
    val error: String? = null,
    val authorizationRequest: AuthorizationRequest? = null,
    val isAuthorizing: Boolean = false,
    val authError: String? = null,
    val playlists: List<Playlist> = emptyList(),
    val isLoadingPlaylists: Boolean = false,
    val playlistError: String? = null,
    val showPlaylistSelection: Boolean = false,
    val selectedPlaylist: Playlist? = null,
    val hasValidAccessToken: Boolean = false,
    val creationCompleted: Boolean = false,
    val isLobbyActive: Boolean = false
) {
    val isLobby: Boolean get() = session?.state == SessionState.WAITING
    val isPlaying: Boolean get() = session?.state == SessionState.IN_PROGRESS
    val isFinished: Boolean get() = session?.state == SessionState.FINISHED
}

data class CreationParams(val hostName: String, val totalRounds: Int, val avatarUrl: String?)

private val SPOTIFY_SCOPES = listOf(
    "playlist-read-private",
    "playlist-read-collaborative",
    "user-read-private"
)
