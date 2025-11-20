package com.spothit.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spothit.ui.theme.SpotHitButtonDefaults

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = SpotHitButtonDefaults.primaryColors(),
        shape = SpotHitButtonDefaults.shape,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = SpotHitButtonDefaults.secondaryColors(),
        shape = SpotHitButtonDefaults.shape,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
