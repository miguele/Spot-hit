package com.spothit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spothit.GameViewModel
import com.spothit.core.model.GameSession
import com.spothit.core.model.Player
import com.spothit.ui.components.InfoCard
import com.spothit.ui.components.PrimaryButton
import com.spothit.ui.components.SecondaryButton
import com.spothit.ui.components.SpotHitCard
import com.spothit.ui.components.SpotHitListRow
import com.spothit.ui.components.SpotHitScaffold
import com.spothit.ui.theme.SpotHitCardDefaults

@Composable
fun LobbyScreen(viewModel: GameViewModel, onStartGame: () -> Unit, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val session = uiState.session

    SpotHitScaffold(
        topContent = {
            SessionHeader(session)
        },
        bodyContent = {
            PlayerList(
                players = session?.players.orEmpty(),
                host = session?.host,
                modifier = Modifier.weight(1f)
            )
        },
        actionsContent = {
            PrimaryButton(
                text = "Empezar partida",
                enabled = session?.players?.isNotEmpty() == true,
                onClick = {
                    viewModel.startRound()
                    onStartGame()
                }
            )
            SecondaryButton(text = "Volver al inicio", onClick = onBack)
        }
    )
}

@Composable
private fun SessionHeader(session: GameSession?) {
    InfoCard(
        modifier = Modifier.fillMaxWidth(),
        title = "Sala",
        subtitle = session?.playlist?.name ?: "Playlist pendiente"
    )
    Spacer(modifier = Modifier.height(8.dp))
    LobbyCodeCard(session)
}

@Composable
private fun LobbyCodeCard(session: GameSession?) {
    val code = session?.code ?: "-"
    SpotHitCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Código de sala",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = code,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .size(164.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = SpotHitCardDefaults.shape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCode,
                    contentDescription = "Código QR",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(96.dp)
                )
            }
            Text(
                text = "Escanéalo o comparte el código para invitar jugadores.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PlayerList(players: List<Player>, host: Player?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Jugadores",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${players.size} en la sala",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (players.isEmpty()) {
            Text(
                text = "Aún no hay jugadores en el lobby.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(players) { player ->
                    PlayerRow(player = player, isHost = player.id == host?.id)
                }
            }
        }
    }
}

@Composable
private fun PlayerRow(player: Player, isHost: Boolean) {
    SpotHitListRow(
        modifier = Modifier.fillMaxWidth(),
        avatarText = player.name,
        primaryText = player.name,
        secondaryText = if (isHost) "Anfitrión" else "En lobby",
        highlight = true,
        trailingContent = {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                tint = if (isHost) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    )
}
