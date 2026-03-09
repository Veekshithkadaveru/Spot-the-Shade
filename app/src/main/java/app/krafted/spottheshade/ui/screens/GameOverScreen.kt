package app.krafted.spottheshade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
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
import app.krafted.spottheshade.data.model.GameResult
import app.krafted.spottheshade.data.model.UserPreferences
import app.krafted.spottheshade.ui.navigation.Screen
import app.krafted.spottheshade.ui.screens.components.ThemeUnlockedCelebration
import app.krafted.spottheshade.ui.theme.LocalThemeColors
import app.krafted.spottheshade.viewmodel.GameViewModel
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.geometry.Offset
import app.krafted.spottheshade.R
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavHostController

@Composable
fun GameOverScreen(
    navController: NavHostController,
    finalScore: Int,
    viewModel: GameViewModel
) {
    val userPreferences by viewModel.userPreferences.collectAsState(initial = UserPreferences())
    val gameState by viewModel.gameState.collectAsState()
    val newlyUnlockedThemes by viewModel.newlyUnlockedThemes.collectAsState()
    val themeColors = LocalThemeColors.current

    // Show theme unlock celebration dialog if there are newly unlocked themes
    if (newlyUnlockedThemes.isNotEmpty()) {
        ThemeUnlockedCelebration(
            unlockedThemes = newlyUnlockedThemes,
            onUseTheme = { theme ->
                viewModel.setCurrentTheme(theme)
                viewModel.clearNewlyUnlockedThemes()
            },
            onContinue = {
                viewModel.clearNewlyUnlockedThemes()
            }
        )
    }

    // Determine the game over message based on how the game ended
    val gameOverMessage = when (gameState.lastEndingReason) {
        GameResult.Timeout -> stringResource(R.string.out_of_time)
        GameResult.Wrong -> stringResource(R.string.wrong_shade_title)
        else -> stringResource(R.string.game_over_title)
    }

    val gameEndedDescription = stringResource(R.string.game_ended_description, gameOverMessage)
    val newHighScoreDescription = stringResource(R.string.new_high_score_description, finalScore, userPreferences.highScore)
    val finalScoreDescription = stringResource(R.string.final_score_description, finalScore, userPreferences.highScore)
    val startNewGameDescription = stringResource(R.string.start_new_game_description)
    val returnToMainMenuDescription = stringResource(R.string.return_to_main_menu_description)

    // Use theme colors for natural wallpaper background
    val gradientColors = themeColors.gradientColors

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
                color = themeColors.titleColor,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                ),
                modifier = Modifier
                    .padding(bottom = 40.dp)
                    .semantics {
                        contentDescription = gameEndedDescription
                    }
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
                                    Color(0xFF1A1A2E),
                                    Color(0xFF16213E),
                                    Color(0xFF0F3460)
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
                        .semantics {
                            contentDescription = if (finalScore > userPreferences.highScore) {
                                newHighScoreDescription
                            } else {
                                finalScoreDescription
                            }
                        }
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
                                text = stringResource(R.string.final_score_label),
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
                                text = if (finalScore > userPreferences.highScore) stringResource(R.string.new_best) else stringResource(R.string.previous_best),
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
                                        fontSize = 24.sp,
                                        modifier = Modifier.semantics {
                                            contentDescription = "Trophy for new high score achievement"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Consider adding these monetization features:
            // 1. "REVIVE" button - Watch ad to continue with extra lives
            // 2. "DOUBLE COINS" button - Watch ad to double any earned coins/points
            // 3. "UNLOCK PREMIUM THEME" button - Special offer after high scores
            // 4. Interstitial ad after every 3-5 games (non-intrusive timing)
            // 5. Banner ad at bottom (optional, less intrusive)

            // Retry Button
            Button(
                onClick = {
                    navController.navigate(Screen.Gameplay.route) {
                        popUpTo(Screen.MainMenu.route) {
                            inclusive = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
                    .semantics {
                        contentDescription = startNewGameDescription
                        role = Role.Button
                    },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.accent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh_icon_description),
                    tint = themeColors.textOnButton,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.retry),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textOnButton
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Menu Button
            Button(
                onClick = {
                    navController.navigate(Screen.MainMenu.route) {
                        popUpTo(Screen.MainMenu.route) {
                            inclusive = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
                    .semantics {
                        contentDescription = returnToMainMenuDescription
                        role = Role.Button
                    },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = stringResource(R.string.home_icon_description),
                    tint = themeColors.textOnButton,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.main_menu),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textOnButton
                )
            }
        }
    }
}
