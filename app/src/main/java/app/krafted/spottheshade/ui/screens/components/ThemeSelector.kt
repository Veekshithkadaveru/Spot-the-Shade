package app.krafted.spottheshade.ui.screens.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import app.krafted.spottheshade.data.model.ThemeType
import app.krafted.spottheshade.data.model.UserPreferences
import app.krafted.spottheshade.ui.theme.*

@Composable
fun ThemeSelector(
    userPreferences: UserPreferences,
    onThemeSelected: (ThemeType) -> Unit,
    onLockedThemeTapped: (ThemeType) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎨 THEMES",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.titleColor,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.6f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            ),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .semantics {
                    contentDescription = "Theme selection section. Swipe to browse available themes"
                }
        )

        // Sort themes: unlocked first, then locked
        val sortedThemes = ThemeType.entries.sortedByDescending {
            userPreferences.unlockedThemes.contains(it)
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.semantics {
                contentDescription = "Horizontal scrollable list of game themes"
            }
        ) {
            items(sortedThemes) { theme ->
                ThemeCard(
                    theme = theme,
                    isUnlocked = userPreferences.unlockedThemes.contains(theme),
                    isSelected = userPreferences.currentTheme == theme,
                    onThemeSelected = onThemeSelected,
                    onLockedThemeTapped = onLockedThemeTapped
                )
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: ThemeType,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onThemeSelected: (ThemeType) -> Unit,
    onLockedThemeTapped: (ThemeType) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = getThemeColors(theme)
    val borderColor = if (isSelected) Color.White else Color.Transparent
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "theme_card_scale"
    )

    // Add rotation animation for extra visual appeal
    val rotation by animateFloatAsState(
        targetValue = if (isSelected) 2f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "theme_card_rotation"
    )

    Card(
        modifier = modifier
            .widthIn(min = 120.dp, max = 140.dp)
            .heightIn(min = 160.dp, max = 180.dp)
            .scale(scale)
            .graphicsLayer {
                rotationZ = rotation
            }
            .border(3.dp, borderColor, RoundedCornerShape(16.dp))
            .semantics {
                val themeName = getThemeDisplayName(theme)
                contentDescription = if (isUnlocked) {
                    if (isSelected) {
                        "$themeName theme - currently selected"
                    } else {
                        "$themeName theme - tap to select"
                    }
                } else {
                    "$themeName theme - locked. ${getUnlockText(theme)}. Tap to unlock with ad"
                }
                role = Role.Button
            }
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (isUnlocked) {
                    onThemeSelected(theme)
                } else {
                    // Show unlock dialog with requirements and progress
                    onLockedThemeTapped(theme)
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 12.dp else 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = themeColors.gradientColors.let { colors ->
                            // Use a subset of the gradient colors for preview
                            if (colors.size >= 4) {
                                listOf(colors[1], colors[3], colors[5])
                            } else {
                                colors
                            }
                        }
                    )
                )
        ) {
            // Theme preview - show actual gradient preview
            if (isUnlocked) {
                // Add a subtle shimmer effect for better visual appeal
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.05f)
                                ),
                                radius = 120f
                            )
                        )
                )

                // Add subtle animated dots to show theme activity
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Currently selected theme indicator",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
            } else {
                // Locked state - tap to show unlock requirements dialog
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Theme is locked",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = getUnlockText(theme),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .fillMaxWidth()
                    )
                }
            }

            // Theme name
            Text(
                text = getThemeDisplayName(theme),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            )
        }
    }
}

private fun getThemeDisplayName(theme: ThemeType): String {
    return when (theme) {
        ThemeType.DEFAULT -> "Default"
        ThemeType.FOREST -> "Forest"
        ThemeType.OCEAN -> "Ocean"
        ThemeType.SUNSET -> "Sunset"
        ThemeType.WINTER -> "Winter"
        ThemeType.SPRING -> "Spring"
        ThemeType.NEON_CYBER -> "Neon Cyber"
        ThemeType.VOLCANIC -> "Volcanic"
        ThemeType.ROYAL_GOLD -> "Royal Gold"
    }
}

private fun getUnlockText(theme: ThemeType): String {
    return when (theme) {
        ThemeType.DEFAULT -> "Unlocked"
        ThemeType.FOREST -> "🌲 Level 10"
        ThemeType.OCEAN -> "🌊 Level 20"
        ThemeType.SUNSET -> "🌅 Level 30"
        ThemeType.WINTER -> "❄️ Level 40"
        ThemeType.SPRING -> "🌸 Level 50"
        ThemeType.NEON_CYBER -> "⚡ 1000 Pts"
        ThemeType.VOLCANIC -> "🌋 2000 Pts"
        ThemeType.ROYAL_GOLD -> "\uD83D\uDC51 5000 Pts"
    }
}
