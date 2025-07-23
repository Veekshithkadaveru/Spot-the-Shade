package com.example.spottheshade.ui.screens.components

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
import com.example.spottheshade.data.model.ThemeType
import com.example.spottheshade.data.model.UserPreferences
import com.example.spottheshade.data.repository.HapticManager
import com.example.spottheshade.ui.theme.*

@Composable
fun ThemeSelector(
    userPreferences: UserPreferences,
    onThemeSelected: (ThemeType) -> Unit,
    onUnlockTheme: (ThemeType) -> Unit,
    modifier: Modifier = Modifier,
    hapticManager: HapticManager? = null
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
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(ThemeType.entries.toTypedArray()) { theme ->
                ThemeCard(
                    theme = theme,
                    isUnlocked = userPreferences.unlockedThemes.contains(theme),
                    isSelected = userPreferences.currentTheme == theme,
                    onThemeSelected = onThemeSelected,
                    onUnlockTheme = onUnlockTheme,
                    hapticManager = hapticManager
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
    onUnlockTheme: (ThemeType) -> Unit,
    modifier: Modifier = Modifier,
    hapticManager: HapticManager? = null
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
            .clickable {

                if (hapticManager != null) {
                    hapticManager.themeSelect(haptic)
                } else {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                if (isUnlocked) {
                    onThemeSelected(theme)
                } else {
                    // TODO: REWARDED AD INTEGRATION - Theme Unlock UI
                    // When user clicks on locked theme, this should:
                    // 1. Show a dialog/bottom sheet explaining the theme unlock
                    // 2. Display "Watch Ad to Unlock" button
                    // 3. Show loading state while ad loads
                    // 4. Handle ad failures gracefully with alternative options
                    // 5. Provide visual feedback when theme is successfully unlocked
                    onUnlockTheme(theme)
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
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
            } else {
                // TODO: REWARDED AD INTEGRATION - Locked Theme UI
                // This locked state UI should be enhanced to:
                // 1. Show attractive "Watch Ad" or unlock requirement clearly
                // 2. Add animated pulsing effect to grab attention
                // 3. Display estimated unlock time for ads (e.g., "30 sec ad")
                // 4. Show loading spinner when ad is loading
                // 5. Provide alternative unlock paths (achievements vs ads)
                
                // Locked state
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
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
    }
} 