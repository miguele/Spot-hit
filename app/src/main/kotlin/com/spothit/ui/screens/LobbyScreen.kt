package com.spothit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spothit.GameViewModel
import com.spothit.core.model.GameSession
import com.spothit.ui.components.InfoCard
import com.spothit.ui.components.PrimaryButton

@Composable
fun LobbyScreen(viewModel: GameViewModel, onStartGame: () -> Unit, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val session = uiState.session

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        SessionHeader(session)
        Spacer(modifier = Modifier.height(16.dp))
        InfoCard(
            title = "Código de sala",
            subtitle = session?.code ?: "-"
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Jugadores", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(session?.players ?: emptyList()) { player ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Icon(imageVector = Icons.Default.Group, contentDescription = null)
                    Text(text = player.name, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        PrimaryButton(text = "Empezar", enabled = session?.players?.isNotEmpty() == true, onClick = {
            viewModel.startRound()
            onStartGame()
        })
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryButton(text = "Volver", onClick = onBack)
    }
}

@Composable
private fun SessionHeader(session: GameSession?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = session?.playlist?.name ?: "Playlist pendiente",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Rondas: ${session?.totalRounds ?: 0} | Modo: ${session?.mode ?: "-"}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
