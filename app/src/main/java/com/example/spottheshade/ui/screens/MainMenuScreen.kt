package com.example.spottheshade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.spottheshade.data.model.UserPreferences
import com.example.spottheshade.navigation.Screen
import com.example.spottheshade.ui.screens.components.ThemeSelector
import com.example.spottheshade.ui.theme.DailyChallengeEnd
import com.example.spottheshade.ui.theme.DailyChallengeStart
import com.example.spottheshade.ui.theme.GradientGreen
import com.example.spottheshade.ui.theme.GradientOrange
import com.example.spottheshade.ui.theme.GradientYellow
import com.example.spottheshade.ui.theme.LocalThemeColors
import com.example.spottheshade.ui.theme.PlayButtonEnd
import com.example.spottheshade.ui.theme.PlayButtonStart
import com.example.spottheshade.ui.theme.White
import com.example.spottheshade.viewmodel.GameViewModel

@Composable
fun MainMenuScreen(
    navController: NavHostController,
    viewModel: GameViewModel = hiltViewModel()
) {
    val userPreferences by viewModel.userPreferences.collectAsState(initial = UserPreferences())
    val themeColors = LocalThemeColors.current
    val hapticManager = viewModel.getHapticManager()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Natural wallpaper-like gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = themeColors.gradientColors
                    )
                )
        )

        // Bottom section with surface gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.15f)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = themeColors.surfaceGradient
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top section with sound toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { viewModel.toggleSound() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = themeColors.overlayColor,
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = if (userPreferences.soundEnabled) "🔊" else "🔇",
                        fontSize = 20.sp,
                        color = themeColors.iconColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Game title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 64.dp)
            ) {
                Text(
                    text = "Spot the Shade",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Cursive,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.8f),
                            offset = Offset(4f, 4f),
                            blurRadius = 8f
                        )
                    ),
                    color = themeColors.titleColor
                )
            }

            // Play button - use theme-specific button colors
            GradientButton(
                text = "PLAY",
                subText = "HIGH SCORE: ${if (userPreferences.highScore > 0) "${userPreferences.highScore}" else "0"}",
                gradientColors = themeColors.buttonPrimary,
                textColor = themeColors.textOnButton,
                onClick = { 
                    viewModel.navigationHelper.safeNavigate(
                        navController = navController,
                        route = Screen.Gameplay.route
                    )
                },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Daily Challenge button - use theme-specific secondary colors
            GradientButton(
                text = "DAILY\nCHALLENGE",
                subText = null,
                gradientColors = themeColors.buttonSecondary,
                textColor = themeColors.textOnButton,
                onClick = {
                    // TODO: Implement daily challenge navigation
                    viewModel.navigationHelper.safeNavigate(
                        navController = navController,
                        route = Screen.Gameplay.route
                    )
                },
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Theme Selector
            ThemeSelector(
                userPreferences = userPreferences,
                onThemeSelected = { theme ->
                    viewModel.setCurrentTheme(theme)
                },
                onUnlockTheme = { theme ->
                    viewModel.unlockThemeWithRewardedAd(theme)
                },
                modifier = Modifier.padding(bottom = 16.dp),
                hapticManager = hapticManager
            )
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    subText: String?,
    gradientColors: List<Color>,
    textColor: Color = White,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(if (subText != null) 80.dp else 70.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(25.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(25.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(gradientColors),
                    shape = RoundedCornerShape(25.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = if (subText != null) 20.sp else 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (subText != null) {
                    Text(
                        text = subText,
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
} 