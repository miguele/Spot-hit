package com.spothit.network

import androidx.annotation.VisibleForTesting
import com.spothit.core.network.LobbySocketClient
import com.spothit.core.network.LobbySocketEvent
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlin.math.min

class WebSocketManager(
    private val okHttpClient: OkHttpClient,
    private val webSocketUrl: String,
    private val tokenProvider: TokenProvider,
    moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : LobbySocketClient {
    private val messageAdapter = moshi.adapter(RawSocketMessage::class.java)
    private val _events = MutableSharedFlow<LobbySocketEvent>(extraBufferCapacity = 32)
    override val events: SharedFlow<LobbySocketEvent> = _events.asSharedFlow()

    private val reconnectionDelaysMs = listOf(0L, 1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 30_000L)
    private var reconnectAttempt = 0
    private var activeLobbyId: String? = null
    private var webSocket: WebSocket? = null

    override fun connect(lobbyId: String) {
        activeLobbyId = lobbyId
        reconnectAttempt = 0
        openSocket(lobbyId)
    }

    override fun disconnect() {
        activeLobbyId = null
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
    }

    private fun openSocket(lobbyId: String) {
        val requestBuilder = Request.Builder()
            .url("${webSocketUrl.trimEnd('/')}/$lobbyId")

        tokenProvider.accessToken()?.takeIf { it.isNotBlank() }
            ?.let { requestBuilder.addHeader("Authorization", "Bearer $it") }

        webSocket = okHttpClient.newWebSocket(requestBuilder.build(), createListener(lobbyId))
    }

    private fun createListener(lobbyId: String) = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            reconnectAttempt = 0
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            parseMessage(text)?.let { _events.tryEmit(it) }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (activeLobbyId == lobbyId) scheduleReconnect(lobbyId)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (activeLobbyId == lobbyId) scheduleReconnect(lobbyId)
        }
    }

    private fun scheduleReconnect(lobbyId: String) {
        val delayMs = reconnectionDelaysMs[min(reconnectAttempt, reconnectionDelaysMs.lastIndex)]
        reconnectAttempt++
        scope.launch {
            delay(delayMs)
            if (activeLobbyId == lobbyId) {
                openSocket(lobbyId)
            }
        }
    }

    @VisibleForTesting
    internal fun parseMessage(text: String): LobbySocketEvent? {
        val raw = runCatching { messageAdapter.fromJson(text) }.getOrNull() ?: return null
        return when (raw.type) {
            "player_joined" -> LobbySocketEvent.PlayerJoined(
                lobbyId = raw.lobbyId.orEmpty(),
                playerName = raw.playerName.orEmpty()
            )

            "player_left" -> LobbySocketEvent.PlayerLeft(
                lobbyId = raw.lobbyId.orEmpty(),
                playerName = raw.playerName.orEmpty()
            )

            "state_changed" -> LobbySocketEvent.StateChanged(
                lobbyId = raw.lobbyId.orEmpty(),
                state = raw.state.orEmpty(),
                playlistName = raw.playlistName
            )

            "message" -> LobbySocketEvent.Message(
                lobbyId = raw.lobbyId.orEmpty(),
                from = raw.playerName.orEmpty(),
                content = raw.payload.orEmpty()
            )

            else -> LobbySocketEvent.Raw(raw.type.orEmpty(), raw.payload)
        }
    }
}

@JsonClass(generateAdapter = true)
data class RawSocketMessage(
    val type: String?,
    @Json(name = "lobby_id") val lobbyId: String?,
    @Json(name = "player_name") val playerName: String?,
    val state: String?,
    @Json(name = "playlist_name") val playlistName: String?,
    val payload: String?
)
