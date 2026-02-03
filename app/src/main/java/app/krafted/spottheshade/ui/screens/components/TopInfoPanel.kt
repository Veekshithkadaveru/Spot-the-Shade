package app.krafted.spottheshade.ui.screens.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.krafted.spottheshade.ui.theme.ThemeColors

@Composable
fun TopInfoPanel(
    level: Int,
    score: Int,
    lives: Int,
    highScore: Int,
    timeRemaining: Int,
    themeColors: ThemeColors
) {
    // Timer pulse animation when time is low
    val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
    val pulseScale = if (timeRemaining in 1..5) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            ), label = "pulse"
        ).value
    } else {
        1f
    }

    // Theme-appropriate background with semi-transparency
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = themeColors.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
            .semantics {
                contentDescription = "Game status: Level $level, Score $score out of $highScore best, $lives lives remaining, $timeRemaining seconds left"
            }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top row: Level and Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level section
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Level $level",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.onSurface
                    )
                    val difficulty = when {
                        level <= 10 -> "Easy"
                        level <= 25 -> "Medium"
                        level <= 40 -> "Hard"
                        level <= 55 -> "Expert"
                        level <= 70 -> "Master"
                        else -> "Legendary"
                    }
                    Text(
                        text = difficulty,
                        style = MaterialTheme.typography.labelMedium,
                        color = themeColors.onSurface.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                // Score section
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Score star",
                            tint = Color(0xFFFFD700), // Keep gold star
                            modifier = Modifier.size(16.dp)
                        )
                        // Animated score
                        AnimatedContent(
                            targetState = score,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(200)) togetherWith
                                        fadeOut(animationSpec = tween(200))
                            },
                            label = "score"
                        ) { targetScore ->
                            Text(
                                text = " $targetScore",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.onSurface
                            )
                        }
                    }
                    Text(
                        text = "Best: $highScore",
                        style = MaterialTheme.typography.labelMedium,
                        color = themeColors.onSurface.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // Bottom row: Lives and Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lives section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics {
                        contentDescription = "$lives out of 3 lives remaining"
                    }
                ) {
                    Text(
                        text = "Lives: ",
                        style = MaterialTheme.typography.titleMedium,
                        color = themeColors.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    // Animated hearts
                    Row {
                        for (i in 1..3) {
                            val filled = i <= lives
                            Crossfade(
                                targetState = filled,
                                animationSpec = tween(500),
                                label = "heart"
                            ) { isFilled ->
                                Text(
                                    text = if (isFilled) "❤️" else "🤍",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(end = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Timer section with pulse animation
                Text(
                    text = "Time: ${timeRemaining}s",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        timeRemaining <= 5 -> Color.Red
                        timeRemaining <= 10 -> Color(0xFFFF8C00) // Orange
                        else -> themeColors.onSurface
                    },
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .semantics {
                            contentDescription = when {
                                timeRemaining <= 5 -> "$timeRemaining seconds remaining - time is running out!"
                                timeRemaining <= 10 -> "$timeRemaining seconds remaining - hurry up!"
                                else -> "$timeRemaining seconds remaining"
                            }
                        }
                )
            }
        }
    }
}
