package com.spothit.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.spothit.core.network.LobbySocketEvent
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebSocketManagerTest {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Test
    fun `parses player join events`() {
        val manager = WebSocketManager(
            okHttpClient = OkHttpClient(),
            webSocketUrl = NetworkConfig.BACKEND_WEBSOCKET_URL,
            tokenProvider = InMemoryTokenProvider(),
            moshi = moshi
        )

        val event = manager.parseMessage(
            """{"type":"player_joined","lobby_id":"abc","player_name":"Sam"}"""
        )

        assertTrue(event is LobbySocketEvent.PlayerJoined)
        event as LobbySocketEvent.PlayerJoined
        assertEquals("abc", event.lobbyId)
        assertEquals("Sam", event.playerName)
    }

    @Test
    fun `falls back to raw event when unknown`() {
        val manager = WebSocketManager(
            okHttpClient = OkHttpClient(),
            webSocketUrl = NetworkConfig.BACKEND_WEBSOCKET_URL,
            tokenProvider = InMemoryTokenProvider(),
            moshi = moshi
        )

        val event = manager.parseMessage("""{"type":"custom","payload":"data"}""")

        assertTrue(event is LobbySocketEvent.Raw)
        event as LobbySocketEvent.Raw
        assertEquals("custom", event.type)
        assertEquals("data", event.payload)
    }
}
