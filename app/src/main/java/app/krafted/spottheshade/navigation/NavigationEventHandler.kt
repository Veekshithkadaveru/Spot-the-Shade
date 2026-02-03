package app.krafted.spottheshade.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import app.krafted.spottheshade.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Composable that handles navigation events from the ViewModel.
 * This keeps navigation logic triggered by ViewModel state changes
 * while the actual NavController manipulation happens in the UI layer.
 */
@Composable
fun NavigationEventHandler(
    navController: NavHostController,
    viewModel: GameViewModel
) {
    LaunchedEffect(viewModel.navigationEvents) {
        viewModel.navigationEvents.collectLatest { event ->
            when (event) {
                NavigationEvent.NavigateToGameplay -> {
                    viewModel.navigationHelper.safeNavigate(
                        navController = navController,
                        route = Screen.Gameplay.route
                    )
                }
                NavigationEvent.NavigateToMainMenu -> {
                    viewModel.navigationHelper.safeNavigate(
                        navController = navController,
                        route = Screen.MainMenu.route
                    )
                }
                is NavigationEvent.NavigateToGameOver -> {
                    viewModel.navigationHelper.safeNavigate(
                        navController = navController,
                        route = Screen.GameOver.createRoute(event.score, event.level),
                        popUpTo = Screen.MainMenu.route,
                        inclusive = false
                    )
                }
                NavigationEvent.PopBackStack -> {
                    viewModel.navigationHelper.safePopBackStack(navController)
                }
            }
        }
    }
}
