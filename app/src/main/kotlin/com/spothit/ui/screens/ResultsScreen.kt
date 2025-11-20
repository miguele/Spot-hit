package com.spothit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spothit.GameViewModel
import com.spothit.core.model.Player
import com.spothit.ui.components.PrimaryButton
import com.spothit.ui.components.SecondaryButton
import com.spothit.ui.components.SpotHitCard
import com.spothit.ui.components.SpotHitListRow
import com.spothit.ui.components.SpotHitScaffold
import com.spothit.ui.theme.GreenPrimary

@Composable
fun ResultsScreen(viewModel: GameViewModel, onPlayAgain: () -> Unit, onBackToHome: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val session = uiState.session
    val scores = session?.scores?.entries?.sortedByDescending { it.value } ?: emptyList()
    val playersById = session?.players?.associateBy { it.id } ?: emptyMap()
    val currentUserId = session?.host?.id

    SpotHitScaffold(
        topContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Ranking final",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Así quedó la sala tras ${session?.totalRounds ?: 0} rondas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        bodyContent = {
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(scores) { index, score ->
                    val player = playersById[score.key]
                    ScoreRow(
                        position = index + 1,
                        player = player,
                        points = score.value,
                        isCurrentUser = currentUserId == score.key
                    )
                }
            }
        },
        actionsContent = {
            PrimaryButton(text = "Jugar otra vez", onClick = onPlayAgain)
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryButton(text = "Volver al inicio", onClick = onBackToHome)
        }
    )
}

@Composable
private fun ScoreRow(position: Int, player: Player?, points: Int, isCurrentUser: Boolean) {
    SpotHitCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        SpotHitListRow(
            avatarText = player?.name ?: "?",
            avatarUrl = player?.avatarUrl,
            primaryText = player?.name ?: "Jugador",
            secondaryText = if (isCurrentUser) "Tú" else "${player?.id ?: ""}",
            highlight = isCurrentUser,
            highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            leadingContent = { Medal(position) },
            trailingContent = {
                Text(
                    text = "$points pts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GreenPrimary
                )
            }
        )
    }
}

@Composable
private fun Medal(position: Int) {
    val color = when (position) {
        1 -> MaterialTheme.colorScheme.primary
        2 -> MaterialTheme.colorScheme.onSurfaceVariant
        3 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = color
        )
        Text(
            text = position.toString(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
