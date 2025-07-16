package com.example.spottheshade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.spottheshade.data.model.GameResult
import com.example.spottheshade.data.model.UserPreferences
import com.example.spottheshade.navigation.Screen
import com.example.spottheshade.ui.theme.GradientGreen
import com.example.spottheshade.ui.theme.PurpleBackground
import com.example.spottheshade.ui.theme.White
import com.example.spottheshade.viewmodel.GameViewModel

@Composable
fun GameOverScreen(
    navController: NavHostController,
    finalScore: Int,
    finalLevel: Int,
    viewModel: GameViewModel = hiltViewModel()
) {
    val userPreferences by viewModel.userPreferences.collectAsState(initial = UserPreferences())
    val gameState by viewModel.gameState.collectAsState()
    
    // Determine the game over message based on how the game ended
    val gameOverMessage = when (gameState.lastEndingReason) {
        GameResult.Timeout -> "Out of Time!"
        GameResult.Wrong -> "Wrong Shade!"
        else -> "Game Over!"
    }
    
    // Gradient background colors
    val gradientColors = listOf(
        Color(0xFF8B7CE8), // Light purple
        Color(0xFF6B5B95)  // Darker purple
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(gradientColors)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title with better visibility
            Text(
                text = gameOverMessage,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 40.dp)
            )
            
            // Premium Score Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1A1A2E), // Dark navy
                                    Color(0xFF16213E), // Darker navy
                                    Color(0xFF0F3460)  // Deep blue
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Score section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "FINAL SCORE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64B5F6), // Light blue
                                letterSpacing = 2.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = String.format("%,d", finalScore),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700) // Gold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(1.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Best score section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (finalScore > userPreferences.highScore) "NEW BEST!" else "PREVIOUS BEST",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (finalScore > userPreferences.highScore) 
                                    Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                                letterSpacing = 1.5.sp
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = String.format("%,d", userPreferences.highScore),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (finalScore > userPreferences.highScore) 
                                    Color(0xFF4CAF50) else Color.White
                            )
                            
                            // Achievement indicator
                            if (finalScore > userPreferences.highScore) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF4CAF50).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(50)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "🏆",
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Retry Button
            Button(
                onClick = { navController.navigate(Screen.Gameplay.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientGreen
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RETRY",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main Menu Button
            Button(
                onClick = { navController.navigate(Screen.MainMenu.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B73FF) // Blue color
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MAIN MENU",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
    }
} 