package com.spothit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.spothit.ui.navigation.SpotHitNavGraph

@Composable
fun SpotHitApp(appContainer: AppContainer = remember { AppContainer() }) {
    val navController = rememberNavController()
    val viewModel: GameViewModel = viewModel(factory = appContainer.viewModelFactory)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        SpotHitNavGraph(navController = navController, viewModel = viewModel)
    }
}
