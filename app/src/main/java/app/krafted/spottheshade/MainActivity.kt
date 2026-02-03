package app.krafted.spottheshade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import app.krafted.spottheshade.data.model.UserPreferences
import app.krafted.spottheshade.services.SoundManager
import app.krafted.spottheshade.navigation.GameNavGraph
import app.krafted.spottheshade.navigation.NavigationEventHandler
import app.krafted.spottheshade.ui.theme.SpotTheShadeTheme
import app.krafted.spottheshade.ui.util.ErrorEventHandler
import app.krafted.spottheshade.viewmodel.GameViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var soundManager: SoundManager

    // TODO: REWARDED AD INTEGRATION - Initialize AdMob in MainActivity
    // Add these when ready to integrate ads:
    // 1. Initialize Mobile Ads SDK in onCreate:
    //    MobileAds.initialize(this) {}
    // 2. Create AdManager instance (inject via Hilt)
    // 3. Handle ad loading and lifecycle management
    // 4. Request consent form for GDPR compliance if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: GameViewModel = hiltViewModel()
            val userPreferences by viewModel.userPreferences.collectAsState(initial = UserPreferences())

            SpotTheShadeTheme(themeType = userPreferences.currentTheme) {
                val navController = rememberNavController()

                // Handle error events from data layer as Toasts
                ErrorEventHandler(viewModel)

                // Handle navigation events from ViewModel
                NavigationEventHandler(navController, viewModel)

                GameNavGraph(navController = navController)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Fallback cleanup for devices without ProcessLifecycleOwner
        soundManager.onAppBackground()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Fallback cleanup for devices without ProcessLifecycleOwner
        if (isFinishing) {
            soundManager.release()
        }
    }
}
