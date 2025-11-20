package com.spothit.core.model

enum class GameMode { 
    GUESS_THE_YEAR
}

enum class Placement { 
    CORRECT, 
    INCORRECT, 
    PENDING
}

data class Player(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val isPremium: Boolean = false
)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int,
    val albumArtUrl: String? = null,
    val previewUrl: String? = null,
    val uri: String
)

data class Playlist(
    val id: String,
    val name: String,
    val coverUrl: String?,
    val trackCount: Int
)

data class TimelineSong(
    val song: Song,
    val placement: Placement = Placement.PENDING
)

enum class TurnState { 
    GUESSING, 
    REVEALED
}

enum class SessionState { 
    WAITING, 
    IN_PROGRESS, 
    FINISHED
}

data class GameSession(
    val code: String,
    val host: Player,
    val players: List<Player>,
    val playlist: Playlist?,
    val mode: GameMode,
    val currentRound: Int,
    val totalRounds: Int,
    val scores: Map<String, Int>,
    val state: SessionState,
    val currentSong: Song?,
    val timeline: List<TimelineSong>,
    val songs: List<Song>,
    val turnState: TurnState,
    val lastGuessResult: String?,
    val turnStartTime: Long?
)
