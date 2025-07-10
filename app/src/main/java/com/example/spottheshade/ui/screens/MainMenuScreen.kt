package com.example.spottheshade.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.spottheshade.navigation.Screen
import com.example.spottheshade.viewmodel.GameViewModel
import com.example.spottheshade.viewmodel.GameViewModelFactory
import com.example.spottheshade.data.model.UserPreferences

@Composable
fun MainMenuScreen(
    navController: NavHostController,
    viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(LocalContext.current))
) {
    val userPreferences by viewModel.userPreferences.collectAsState(initial = UserPreferences())
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Spot the Shade",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Show high scores if player has played before
        if (userPreferences.totalGamesPlayed > 0) {
            Card(
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Best",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "High Score: ${userPreferences.highScore}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Best Level: ${userPreferences.highestLevel}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Games Played: ${userPreferences.totalGamesPlayed}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        
        Button(
            onClick = { navController.navigate(Screen.Gameplay.route) }
        ) {
            Text("Start Game")
        }
    }
} 