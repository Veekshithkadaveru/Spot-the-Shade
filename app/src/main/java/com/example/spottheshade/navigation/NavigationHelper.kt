package com.example.spottheshade.navigation

import androidx.navigation.NavHostController
import com.example.spottheshade.data.repository.ErrorFeedbackManager
import com.example.spottheshade.data.repository.UserError
import com.example.spottheshade.navigation.Screen
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationHelper @Inject constructor(
    private val errorFeedbackManager: ErrorFeedbackManager
) {
    
    fun safeNavigate(
        navController: NavHostController,
        route: String,
        fallbackRoute: String = Screen.MainMenu.route,
        popUpTo: String? = null,
        inclusive: Boolean = false
    ) {
        try {
            if (popUpTo != null) {
                navController.navigate(route) {
                    popUpTo(popUpTo) { 
                        this.inclusive = inclusive 
                    }
                }
            } else {
                navController.navigate(route)
            }
        } catch (e: Exception) {
            android.util.Log.e("NavigationHelper", "Navigation failed from current to $route", e)
            errorFeedbackManager.showError(UserError.NavigationFailed)
            
            // Try fallback navigation
            try {
                navController.navigate(fallbackRoute) {
                    popUpTo(Screen.MainMenu.route) { this.inclusive = false }
                }
            } catch (fallbackError: Exception) {
                android.util.Log.e("NavigationHelper", "Fallback navigation also failed", fallbackError)
                // If even fallback fails, try to pop back stack
                try {
                    navController.popBackStack()
                } catch (popError: Exception) {
                    android.util.Log.e("NavigationHelper", "All navigation recovery attempts failed", popError)
                }
            }
        }
    }
    
    fun safePopBackStack(
        navController: NavHostController,
        route: String? = null,
        inclusive: Boolean = false
    ) {
        try {
            if (route != null) {
                navController.popBackStack(route, inclusive)
            } else {
                navController.popBackStack()
            }
        } catch (e: Exception) {
            android.util.Log.e("NavigationHelper", "Pop back stack failed", e)
            errorFeedbackManager.showError(UserError.NavigationFailed)
            
            // Try to navigate to main menu as last resort
            try {
                navController.navigate(Screen.MainMenu.route) {
                    popUpTo(Screen.MainMenu.route) { this.inclusive = false }
                }
            } catch (fallbackError: Exception) {
                android.util.Log.e("NavigationHelper", "Emergency navigation to main menu failed", fallbackError)
            }
        }
    }
}