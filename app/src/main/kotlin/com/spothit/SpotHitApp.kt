package com.spothit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.spothit.ui.navigation.SpotHitNavGraph

@Composable
fun SpotHitApp(appContainer: AppContainer, viewModel: GameViewModel) {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        SpotHitNavGraph(navController = navController, viewModel = viewModel)
    }
}
