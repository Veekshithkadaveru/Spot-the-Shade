package com.example.spottheshade.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.spottheshade.ui.screens.GameplayScreen
import com.example.spottheshade.ui.screens.GameOverScreen
import com.example.spottheshade.ui.screens.MainMenuScreen

sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object Gameplay : Screen("gameplay")
    object GameOver : Screen("game_over")
}

@Composable
fun GameNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.MainMenu.route) {
        composable(Screen.MainMenu.route) { MainMenuScreen(navController) }
        composable(Screen.Gameplay.route) { GameplayScreen(navController) }
        composable(Screen.GameOver.route) { GameOverScreen(navController) }
    }
} 