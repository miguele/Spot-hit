package com.spothit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spothit.GameViewModel
import com.spothit.ui.components.InfoCard
import com.spothit.ui.components.PrimaryButton

@Composable
fun HomeScreen(viewModel: GameViewModel, onNavigateToLobby: () -> Unit) {
    var hostName by remember { mutableStateOf("") }
    var friendName by remember { mutableStateOf("") }
    var rounds by remember { mutableStateOf("3") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Spot-Hit",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
        )
        Text(
            text = "Crea o únete a una partida y adivina el año de los éxitos",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        InfoCard(
            title = "¿Cómo funciona?",
            subtitle = "El anfitrión crea una sala, los amigos se unen y cada ronda debéis adivinar el año de la canción."
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text("Crear partida", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryButton(
            text = "Crear sala",
            enabled = hostName.isNotBlank() && rounds.toIntOrNull() != null,
            onClick = {
                val totalRounds = rounds.toIntOrNull()?.coerceIn(1, 10) ?: 3
                viewModel.createSession(hostName.trim(), totalRounds)
                onNavigateToLobby()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Unirse a partida", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        OutlinedTextField(
            value = friendName,
            onValueChange = { friendName = it },
            label = { Text("Tu nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryButton(
            text = "Unirme",
            enabled = friendName.isNotBlank(),
            onClick = {
                viewModel.joinSession(friendName.trim())
                onNavigateToLobby()
            }
        )
    }
}
