package com.example.spottheshade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.spottheshade.navigation.GameNavGraph
import com.example.spottheshade.ui.theme.SpotTheShadeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpotTheShadeTheme {
                val navController = rememberNavController()
                GameNavGraph(navController = navController)
            }
        }
    }
}