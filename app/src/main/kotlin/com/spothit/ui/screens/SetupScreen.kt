package com.spothit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spothit.GameUiState
import com.spothit.GameViewModel
import com.spothit.core.model.Playlist
import com.spothit.ui.components.PrimaryButton
import com.spothit.ui.components.SecondaryButton
import com.spothit.ui.components.SpotHitScreen
import kotlinx.coroutines.launch

enum class SetupMode { DJ, GUEST;
    companion object {
        fun fromRoute(route: String?): SetupMode = when (route?.lowercase()) {
            "guest" -> GUEST
            else -> DJ
        }
    }
}

@Composable
fun SetupScreen(
    viewModel: GameViewModel,
    initialMode: SetupMode,
    onBack: () -> Unit,
    onNavigateToLobby: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMode by rememberSaveable { mutableStateOf(initialMode) }
    var hostName by rememberSaveable { mutableStateOf("") }
    var guestName by rememberSaveable { mutableStateOf("") }
    var lobbyCode by rememberSaveable { mutableStateOf("") }
    var rounds by rememberSaveable { mutableStateOf("3") }
    var hasNavigated by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.creationCompleted) {
        if (uiState.creationCompleted) {
            onNavigateToLobby()
            hasNavigated = true
            viewModel.consumeCreationCompleted()
        }
    }

    LaunchedEffect(uiState.isLobby) {
        if (uiState.isLobby && !hasNavigated) {
            hasNavigated = true
            onNavigateToLobby()
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

    SpotHitScreen(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(onBack = onBack)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Elige cómo quieres jugar",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text = "Configura tu sala como DJ o ingresa como invitado para unirte a tus amigos.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9AA3B5)),
                modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
            )

            ModeSelector(selectedMode = selectedMode, onModeSelected = { selectedMode = it })
            Spacer(modifier = Modifier.height(18.dp))

            when (selectedMode) {
                SetupMode.DJ -> HostCard(
                    hostName = hostName,
                    rounds = rounds,
                    isLoading = uiState.isLoading || uiState.isAuthorizing,
                    onHostNameChange = { hostName = it },
                    onRoundsChange = { value -> rounds = value.filter { char -> char.isDigit() }.take(2) },
                    onContinue = {
                        val totalRounds = rounds.toIntOrNull()?.coerceIn(1, 10) ?: 3
                        viewModel.startAuthorization(hostName.trim(), totalRounds)
                        hasNavigated = false
                    }
                )

                SetupMode.GUEST -> GuestCard(
                    guestName = guestName,
                    gameCode = lobbyCode,
                    isLoading = uiState.isLoading,
                    onGuestNameChange = { guestName = it },
                    onGameCodeChange = { lobbyCode = it },
                    onScanQr = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Escaneo QR disponible próximamente"
                            )
                        }
                    },
                    onJoin = {
                        viewModel.joinSession(guestName.trim(), lobbyCode.trim().uppercase())
                        hasNavigated = false
                    }
                )
            }
        }
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
private fun TopBar(onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
        },
        title = {
            Text(
                text = "Configuración",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun ModeSelector(selectedMode: SetupMode, onModeSelected: (SetupMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x331DB954), shape = RoundedCornerShape(18.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModeChip(
            title = "DJ (Host)",
            description = "Controla la playlist y el ritmo",
            selected = selectedMode == SetupMode.DJ,
            icon = Icons.Default.HeadsetMic,
            onClick = { onModeSelected(SetupMode.DJ) }
        )
        ModeChip(
            title = "Invitado",
            description = "Únete y adivina los hits",
            selected = selectedMode == SetupMode.GUEST,
            icon = Icons.Default.Person,
            onClick = { onModeSelected(SetupMode.GUEST) }
        )
    }
}

@Composable
private fun ModeChip(
    title: String,
    description: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val background = if (selected) Color(0xFF1DB954).copy(alpha = 0.2f) else Color.Transparent
    val borderColor = if (selected) Color(0xFF1DB954) else Color(0xFF2E3A55)

    Row(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .background(background, shape = RoundedCornerShape(14.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color(0xFF1DB954).copy(alpha = 0.12f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF1DB954))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(text = description, color = Color(0xFF9AA3B5), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun HostCard(
    hostName: String,
    rounds: String,
    isLoading: Boolean,
    onHostNameChange: (String) -> Unit,
    onRoundsChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x3319273F)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Configura tu sala",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "El DJ controla la playlist. Tras autorizar Spotify, elige la playlist y define las rondas.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9AA3B5))
            )
            OutlinedTextField(
                value = hostName,
                onValueChange = onHostNameChange,
                label = { Text("Tu nombre") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            OutlinedTextField(
                value = rounds,
                onValueChange = onRoundsChange,
                label = { Text("Rondas (máx 10)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            PrimaryButton(
                text = if (isLoading) "Preparando..." else "Continuar como DJ",
                enabled = hostName.isNotBlank() && rounds.toIntOrNull() != null && !isLoading,
                onClick = onContinue
            )
        }
    }
}

@Composable
private fun GuestCard(
    guestName: String,
    gameCode: String,
    isLoading: Boolean,
    onGuestNameChange: (String) -> Unit,
    onGameCodeChange: (String) -> Unit,
    onScanQr: () -> Unit,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x3319273F)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Únete a la partida",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Ingresa tu nombre y el código de sala que comparte el DJ o escanea el QR.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9AA3B5))
            )
            OutlinedTextField(
                value = guestName,
                onValueChange = onGuestNameChange,
                label = { Text("Tu nombre") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Text(
                text = "Datos de la partida",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Escribe el código o escanéalo para confirmar la sala correcta.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9AA3B5))
            )
            val normalizedCode = gameCode.uppercase()
            val isCodeIncomplete = normalizedCode.length in 1..5
            OutlinedTextField(
                value = normalizedCode,
                onValueChange = { value -> onGameCodeChange(value.filter { !it.isWhitespace() }.uppercase().take(6)) },
                label = { Text("Código de sala") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                isError = isCodeIncomplete,
                supportingText = {
                    Text(
                        text = if (isCodeIncomplete) "El código debe tener 6 caracteres" else "Pide al DJ el código o escanea el QR",
                        color = if (isCodeIncomplete) MaterialTheme.colorScheme.error else Color(0xFF9AA3B5)
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null)
                }
            )
            SecondaryButton(
                text = "Escanear QR",
                onClick = onScanQr,
                enabled = !isLoading
            )
            PrimaryButton(
                text = if (isLoading) "Uniendo..." else "Unirme a la sala",
                enabled = guestName.isNotBlank() && normalizedCode.length == 6 && !isLoading,
                onClick = onJoin
            )
        }
    }
}

@Composable
private fun PlaylistSelectionDialog(
    uiState: GameUiState,
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
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
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
