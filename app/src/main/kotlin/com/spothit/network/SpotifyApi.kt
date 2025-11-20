package com.spothit.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SpotifyApi {
    @GET("me")
    suspend fun getCurrentUser(): SpotifyUser

    @GET("me/playlists")
    suspend fun getPlaylists(@Query("limit") limit: Int = 20): SpotifyPlaylistPage

    @GET("playlists/{id}/tracks")
    suspend fun getPlaylistTracks(
        @Path("id") playlistId: String,
        @Query("limit") limit: Int = 100
    ): SpotifyTrackPage
}

@JsonClass(generateAdapter = true)
data class SpotifyUser(
    val id: String,
    @Json(name = "display_name") val displayName: String?
)

@JsonClass(generateAdapter = true)
data class SpotifyPlaylistPage(
    val items: List<SpotifyPlaylist> = emptyList(),
    val next: String?
)

@JsonClass(generateAdapter = true)
data class SpotifyPlaylist(
    val id: String,
    val name: String,
    val images: List<SpotifyImage> = emptyList(),
    val tracks: SpotifyTrackRef?
)

@JsonClass(generateAdapter = true)
data class SpotifyTrackRef(@Json(name = "total") val total: Int = 0)

@JsonClass(generateAdapter = true)
data class SpotifyImage(val url: String?, val width: Int?, val height: Int?)

@JsonClass(generateAdapter = true)
data class SpotifyTrackPage(val items: List<SpotifyPlaylistTrack> = emptyList())

@JsonClass(generateAdapter = true)
data class SpotifyPlaylistTrack(@Json(name = "track") val track: SpotifyTrack?)

@JsonClass(generateAdapter = true)
data class SpotifyTrack(
    val id: String?,
    val name: String?,
    val artists: List<SpotifyArtist> = emptyList(),
    val album: SpotifyAlbum?
)

@JsonClass(generateAdapter = true)
data class SpotifyArtist(val id: String?, val name: String?)

@JsonClass(generateAdapter = true)
data class SpotifyAlbum(val id: String?, val name: String?)
