package com.example.spottheshade.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.spottheshade.navigation.Screen
import com.example.spottheshade.viewmodel.GameViewModel
import com.example.spottheshade.data.model.UserPreferences

@Composable
fun GameOverScreen(
    navController: NavHostController,
    finalScore: Int,
    finalLevel: Int,
    viewModel: GameViewModel = hiltViewModel()
) {
    val userPreferences by viewModel.userPreferences.collectAsState(initial = UserPreferences())
    val isNewHighScore = finalScore > userPreferences.highScore
    val isNewHighLevel = finalLevel > userPreferences.highestLevel
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Over",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp),
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = if (isNewHighScore) "🎉 NEW HIGH SCORE! 🎉" else "Final Score",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            color = if (isNewHighScore) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$finalScore",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            fontWeight = FontWeight.Bold,
            color = if (isNewHighScore) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        if (!isNewHighScore) {
            Text(
                text = "Previous High Score: ${userPreferences.highScore}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            Text(
                text = "Previous: ${userPreferences.highScore}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        Text(
            text = if (isNewHighLevel) "🏆 NEW LEVEL RECORD! 🏆" else "Highest Level Reached",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            color = if (isNewHighLevel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Level $finalLevel",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp),
            fontWeight = FontWeight.SemiBold,
            color = if (isNewHighLevel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
        )
        if (!isNewHighLevel) {
            Text(
                text = "Previous Best: Level ${userPreferences.highestLevel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            Text(
                text = "Previous: Level ${userPreferences.highestLevel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Game Statistics
        Text(
            text = "📊 Your Stats",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        Text(
            text = "Games Played: ${userPreferences.totalGamesPlayed} • Correct Answers: ${userPreferences.totalCorrectAnswers}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate(Screen.Gameplay.route) }
            ) {
                Text("Play Again")
            }
            Button(
                onClick = { navController.navigate(Screen.MainMenu.route) }
            ) {
                Text("Main Menu")
            }
        }
    }
} 