package com.example.spottheshade.ui.screens.components

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TopInfoPanel(
    level: Int,
    score: Int,
    lives: Int,
    highScore: Int,
    timeRemaining: Int
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Level: $level",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
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
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
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
                        text = "Score: $targetScore",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Best: $highScore",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Timer and Lives
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Lives: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
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
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }

            // Timer with pulse animation when time is low
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

            Text(
                text = "Time: ${timeRemaining}s",
                style = MaterialTheme.typography.titleLarge,
                color = when {
                    timeRemaining <= 5 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
            )
        }
    }
} 