package app.krafted.spottheshade.navigation

/**
 * Sealed class representing navigation events emitted from ViewModel.
 * UI layer collects these events and performs actual navigation.
 */
sealed class NavigationEvent {
    object NavigateToGameplay : NavigationEvent()
    object NavigateToMainMenu : NavigationEvent()
    data class NavigateToGameOver(val score: Int, val level: Int) : NavigationEvent()
    object PopBackStack : NavigationEvent()
}
