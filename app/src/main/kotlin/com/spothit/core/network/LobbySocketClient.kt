package com.spothit.core.network

import kotlinx.coroutines.flow.SharedFlow

/**
 * Abstraction for real-time lobby communication.
 *
 * The domain layer owns this contract so presentation components can
 * depend on it without referencing a specific networking technology
 * (WebSocket, polling, etc.).
 */
interface LobbySocketClient {
    /** Stream of socket events emitted by the active lobby connection. */
    val events: SharedFlow<LobbySocketEvent>

    /** Establish a connection to the given lobby. */
    fun connect(lobbyId: String)

    /** Cleanly close the active lobby connection if present. */
    fun disconnect()
}

/**
 * Domain-level representation of lobby socket messages.
 */
sealed class LobbySocketEvent {
    data class PlayerJoined(val lobbyId: String, val playerName: String) : LobbySocketEvent()
    data class PlayerLeft(val lobbyId: String, val playerName: String) : LobbySocketEvent()
    data class StateChanged(val lobbyId: String, val state: String, val playlistName: String?) : LobbySocketEvent()
    data class Message(val lobbyId: String, val from: String, val content: String) : LobbySocketEvent()
    data class Raw(val type: String, val payload: String?) : LobbySocketEvent()
}
