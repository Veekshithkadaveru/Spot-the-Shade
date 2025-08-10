package com.example.spottheshade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.spottheshade.data.model.UserPreferences
import com.example.spottheshade.services.SoundManager
import com.example.spottheshade.navigation.GameNavGraph
import com.example.spottheshade.ui.theme.SpotTheShadeTheme
import com.example.spottheshade.viewmodel.GameViewModel
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