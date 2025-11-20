package com.spothit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.spothit.GameViewModel
import com.spothit.ui.screens.GameScreen
import com.spothit.ui.screens.HomeScreen
import com.spothit.ui.screens.LobbyScreen
import com.spothit.ui.screens.ResultsScreen

sealed class SpotHitDestination(val route: String) {
    data object Home : SpotHitDestination("home")
    data object Lobby : SpotHitDestination("lobby")
    data object Game : SpotHitDestination("game")
    data object Results : SpotHitDestination("results")
}

@Composable
fun SpotHitNavGraph(
    navController: NavHostController,
    viewModel: GameViewModel
) {
    NavHost(navController = navController, startDestination = SpotHitDestination.Home.route) {
        composable(SpotHitDestination.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToLobby = { navController.navigate(SpotHitDestination.Lobby.route) }
            )
        }
        composable(SpotHitDestination.Lobby.route) {
            LobbyScreen(
                viewModel = viewModel,
                onStartGame = { navController.navigate(SpotHitDestination.Game.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(SpotHitDestination.Game.route) {
            GameScreen(
                viewModel = viewModel,
                onShowResults = { navController.navigate(SpotHitDestination.Results.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(SpotHitDestination.Results.route) {
            ResultsScreen(
                viewModel = viewModel,
                onPlayAgain = {
                    viewModel.reset()
                    navController.popBackStack(SpotHitDestination.Home.route, inclusive = false)
                },
                onBackToHome = {
                    navController.popBackStack(SpotHitDestination.Home.route, inclusive = false)
                }
            )
        }
    }
}
