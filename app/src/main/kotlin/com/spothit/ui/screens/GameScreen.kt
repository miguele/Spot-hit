package com.spothit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spothit.GameViewModel
import com.spothit.core.model.GameSession
import com.spothit.core.model.TurnState
import com.spothit.ui.components.PrimaryButton

@Composable
fun GameScreen(viewModel: GameViewModel, onShowResults: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val session = state.session
    var guess by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SessionInfo(session)
        Spacer(modifier = Modifier.height(16.dp))
        CurrentSongCard(session)
        Spacer(modifier = Modifier.height(12.dp))
        if (session?.turnState == TurnState.GUESSING) {
            OutlinedTextField(
                value = guess,
                onValueChange = { guess = it.filter { char -> char.isDigit() }.take(4) },
                label = { Text("¿Año?") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(
                text = "Enviar",
                enabled = guess.length == 4,
                onClick = {
                    viewModel.submitGuess(session.host.id, guess.toInt())
                }
            )
        } else {
            Text(
                text = session?.lastGuessResult ?: "",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            PrimaryButton(text = "Siguiente", onClick = { viewModel.startRound() })
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PrimaryButton(text = "Resultados", onClick = onShowResults, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            PrimaryButton(text = "Volver", onClick = onBack, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SessionInfo(session: GameSession?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Ronda ${session?.currentRound ?: 0} / ${session?.totalRounds ?: 0}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(text = "Jugadores: ${session?.players?.size ?: 0}")
    }
}

@Composable
private fun CurrentSongCard(session: GameSession?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = session?.currentSong?.title ?: "Canción en espera", style = MaterialTheme.typography.titleLarge)
            Text(text = session?.currentSong?.artist ?: "Artista por mostrar", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Estado: ${session?.turnState ?: TurnState.GUESSING}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
