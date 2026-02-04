package app.krafted.spottheshade.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavHostController
import app.krafted.spottheshade.data.model.UserPreferences
import app.krafted.spottheshade.ui.screens.components.ThemeSelector
import app.krafted.spottheshade.ui.navigation.Screen
import app.krafted.spottheshade.ui.theme.LocalThemeColors
import app.krafted.spottheshade.ui.theme.LockedButtonEnd
import app.krafted.spottheshade.ui.theme.LockedButtonStart
import app.krafted.spottheshade.ui.theme.White
import app.krafted.spottheshade.viewmodel.GameViewModel
import androidx.compose.foundation.Image

@Composable
fun MainMenuScreen(
    navController: NavHostController,
    viewModel: GameViewModel
) {
    val userPreferences by viewModel.userPreferences.collectAsState(initial = UserPreferences())
    val themeColors = LocalThemeColors.current
    val context = LocalContext.current

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
                        .size(48.dp)
                        .background(
                            color = themeColors.overlayColor,
                            shape = CircleShape
                        )
                        .semantics {
                            contentDescription = if (userPreferences.soundEnabled) {
                                "Sound enabled. Tap to turn off sound"
                            } else {
                                "Sound disabled. Tap to turn on sound"
                            }
                            role = Role.Button
                        }
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
                onClick = { navController.navigate(Screen.Gameplay.route) },
                modifier = Modifier.padding(bottom = 24.dp),
                contentDescription = "Start new game. Current high score: ${userPreferences.highScore}"
            )

            // Daily Challenge button - use theme-specific secondary colors
            GradientButton(
                text = "DAILY CHALLENGE",
                subText = null,
                gradientColors = listOf(LockedButtonStart, LockedButtonEnd),
                textColor = themeColors.textOnButton.copy(alpha = 0.6f),
                onClick = {
                    Toast.makeText(context, "Coming Soon!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.padding(bottom = 24.dp),
                contentDescription = "Daily challenge is locked. This feature is coming soon."
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
                modifier = Modifier.padding(bottom = 16.dp)
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
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(25.dp)
            )
            .then(
                if (contentDescription != null) {
                    Modifier.semantics {
                        this.contentDescription = contentDescription
                        role = Role.Button
                    }
                } else Modifier
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
                if (subText != null) {
                    Text(
                        text = text,
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subText,
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = text,
                            color = textColor,
                            fontSize = 20.sp, // Consistent font size
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Add some space between text and icon
                        Image(
                            painter = painterResource(id = app.krafted.spottheshade.R.drawable.ic_locked),
                            contentDescription = "Locked",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
