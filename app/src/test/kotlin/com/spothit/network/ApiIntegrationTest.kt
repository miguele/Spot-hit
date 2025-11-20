package com.spothit.network

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ApiIntegrationTest {
    private lateinit var server: MockWebServer
    private val client = OkHttpClient()

    @Before
    fun setUp() {
        server = MockWebServer()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `spotify api parses playlists`() = runTest {
        val responseJson = """
            {"items":[{"id":"123","name":"My Playlist","images":[{"url":"https://img"}],"tracks":{"total":10}}],"next":null}
        """
        server.enqueue(MockResponse().setBody(responseJson))

        val retrofit = RetrofitProvider.create(
            baseUrl = server.url("/").toString(),
            okHttpClient = client
        )
        val api = retrofit.create(SpotifyApi::class.java)

        val payload = api.getPlaylists()
        assertEquals(1, payload.items.size)
        assertEquals("My Playlist", payload.items.first().name)
        assertEquals(10, payload.items.first().tracks?.total)
    }

    @Test
    fun `backend api parses lobby response`() = runTest {
        val responseJson = """
            {"id":"1","code":"ABCD","state":"WAITING","players":[{"id":"p1","name":"Player"}],"playlistName":"Mix"}
        """
        server.enqueue(MockResponse().setBody(responseJson))

        val retrofit = RetrofitProvider.create(
            baseUrl = server.url("/").toString(),
            okHttpClient = client
        )
        val api = retrofit.create(BackendApi::class.java)

        val lobby = api.getLobby("ABCD")
        assertEquals("ABCD", lobby.code)
        assertEquals("Player", lobby.players.first().name)
        assertEquals("Mix", lobby.playlistName)
    }
}
