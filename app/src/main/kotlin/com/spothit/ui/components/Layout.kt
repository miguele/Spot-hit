package com.spothit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SpotHitScaffold(
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 16.dp,
    topContent: @Composable ColumnScope.() -> Unit = {},
    bodyContent: @Composable ColumnScope.() -> Unit,
    actionsContent: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        topContent()
        bodyContent()
        actionsContent()
    }
}
