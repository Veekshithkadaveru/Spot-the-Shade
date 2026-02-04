package app.krafted.spottheshade.ui.screens.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.krafted.spottheshade.data.model.ThemeType
import app.krafted.spottheshade.data.model.UserPreferences
import app.krafted.spottheshade.ui.theme.getThemeColors

@Composable
fun ThemeUnlockDialog(
    theme: ThemeType,
    userPreferences: UserPreferences,
    onDismiss: () -> Unit
) {
    val themeColors = getThemeColors(theme)
    val requirement = getUnlockRequirement(theme)
    val currentProgress = getPlayerProgress(theme, userPreferences)
    val progress = (currentProgress.toFloat() / requirement.second).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "progress_animation"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A2E)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${getThemeEmoji(theme)} ${getThemeDisplayName(theme)} Theme",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = themeColors.gradientColors.let { colors ->
                                    if (colors.size >= 4) {
                                        listOf(colors[1], colors[3], colors[5])
                                    } else {
                                        colors
                                    }
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Locked",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD700)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = getUnlockRequirementText(theme),
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = getProgressText(theme, currentProgress, requirement.second),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        themeColors.gradientColors.getOrElse(2) { Color.Cyan },
                                        themeColors.gradientColors.getOrElse(4) { Color.Blue }
                                    )
                                )
                            )
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.gradientColors.getOrElse(3) { Color(0xFF6366F1) }
                    )
                ) {
                    Text(
                        text = "Keep Playing!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
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

private fun getThemeEmoji(theme: ThemeType): String {
    return when (theme) {
        ThemeType.DEFAULT -> ""
        ThemeType.FOREST -> "\uD83C\uDF32"
        ThemeType.OCEAN -> "\uD83C\uDF0A"
        ThemeType.SUNSET -> "\uD83C\uDF05"
        ThemeType.WINTER -> "\u2744\uFE0F"
        ThemeType.SPRING -> "\uD83C\uDF38"
        ThemeType.NEON_CYBER -> "\u26A1"
        ThemeType.VOLCANIC -> "\uD83C\uDF0B"
        ThemeType.ROYAL_GOLD -> "\uD83D\uDC51"
    }
}

/**
 * Returns the unlock requirement type and target value for a theme.
 * @return Pair of (requirementType: "level" or "score", targetValue: Int)
 */
fun getUnlockRequirement(theme: ThemeType): Pair<String, Int> {
    return when (theme) {
        ThemeType.DEFAULT -> Pair("level", 0)
        ThemeType.FOREST -> Pair("level", 10)
        ThemeType.OCEAN -> Pair("level", 20)
        ThemeType.SUNSET -> Pair("level", 30)
        ThemeType.WINTER -> Pair("level", 40)
        ThemeType.SPRING -> Pair("level", 50)
        ThemeType.NEON_CYBER -> Pair("score", 1000)
        ThemeType.VOLCANIC -> Pair("score", 2000)
        ThemeType.ROYAL_GOLD -> Pair("score", 5000)
    }
}

/**
 * Returns the player's current progress toward unlocking the theme.
 */
fun getPlayerProgress(theme: ThemeType, prefs: UserPreferences): Int {
    val requirement = getUnlockRequirement(theme)
    return when (requirement.first) {
        "level" -> prefs.highestLevel
        "score" -> prefs.highScore
        else -> 0
    }
}

private fun getUnlockRequirementText(theme: ThemeType): String {
    val requirement = getUnlockRequirement(theme)
    return when (requirement.first) {
        "level" -> "Reach Level ${requirement.second} to unlock"
        "score" -> "Score ${requirement.second} Points to unlock"
        else -> "Keep playing to unlock"
    }
}

private fun getProgressText(theme: ThemeType, current: Int, target: Int): String {
    val requirement = getUnlockRequirement(theme)
    return when (requirement.first) {
        "level" -> "Your Level: $current / $target"
        "score" -> "Your High Score: $current / $target"
        else -> "Progress: $current / $target"
    }
}
