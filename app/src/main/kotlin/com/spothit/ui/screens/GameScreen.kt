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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spothit.GameViewModel
import com.spothit.core.model.GameSession
import com.spothit.core.model.TurnState
import com.spothit.ui.components.PrimaryButton
import com.spothit.ui.components.SecondaryButton
import com.spothit.ui.components.SpotHitCard
import com.spothit.ui.components.SpotHitScaffold
import com.spothit.ui.theme.SpotHitCardDefaults
import com.spothit.ui.theme.SuccessGreen

@Composable
fun GameScreen(viewModel: GameViewModel, onShowResults: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val session = state.session
    val currentPlayerId = session?.host?.id
    val currentScore = currentPlayerId?.let { session?.scores?.get(it) } ?: 0
    val roundPoints = session?.lastGuessResult?.let { result ->
        """([+-]?\d+)""".toRegex().find(result)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }
    var guess by remember { mutableStateOf("") }

    SpotHitScaffold(
        topContent = {
            SessionInfo(session)
        },
        bodyContent = {
            Spacer(modifier = Modifier.height(16.dp))
            CurrentSongCard(session)
            Spacer(modifier = Modifier.height(12.dp))
            if (session?.turnState == TurnState.GUESSING) {
                YearInput(
                    value = guess,
                    onValueChange = { guess = it.filter { char -> char.isDigit() }.take(4) }
                )
            } else {
                RoundResult(
                    correctYear = session?.currentSong?.year,
                    roundPoints = roundPoints ?: currentScore,
                    message = session?.lastGuessResult.orEmpty()
                )
            }
        },
        actionsContent = {
            if (session?.turnState == TurnState.GUESSING) {
                PrimaryButton(
                    text = "Enviar respuesta",
                    enabled = guess.length == 4,
                    onClick = {
                        viewModel.submitGuess(session.host.id, guess.toInt())
                    }
                )
                SecondaryButton(text = "Volver al lobby", onClick = onBack)
            } else {
                val isLastRound = session?.currentRound == session?.totalRounds
                PrimaryButton(
                    text = if (isLastRound) "Ver ranking" else "Siguiente ronda",
                    onClick = {
                        if (isLastRound) {
                            viewModel.completeGame()
                            onShowResults()
                        } else {
                            viewModel.startRound()
                        }
                    }
                )
                SecondaryButton(text = "Ver resultados", onClick = onShowResults)
            }
        }
    )
}

@Composable
private fun SessionInfo(session: GameSession?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Ronda ${session?.currentRound ?: 0} / ${session?.totalRounds ?: 0}",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "${session?.players?.size ?: 0} jugadores",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Card(
            colors = SpotHitCardDefaults.colors(),
            shape = SpotHitCardDefaults.shape,
            elevation = SpotHitCardDefaults.elevated()
        ) {
            Text(
                text = session?.mode?.name ?: "",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun CurrentSongCard(session: GameSession?) {
    SpotHitCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session?.currentSong?.title ?: "Canción en espera",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = session?.currentSong?.artist ?: "Artista por mostrar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Turno: ${session?.turnState ?: TurnState.GUESSING}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun YearInput(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("¿En qué año salió?") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        )
    )
}

@Composable
private fun RoundResult(correctYear: Int?, roundPoints: Int, message: String) {
    val description = message.ifBlank { "Consulta el año y avanza a la siguiente ronda." }
    SpotHitCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Resultado de la ronda",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "Año correcto",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = correctYear?.toString() ?: "--",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Puntos obtenidos",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$roundPoints pts",
                    style = MaterialTheme.typography.titleLarge.copy(color = SuccessGreen)
                )
            }
        }
    }
}
