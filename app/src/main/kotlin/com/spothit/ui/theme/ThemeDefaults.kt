package com.spothit.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object SpotHitCardDefaults {
    @Composable
    fun colors() = CardDefaults.cardColors(
        containerColor = DarkCard,
        contentColor = OnDark
    )

    @Composable
    fun elevated() = CardDefaults.cardElevation(defaultElevation = 8.dp)

    val shape = RoundedCornerShape(16.dp)
}

object SpotHitButtonDefaults {
    @Composable
    fun primaryColors() = ButtonDefaults.buttonColors(
        containerColor = GreenPrimary,
        contentColor = OnDark
    )

    @Composable
    fun secondaryColors() = ButtonDefaults.buttonColors(
        containerColor = GreenSecondary.copy(alpha = 0.2f),
        contentColor = GreenSecondary
    )

    val shape = RoundedCornerShape(12.dp)
}
