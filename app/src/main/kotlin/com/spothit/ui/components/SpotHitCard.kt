package com.spothit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spothit.ui.theme.SpotHitCardDefaults

@Composable
fun SpotHitCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = SpotHitCardDefaults.colors(),
        shape = SpotHitCardDefaults.shape,
        elevation = SpotHitCardDefaults.elevated()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            content()
        }
    }
}

@Composable
fun SpotHitListRow(
    avatarText: String,
    primaryText: String,
    secondaryText: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    highlightColor: Color? = null,
    avatarColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
    avatarContentColor: Color = MaterialTheme.colorScheme.primary,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val backgroundColor = if (highlight) {
        highlightColor ?: SpotHitCardDefaults.colors().containerColor.copy(alpha = 0.4f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, SpotHitCardDefaults.shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        leadingContent?.invoke()

        Avatar(
            text = avatarText.firstOrNull()?.uppercase() ?: "?",
            backgroundColor = avatarColor,
            contentColor = avatarContentColor
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = primaryText,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = secondaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        trailingContent?.invoke()
    }
}

@Composable
private fun Avatar(text: String, backgroundColor: Color, contentColor: Color) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = contentColor
        )
    }
}
