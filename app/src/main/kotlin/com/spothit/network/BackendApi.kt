package com.spothit.network

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BackendApi {
    @GET("api/lobbies/{code}")
    suspend fun getLobby(@Path("code") code: String): LobbyResponse

    @POST("api/lobbies")
    suspend fun createLobby(@Body request: CreateLobbyRequest): LobbyResponse

    @POST("api/lobbies/{code}/join")
    suspend fun joinLobby(
        @Path("code") code: String,
        @Body request: JoinLobbyRequest
    ): LobbyResponse
}

@JsonClass(generateAdapter = true)
data class CreateLobbyRequest(val hostName: String, val rounds: Int)

@JsonClass(generateAdapter = true)
data class JoinLobbyRequest(val playerName: String)

@JsonClass(generateAdapter = true)
data class LobbyResponse(
    val id: String,
    val code: String,
    val state: String,
    val players: List<PlayerResponse> = emptyList(),
    val playlistName: String? = null
)

@JsonClass(generateAdapter = true)
data class PlayerResponse(val id: String, val name: String)
