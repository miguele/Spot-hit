package com.spothit.core.repository

import com.spothit.core.model.GameMode
import com.spothit.core.model.GameSession
import com.spothit.core.model.Playlist
import com.spothit.core.model.Player
import com.spothit.core.model.Placement
import com.spothit.core.model.Song
import com.spothit.core.model.SessionState
import com.spothit.core.model.TimelineSong
import com.spothit.core.model.TurnState
import kotlinx.coroutines.delay
import java.util.UUID

class InMemoryGameRepository : GameRepository {

    private var session: GameSession = createInitialSession()

    override suspend fun createGame(host: Player, totalRounds: Int, mode: GameMode): GameSession {
        val selectedPlaylist = session.playlist
        session = createInitialSession(selectedPlaylist).copy(
            code = generateGameCode(),
            host = host,
            players = listOf(host),
            totalRounds = totalRounds,
            mode = mode,
            state = SessionState.WAITING
        )
        return session
    }

    override suspend fun joinGame(player: Player): GameSession {
        session = session.copy(players = session.players + player)
        return session
    }

    override suspend fun updatePlaylist(playlist: Playlist): GameSession {
        session = session.copy(playlist = playlist)
        return session
    }

    override suspend fun startNextRound(): GameSession {
        val nextSong = session.songs.getOrNull(session.currentRound)
        val updatedSession = if (nextSong != null) {
            session.copy(
                currentRound = session.currentRound + 1,
                currentSong = nextSong,
                turnState = TurnState.GUESSING,
                lastGuessResult = null,
                timeline = session.timeline + TimelineSong(nextSong, Placement.PENDING),
                state = SessionState.IN_PROGRESS,
                turnStartTime = System.currentTimeMillis()
            )
        } else {
            session.copy(state = SessionState.FINISHED, turnState = TurnState.REVEALED)
        }

        session = updatedSession
        return session
    }

    override suspend fun submitGuess(playerId: String, yearGuess: Int): GameSession {
        val current = session.currentSong ?: return session
        delay(150) // simulate latency
        val wasCorrect = current.year == yearGuess
        val updatedScore = session.scores[playerId]?.let { it + if (wasCorrect) 10 else 0 } ?: 0
        val scores = session.scores + (playerId to updatedScore)
        val updatedTimeline = session.timeline.map { item ->
            if (item.song.id == current.id) item.copy(placement = if (wasCorrect) Placement.CORRECT else Placement.INCORRECT) else item
        }

        session = session.copy(
            scores = scores,
            lastGuessResult = if (wasCorrect) "¡Acierto!" else "Sigue intentándolo",
            turnState = TurnState.REVEALED,
            timeline = updatedTimeline
        )
        return session
    }

    override suspend fun finishGame(): GameSession {
        session = session.copy(state = SessionState.FINISHED, turnState = TurnState.REVEALED)
        return session
    }

    override suspend fun reset(): GameSession {
        session = createInitialSession()
        return session
    }

    override suspend fun currentSession(): GameSession = session

    private fun createInitialSession(playlist: Playlist? = null): GameSession {
        val host = Player(id = UUID.randomUUID().toString(), name = "Anfitrión")
        val songs = bootstrapSongs()
        val selectedPlaylist = playlist?.copy(trackCount = playlist.trackCount.takeIf { it > 0 } ?: songs.size)
        return GameSession(
            code = generateGameCode(),
            host = host,
            players = listOf(host),
            playlist = selectedPlaylist ?: bootstrapPlaylist(songs),
            mode = GameMode.GUESS_THE_YEAR,
            currentRound = 0,
            totalRounds = songs.size,
            scores = emptyMap(),
            state = SessionState.WAITING,
            currentSong = null,
            timeline = emptyList(),
            songs = songs,
            turnState = TurnState.GUESSING,
            lastGuessResult = null,
            turnStartTime = null
        )
    }

    private fun bootstrapPlaylist(songs: List<Song>): Playlist = Playlist(
        id = "playlist-${UUID.randomUUID()}",
        name = "Clásicos del Pop",
        coverUrl = null,
        trackCount = songs.size
    )

    private fun bootstrapSongs(): List<Song> = listOf(
        Song(
            id = "song-1",
            title = "Levitating",
            artist = "Dua Lipa",
            year = 2020,
            albumArtUrl = null,
            previewUrl = null,
            uri = "spotify:track:levitating"
        ),
        Song(
            id = "song-2",
            title = "As It Was",
            artist = "Harry Styles",
            year = 2022,
            albumArtUrl = null,
            previewUrl = null,
            uri = "spotify:track:asiitwas"
        ),
        Song(
            id = "song-3",
            title = "Viva La Vida",
            artist = "Coldplay",
            year = 2008,
            albumArtUrl = null,
            previewUrl = null,
            uri = "spotify:track:viva"
        )
    )

    private fun generateGameCode(): String = UUID.randomUUID().toString().take(6).uppercase()
}
