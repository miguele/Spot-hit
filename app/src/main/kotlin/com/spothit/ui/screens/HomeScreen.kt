package com.spothit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spothit.core.model.Playlist
import com.spothit.GameViewModel
import com.spothit.ui.components.PrimaryButton
import com.spothit.ui.components.SpotHitScreen
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(viewModel: GameViewModel, onNavigateToLobby: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var hostName by remember { mutableStateOf("") }
    var friendName by remember { mutableStateOf("") }
    var rounds by remember { mutableStateOf("3") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.creationCompleted) {
        if (uiState.creationCompleted) {
            onNavigateToLobby()
            viewModel.consumeCreationCompleted()
        }
    }

    LaunchedEffect(uiState.authError, uiState.playlistError, uiState.error) {
        listOf(uiState.authError, uiState.playlistError, uiState.error)
            .filterNotNull()
            .forEach { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
                }
            }
    }

    SpotHitScreen(modifier = Modifier.fillMaxSize(), snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1DB954).copy(alpha = 0.15f))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFF1DB954),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Spot-Hit",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Adivina el año exacto de los temazos que amas.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFE4E7EE)),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Elige un modo, invita amigos y deja que la música hable.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9AA3B5)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PrimaryButton(
                    text = "Crear nueva partida",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onClick = { showCreateDialog = true }
                )
                PrimaryButton(
                    text = "Unirse a la partida",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onClick = { showJoinDialog = true }
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Partidas anteriores",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9AA3B5)),
                    modifier = Modifier.clickable { /* TODO: Navegar a historial */ }
                )
                Text(
                    text = "Powered by Spotify",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6AE2A2),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Crear partida", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = hostName,
                        onValueChange = { hostName = it },
                        label = { Text("Tu nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rounds,
                        onValueChange = { rounds = it.filter { char -> char.isDigit() }.take(2) },
                        label = { Text("Rondas (máx 10)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = hostName.isNotBlank() && rounds.toIntOrNull() != null,
                    onClick = {
                        val totalRounds = rounds.toIntOrNull()?.coerceIn(1, 10) ?: 3
                        viewModel.startAuthorization(hostName.trim(), totalRounds)
                        showCreateDialog = false
                    }
                ) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Unirse a partida", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = friendName,
                        onValueChange = { friendName = it },
                        label = { Text("Tu nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = friendName.isNotBlank(),
                    onClick = {
                        viewModel.joinSession(friendName.trim())
                        showJoinDialog = false
                        onNavigateToLobby()
                    }
                ) { Text("Unirme") }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (uiState.showPlaylistSelection) {
        PlaylistSelectionDialog(
            uiState = uiState,
            onPlaylistSelected = { viewModel.selectPlaylistForPreview(it) },
            onConfirm = {
                uiState.selectedPlaylist?.let { playlist ->
                    viewModel.selectPlaylistAndCreate(playlist)
                }
            },
            onRetry = { viewModel.retryPlaylistLoad() }
        )
    }
}

@Composable
private fun PlaylistSelectionDialog(
    uiState: com.spothit.GameUiState,
    onPlaylistSelected: (Playlist) -> Unit,
    onConfirm: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Selecciona tu playlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState.isLoadingPlaylists) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (uiState.playlists.isEmpty()) {
                        Text("No encontramos playlists en tu cuenta.")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(uiState.playlists) { playlist ->
                                PlaylistRow(
                                    playlist = playlist,
                                    selected = uiState.selectedPlaylist?.id == playlist.id,
                                    onClick = { onPlaylistSelected(playlist) }
                                )
                            }
                        }
                    }
                }

                uiState.playlistError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onRetry) { Text("Reintentar") }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = uiState.selectedPlaylist != null && uiState.hasValidAccessToken && !uiState.isLoadingPlaylists,
                onClick = onConfirm
            ) { Text("Usar playlist") }
        },
        dismissButton = {
            TextButton(onClick = onRetry, enabled = !uiState.isLoadingPlaylists) { Text("Actualizar") }
        }
    )
}

@Composable
private fun PlaylistRow(playlist: Playlist, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = playlist.name, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${playlist.trackCount} canciones",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
