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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import app.krafted.spottheshade.R
import app.krafted.spottheshade.data.model.ThemeType
import app.krafted.spottheshade.data.model.UserPreferences
import app.krafted.spottheshade.data.model.unlockRequirement
import app.krafted.spottheshade.ui.theme.getThemeColors

import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun ThemeUnlockDialog(
    theme: ThemeType,
    userPreferences: UserPreferences,
    onDismiss: () -> Unit,
    onUnlockWithAd: ((android.app.Activity) -> Unit)? = null
) {
    val themeColors = getThemeColors(theme)
    val requirement = theme.unlockRequirement()
    val currentProgress = getPlayerProgress(theme, userPreferences)
    val progress = (currentProgress.toFloat() / requirement.second.coerceAtLeast(1)).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "progress_animation"
    )

    val context = LocalContext.current
    
    // Key change: don't remember loading state locally if we want it to reset 
    // when the user preferences update (e.g. ad finishes).
    // Or, tie the loading state reset to a change in the userPreferences ad progress count.
    var isAdLoading by remember { mutableStateOf(false) }
    val adsWatched = userPreferences.themeAdProgress[theme.name] ?: 0

    // Reset loading state if the ads watched count changes
    LaunchedEffect(adsWatched) {
        isAdLoading = false
    }

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
                    text = stringResource(R.string.theme_title_format, getThemeEmoji(theme), getThemeDisplayName(theme)),
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
                                        listOf(
                                            colors.getOrElse(1) { colors.last() },
                                            colors.getOrElse(3) { colors.last() },
                                            colors.getOrElse(5) { colors.last() }
                                        )
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
                        text = stringResource(R.string.theme_locked_status),
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

                if (onUnlockWithAd != null) {
                    Button(
                        onClick = {
                            val activity = context as? android.app.Activity
                            if (activity != null && !isAdLoading) {
                                isAdLoading = true
                                onUnlockWithAd(activity)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00D4AA)
                        ),
                        enabled = !isAdLoading
                    ) {
                        if (isAdLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            val adsWatched = userPreferences.themeAdProgress[theme.name] ?: 0
                            Text(
                                text = "Watch Ad ($adsWatched/3)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }

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
                        text = stringResource(R.string.keep_playing),
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
 * Returns the player's current progress toward unlocking the theme.
 */
fun getPlayerProgress(theme: ThemeType, prefs: UserPreferences): Int {
    val (type, _) = theme.unlockRequirement()
    return when (type) {
        "level" -> prefs.highestLevel
        "score" -> prefs.highScore
        else -> 0
    }
}

private fun getUnlockRequirementText(theme: ThemeType): String {
    val (type, target) = theme.unlockRequirement()
    return when (type) {
        "level" -> "Reach Level $target to unlock"
        "score" -> "Score $target Points to unlock"
        else -> "Keep playing to unlock"
    }
}

private fun getProgressText(theme: ThemeType, current: Int, target: Int): String {
    val (type, _) = theme.unlockRequirement()
    return when (type) {
        "level" -> "Your Level: $current / $target"
        "score" -> "Your High Score: $current / $target"
        else -> "Progress: $current / $target"
    }
}
