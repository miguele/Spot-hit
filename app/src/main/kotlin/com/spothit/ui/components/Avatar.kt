package com.spothit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

private const val PREDEFINED_PREFIX = "predefined:"

data class PredefinedAvatar(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val contentColor: Color
)

object AvatarDefaults {
    val predefinedAvatars: List<PredefinedAvatar> = listOf(
        PredefinedAvatar(
            id = "neon_mic",
            label = "Mic DJ",
            icon = Icons.Default.Mic,
            backgroundColor = Color(0xFF102A43),
            contentColor = Color(0xFF5BE49B)
        ),
        PredefinedAvatar(
            id = "vinyl_wave",
            label = "Vinilo",
            icon = Icons.Default.Album,
            backgroundColor = Color(0xFF2C1B47),
            contentColor = Color(0xFFE56BFF)
        ),
        PredefinedAvatar(
            id = "radio_star",
            label = "Radio",
            icon = Icons.Default.Radio,
            backgroundColor = Color(0xFF2B2E4A),
            contentColor = Color(0xFFFFC857)
        ),
        PredefinedAvatar(
            id = "headphones",
            label = "Headset",
            icon = Icons.Default.Headphones,
            backgroundColor = Color(0xFF0F3D3E),
            contentColor = Color(0xFF7CF3FF)
        ),
        PredefinedAvatar(
            id = "music_note",
            label = "Nota",
            icon = Icons.Default.MusicNote,
            backgroundColor = Color(0xFF3D1E2F),
            contentColor = Color(0xFFFF9B9B)
        ),
        PredefinedAvatar(
            id = "equalizer",
            label = "EQ",
            icon = Icons.Default.GraphicEq,
            backgroundColor = Color(0xFF2B2F3A),
            contentColor = Color(0xFF9AE6FF)
        )
    )

    fun defaultAvatarUrl(): String = urlFor(predefinedAvatars.first().id)

    fun urlFor(id: String): String = "$PREDEFINED_PREFIX$id"

    fun fromUrl(url: String?): PredefinedAvatar? {
        val id = url?.takeIf { it.startsWith(PREDEFINED_PREFIX) }?.removePrefix(PREDEFINED_PREFIX)
        return predefinedAvatars.find { it.id == id }
    }
}

@Composable
fun PlayerAvatar(
    avatarUrl: String?,
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    val predefined = AvatarDefaults.fromUrl(avatarUrl)
    val initials = displayName.firstOrNull()?.uppercase() ?: "?"

    when {
        predefined != null -> PredefinedAvatarView(predefined = predefined, modifier = modifier, size = size)
        !avatarUrl.isNullOrBlank() -> RemoteAvatar(avatarUrl, initials, modifier, size, backgroundColor, contentColor)
        else -> InitialsAvatar(initials = initials, modifier = modifier, size = size, backgroundColor = backgroundColor, contentColor = contentColor)
    }
}

@Composable
private fun PredefinedAvatarView(predefined: PredefinedAvatar, modifier: Modifier, size: Dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(predefined.backgroundColor)
            .border(1.dp, predefined.contentColor.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = predefined.icon,
            contentDescription = predefined.label,
            tint = predefined.contentColor,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

@Composable
private fun RemoteAvatar(
    avatarUrl: String,
    fallbackInitials: String,
    modifier: Modifier,
    size: Dp,
    backgroundColor: Color,
    contentColor: Color
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalAppContext.current)
            .data(avatarUrl)
            .crossfade(true)
            .build()
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size)
                )
            }

            is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(size * 0.4f),
                    strokeWidth = 2.dp
                )
            }

            is AsyncImagePainter.State.Error -> {
                InitialsAvatar(
                    initials = fallbackInitials,
                    modifier = Modifier,
                    size = size,
                    backgroundColor = backgroundColor,
                    contentColor = contentColor
                )
            }

            else -> {
                InitialsAvatar(
                    initials = fallbackInitials,
                    modifier = Modifier,
                    size = size,
                    backgroundColor = backgroundColor,
                    contentColor = contentColor
                )
            }
        }
    }
}

@Composable
private fun InitialsAvatar(initials: String, modifier: Modifier, size: Dp, backgroundColor: Color, contentColor: Color) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = contentColor
        )
    }
}

@Composable
private val LocalAppContext
    get() = androidx.compose.ui.platform.LocalContext.current
