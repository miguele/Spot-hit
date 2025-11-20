package com.spothit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spothit.BuildConfig
import com.spothit.core.network.LobbySocketClient
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
import com.spothit.network.BackendApi
import com.spothit.network.InMemoryTokenProvider
import com.spothit.network.NetworkConfig
import com.spothit.network.OkHttpProvider
import com.spothit.network.RetrofitProvider
import com.spothit.network.SpotifyApi
import com.spothit.network.TokenProvider
import com.spothit.network.WebSocketManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient

/**
 * Simple dependency container that wires domain interfaces to their
 * data-layer implementations. Presentation code consumes the exposed
 * interfaces (e.g., repositories, socket client) while concrete
 * network/auth/storage classes remain injectable for testability.
 *
 * Dependency direction: presentation -> domain interfaces -> data
 * implementations. Avoid passing concrete network/DB types back up to
 * the UI to keep boundaries clear.
 */
class AppContainer(
    private val repository: GameRepository = InMemoryGameRepository(),
    val tokenProvider: TokenProvider = InMemoryTokenProvider(),
    moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
    private val okHttpClient: OkHttpClient = OkHttpProvider.create(tokenProvider, BuildConfig.DEBUG)
) {
    private val converterFactory = RetrofitProvider.moshiConverterFactory(moshi)

    val spotifyApi: SpotifyApi = RetrofitProvider.create(
        baseUrl = NetworkConfig.SPOTIFY_BASE_URL,
        okHttpClient = okHttpClient,
        converterFactory = converterFactory
    ).create(SpotifyApi::class.java)

    val backendApi: BackendApi = RetrofitProvider.create(
        baseUrl = NetworkConfig.BACKEND_BASE_URL,
        okHttpClient = okHttpClient,
        converterFactory = converterFactory
    ).create(BackendApi::class.java)

    val lobbySocketClient: LobbySocketClient = WebSocketManager(
        okHttpClient = okHttpClient,
        webSocketUrl = NetworkConfig.BACKEND_WEBSOCKET_URL,
        tokenProvider = tokenProvider,
        moshi = moshi
    )

    val viewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(
                    createGameUseCase = CreateGameUseCase(repository),
                    joinGameUseCase = JoinGameUseCase(repository),
                    startRoundUseCase = StartRoundUseCase(repository),
                    submitGuessUseCase = SubmitGuessUseCase(repository),
                    finishGameUseCase = FinishGameUseCase(repository),
                    resetGameUseCase = ResetGameUseCase(repository),
                    updatePlaylistUseCase = UpdatePlaylistUseCase(repository),
                    getSessionUseCase = GetSessionUseCase(repository)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
