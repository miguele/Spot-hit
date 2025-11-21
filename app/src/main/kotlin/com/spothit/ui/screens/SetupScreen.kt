@file:OptIn(ExperimentalMaterial3Api::class)

package com.spothit.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.spothit.GameUiState
import com.spothit.GameViewModel
import com.spothit.core.model.Playlist
import com.spothit.ui.components.AvatarDefaults
import com.spothit.ui.components.PredefinedAvatar
import com.spothit.ui.components.PlayerAvatar
import com.spothit.ui.components.PrimaryButton
import com.spothit.ui.components.SecondaryButton
import com.spothit.ui.components.SpotHitScreen
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.launch

enum class SetupMode { DJ, GUEST;
    companion object {
        fun fromRoute(route: String?): SetupMode = when (route?.lowercase()) {
            "guest" -> GUEST
            else -> DJ
        }
    }
}

enum class AvatarOwner { HOST, GUEST }

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
    var hostAvatarUrl by rememberSaveable { mutableStateOf(AvatarDefaults.defaultAvatarUrl()) }
    var guestAvatarUrl by rememberSaveable { mutableStateOf(AvatarDefaults.defaultAvatarUrl()) }
    var hostAvatarError by rememberSaveable { mutableStateOf<String?>(null) }
    var guestAvatarError by rememberSaveable { mutableStateOf<String?>(null) }
    var hostAvatarLoading by rememberSaveable { mutableStateOf(false) }
    var guestAvatarLoading by rememberSaveable { mutableStateOf(false) }
    var avatarOwner by rememberSaveable { mutableStateOf<AvatarOwner?>(null) }
    var pendingAvatarAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val readImagesPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    fun setAvatarLoading(target: AvatarOwner?, value: Boolean) {
        when (target) {
            AvatarOwner.HOST -> hostAvatarLoading = value
            AvatarOwner.GUEST -> guestAvatarLoading = value
            null -> Unit
        }
    }

    fun setAvatarUrl(target: AvatarOwner?, value: String) {
        when (target) {
            AvatarOwner.HOST -> hostAvatarUrl = value
            AvatarOwner.GUEST -> guestAvatarUrl = value
            null -> Unit
        }
    }

    fun setAvatarError(target: AvatarOwner?, value: String?) {
        when (target) {
            AvatarOwner.HOST -> hostAvatarError = value
            AvatarOwner.GUEST -> guestAvatarError = value
            null -> Unit
        }
    }

    fun showAvatarError(target: AvatarOwner?, message: String) {
        setAvatarError(target, message)
        scope.launch {
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val target = avatarOwner
        if (granted) {
            pendingAvatarAction?.invoke()
        } else {
            showAvatarError(target, "Necesitamos permiso para acceder a tus imágenes")
        }
        pendingAvatarAction = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val target = avatarOwner
        if (granted) {
            pendingAvatarAction?.invoke()
        } else {
            showAvatarError(target, "Permite el uso de la cámara para continuar")
        }
        pendingAvatarAction = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val target = avatarOwner
        setAvatarLoading(target, false)
        if (uri != null && target != null) {
            setAvatarUrl(target, uri.toString())
            setAvatarError(target, null)
        } else if (target != null) {
            showAvatarError(target, "No pudimos cargar la imagen seleccionada")
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        val target = avatarOwner
        setAvatarLoading(target, false)
        if (bitmap != null && target != null) {
            val uri = saveBitmapToCache(context, bitmap)
            if (uri != null) {
                setAvatarUrl(target, uri)
                setAvatarError(target, null)
            } else {
                showAvatarError(target, "No se pudo guardar la foto")
            }
        } else if (target != null) {
            showAvatarError(target, "No se pudo tomar la foto")
        }
    }

    fun launchGallery(target: AvatarOwner) {
        avatarOwner = target
        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        val hasPermission = ContextCompat.checkSelfPermission(context, readImagesPermission) == PackageManager.PERMISSION_GRANTED
        if (needsPermission && !hasPermission) {
            pendingAvatarAction = {
                setAvatarLoading(target, true)
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            galleryPermissionLauncher.launch(readImagesPermission)
        } else {
            setAvatarLoading(target, true)
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    fun launchCamera(target: AvatarOwner) {
        avatarOwner = target
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            pendingAvatarAction = {
                setAvatarLoading(target, true)
                cameraLauncher.launch(null)
            }
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            setAvatarLoading(target, true)
            cameraLauncher.launch(null)
        }
    }

    LaunchedEffect(uiState.creationCompleted) {
        if (uiState.creationCompleted) {
            onNavigateToLobby()
            hasNavigated = true
            viewModel.consumeCreationCompleted()
        }
    }

    LaunchedEffect(uiState.isLobbyActive) {
        if (uiState.isLobbyActive && !hasNavigated) {
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
        Box(modifier = Modifier.fillMaxSize()) {
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
                        avatarUrl = hostAvatarUrl,
                        isAvatarLoading = hostAvatarLoading,
                        avatarError = hostAvatarError,
                        onAvatarSelected = { hostAvatarUrl = it },
                        onPickAvatarFromGallery = { launchGallery(AvatarOwner.HOST) },
                        onCaptureAvatar = { launchCamera(AvatarOwner.HOST) },
                        onHostNameChange = { hostName = it },
                        onRoundsChange = { value -> rounds = value.filter { char -> char.isDigit() }.take(2) },
                        onContinue = {
                            val totalRounds = rounds.toIntOrNull()?.coerceIn(1, 10) ?: 3
                            viewModel.startAuthorization(hostName.trim(), totalRounds, hostAvatarUrl)
                            hasNavigated = false
                        }
                    )

                    SetupMode.GUEST -> GuestCard(
                        guestName = guestName,
                        gameCode = lobbyCode,
                        isLoading = uiState.isLoading,
                        avatarUrl = guestAvatarUrl,
                        isAvatarLoading = guestAvatarLoading,
                        avatarError = guestAvatarError,
                        onAvatarSelected = { guestAvatarUrl = it },
                        onPickAvatarFromGallery = { launchGallery(AvatarOwner.GUEST) },
                        onCaptureAvatar = { launchCamera(AvatarOwner.GUEST) },
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
                            viewModel.joinSession(
                                playerName = guestName.trim(),
                                lobbyCode = lobbyCode.trim().uppercase(),
                                avatarUrl = guestAvatarUrl
                            )
                            hasNavigated = false
                        }
                    )
                }
            }

            if (uiState.showPlaylistSelection) {
                PlaylistSelectionScreen(
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
            onClick = { onModeSelected(SetupMode.DJ) },
            modifier = Modifier.weight(1f)
        )
        ModeChip(
            title = "Invitado",
            description = "Únete y adivina los hits",
            selected = selectedMode == SetupMode.GUEST,
            icon = Icons.Default.Person,
            onClick = { onModeSelected(SetupMode.GUEST) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModeChip(
    title: String,
    description: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (selected) Color(0xFF1DB954).copy(alpha = 0.2f) else Color.Transparent
    val borderColor = if (selected) Color(0xFF1DB954) else Color(0xFF2E3A55)

    Row(
        modifier = modifier
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
        Column {
            Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(text = description, color = Color(0xFF9AA3B5), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AvatarSelector(
    title: String,
    selectedAvatar: String,
    isLoading: Boolean,
    error: String?,
    onAvatarSelected: (String) -> Unit,
    onPickFromGallery: () -> Unit,
    onCaptureFromCamera: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AvatarDefaults.predefinedAvatars.forEach { avatar ->
                val optionUrl = AvatarDefaults.urlFor(avatar.id)
                AvatarOptionChip(
                    predefinedAvatar = avatar,
                    selected = selectedAvatar == optionUrl,
                    onClick = { onAvatarSelected(optionUrl) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SecondaryButton(
                text = "Desde galería",
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                onClick = onPickFromGallery
            )
            SecondaryButton(
                text = "Tomar foto",
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                onClick = onCaptureFromCamera
            )
        }
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun AvatarOptionChip(predefinedAvatar: PredefinedAvatar, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF2E3A55)
    val background = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color(0x3319273F)
    Column(
        modifier = Modifier
            .width(96.dp)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .background(background, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PlayerAvatar(
            avatarUrl = AvatarDefaults.urlFor(predefinedAvatar.id),
            displayName = predefinedAvatar.label,
            size = 56.dp
        )
        Text(
            text = predefinedAvatar.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
    }
}

@Composable
private fun HostCard(
    hostName: String,
    rounds: String,
    isLoading: Boolean,
    avatarUrl: String,
    isAvatarLoading: Boolean,
    avatarError: String?,
    onAvatarSelected: (String) -> Unit,
    onPickAvatarFromGallery: () -> Unit,
    onCaptureAvatar: () -> Unit,
    onHostNameChange: (String) -> Unit,
    onRoundsChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val isBusy = isLoading || isAvatarLoading
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
            AvatarSelector(
                title = "Elige tu avatar de DJ",
                selectedAvatar = avatarUrl,
                isLoading = isAvatarLoading,
                error = avatarError,
                onAvatarSelected = onAvatarSelected,
                onPickFromGallery = onPickAvatarFromGallery,
                onCaptureFromCamera = onCaptureAvatar
            )
            OutlinedTextField(
                value = hostName,
                onValueChange = onHostNameChange,
                label = { Text("Tu nombre") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy
            )
            OutlinedTextField(
                value = rounds,
                onValueChange = onRoundsChange,
                label = { Text("Rondas (máx 10)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy
            )
            PrimaryButton(
                text = if (isLoading) "Preparando..." else "Continuar como DJ",
                enabled = hostName.isNotBlank() && rounds.toIntOrNull() != null && !isBusy,
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
    avatarUrl: String,
    isAvatarLoading: Boolean,
    avatarError: String?,
    onAvatarSelected: (String) -> Unit,
    onPickAvatarFromGallery: () -> Unit,
    onCaptureAvatar: () -> Unit,
    onGuestNameChange: (String) -> Unit,
    onGameCodeChange: (String) -> Unit,
    onScanQr: () -> Unit,
    onJoin: () -> Unit
) {
    val isBusy = isLoading || isAvatarLoading
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
            AvatarSelector(
                title = "Personaliza tu avatar",
                selectedAvatar = avatarUrl,
                isLoading = isAvatarLoading,
                error = avatarError,
                onAvatarSelected = onAvatarSelected,
                onPickFromGallery = onPickAvatarFromGallery,
                onCaptureFromCamera = onCaptureAvatar
            )
            OutlinedTextField(
                value = guestName,
                onValueChange = onGuestNameChange,
                label = { Text("Tu nombre") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy
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
                enabled = !isBusy,
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
                enabled = !isBusy
            )
            PrimaryButton(
                text = if (isLoading) "Uniendo..." else "Unirme a la sala",
                enabled = guestName.isNotBlank() && normalizedCode.length == 6 && !isBusy,
                onClick = onJoin
            )
        }
    }
}

@Composable
private fun PlaylistSelectionScreen(
    uiState: GameUiState,
    onPlaylistSelected: (Playlist) -> Unit,
    onConfirm: () -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE60B1024))
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1728)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Selecciona tu playlist",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                text = "Elige la lista para tu partida antes de abrir el lobby.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9AA3B5))
                            )
                        }
                        TextButton(onClick = onRetry, enabled = !uiState.isLoadingPlaylists) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Actualizar")
                        }
                    }

                    when {
                        uiState.isLoadingPlaylists -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        uiState.playlists.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No encontramos playlists en tu cuenta.",
                                    color = Color.White
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.playlists) { playlist ->
                                    PlaylistCard(
                                        playlist = playlist,
                                        selected = uiState.selectedPlaylist?.id == playlist.id,
                                        onClick = { onPlaylistSelected(playlist) }
                                    )
                                }
                            }
                        }
                    }

                    uiState.playlistError?.let {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x33FF6B6B), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                            TextButton(onClick = onRetry) { Text("Reintentar") }
                        }
                    }
                }

                PrimaryButton(
                    text = "Crear Partida y Abrir Lobby",
                    enabled = uiState.selectedPlaylist != null && uiState.hasValidAccessToken && !uiState.isLoadingPlaylists,
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PlaylistCard(playlist: Playlist, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) Color(0xFF1DB954) else Color(0xFF1B2A44)
    val backgroundColor = if (selected) Color(0x331DB954) else Color(0xFF0D1526)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val painter = rememberAsyncImagePainter(
            model = playlist.coverUrl,
            onError = { },
            onLoading = { }
        )

        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1B2A44)),
            contentAlignment = Alignment.Center
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                }

                is AsyncImagePainter.State.Error -> {
                    Icon(
                        imageVector = Icons.Default.HeadsetMic,
                        contentDescription = null,
                        tint = Color(0xFF9AA3B5)
                    )
                }

                else -> {
                    androidx.compose.foundation.Image(
                        painter = painter,
                        contentDescription = playlist.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${playlist.trackCount} canciones",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9AA3B5))
            )
        }

        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (selected) Color(0xFF1DB954) else Color(0xFF2C364C)
        )
    }
}

private fun saveBitmapToCache(context: Context, bitmap: Bitmap): String? {
    return try {
        val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        file.toUri().toString()
    } catch (io: IOException) {
        null
    }
}
