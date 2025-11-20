package com.spothit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spothit.ui.components.PrimaryButton
import com.spothit.ui.components.SpotHitScreen

@Composable
fun HomeScreen(onNavigateToSetup: (SetupMode) -> Unit) {
    SpotHitScreen(modifier = Modifier.fillMaxSize()) {
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
                    onClick = { onNavigateToSetup(SetupMode.DJ) }
                )
                PrimaryButton(
                    text = "Unirse a la partida",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onClick = { onNavigateToSetup(SetupMode.GUEST) }
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
}
