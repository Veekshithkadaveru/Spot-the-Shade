package app.krafted.spottheshade.ui.screens.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.krafted.spottheshade.data.model.ThemeType
import app.krafted.spottheshade.ui.theme.getThemeColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemeUnlockedCelebration(
    unlockedThemes: List<ThemeType>,
    onUseTheme: (ThemeType) -> Unit,
    onContinue: () -> Unit
) {
    if (unlockedThemes.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { unlockedThemes.size })
    val currentTheme = unlockedThemes[pagerState.currentPage]

    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val sparkleScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_scale"
    )

    Dialog(onDismissRequest = { /* Prevent dismissing by tapping outside */ }) {
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
                Row(
                    modifier = Modifier
                        .scale(sparkleScale)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) {
                        Text(
                            text = "\u2728",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                Text(
                    text = if (unlockedThemes.size == 1) {
                        "\uD83C\uDF89 NEW THEME UNLOCKED! \uD83C\uDF89"
                    } else {
                        "\uD83C\uDF89 NEW THEMES UNLOCKED! \uD83C\uDF89"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center
                )

                if (unlockedThemes.size > 1) {
                    Text(
                        text = "(${unlockedThemes.size} themes)",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val theme = unlockedThemes[page]
                    ThemePreviewCard(theme = theme)
                }

                if (unlockedThemes.size > 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        unlockedThemes.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (index == pagerState.currentPage) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == pagerState.currentPage) {
                                            Color(0xFFFFD700)
                                        } else {
                                            Color.White.copy(alpha = 0.3f)
                                        }
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val themeColors = getThemeColors(currentTheme)
                    Button(
                        onClick = { onUseTheme(currentTheme) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.gradientColors.getOrElse(3) { Color(0xFF6366F1) }
                        )
                    ) {
                        Text(
                            text = "Use Now",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onContinue,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Continue",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(theme: ThemeType) {
    val themeColors = getThemeColors(theme)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "${getThemeEmoji(theme)} ${getThemeDisplayName(theme)}",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(20.dp))
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.1f)
                            ),
                            radius = 150f
                        )
                    )
            )

            Text(
                text = "\u2713",
                fontSize = 48.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = getUnlockAchievementText(theme),
            fontSize = 14.sp,
            color = Color(0xFF4CAF50),
            textAlign = TextAlign.Center
        )
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

private fun getUnlockAchievementText(theme: ThemeType): String {
    return when (theme) {
        ThemeType.DEFAULT -> "Always available!"
        ThemeType.FOREST -> "You reached Level 10!"
        ThemeType.OCEAN -> "You reached Level 20!"
        ThemeType.SUNSET -> "You reached Level 30!"
        ThemeType.WINTER -> "You reached Level 40!"
        ThemeType.SPRING -> "You reached Level 50!"
        ThemeType.NEON_CYBER -> "You scored 1000 points!"
        ThemeType.VOLCANIC -> "You scored 2000 points!"
        ThemeType.ROYAL_GOLD -> "You scored 5000 points!"
    }
}
