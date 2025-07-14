package com.example.spottheshade.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spottheshade.data.model.GameResult

@Composable
fun GameResultOverlay(
    gameResult: GameResult?,
    lives: Int,
    onContinue: () -> Unit,
    onDeclineExtraTime: () -> Unit,
    onUseExtraTime: () -> Unit,
    onGoToMenu: () -> Unit
) {
    AnimatedVisibility(
        visible = gameResult in listOf(GameResult.Wrong, GameResult.Timeout, GameResult.OfferContinue),
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                .clickable(enabled = false, onClick = {}), // Block background clicks
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when (gameResult) {
                            GameResult.Wrong -> "Wrong Shade!"
                            GameResult.Timeout -> "Time's Up!"
                            GameResult.OfferContinue -> "Use a life to continue?"
                            else -> ""
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Show options based on result
                    when (gameResult) {
                        GameResult.Wrong -> {
                            Button(
                                onClick = onContinue,
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Try Again")
                            }
                        }
                        GameResult.Timeout -> {
                            Text(
                                text = "Watch an ad for 5 extra seconds?",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Row(
                                modifier = Modifier.padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = onUseExtraTime) {
                                    Text("Get Extra Time")
                                }
                                Button(
                                    onClick = onDeclineExtraTime,
                                    modifier = Modifier.padding(start = 16.dp)
                                ) {
                                    Text("No Thanks")
                                }
                            }
                        }
                        GameResult.OfferContinue -> {
                            Text(
                                text = "You have $lives ${if (lives == 1) "life" else "lives"} left.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Row(
                                modifier = Modifier.padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = onContinue) {
                                    Text("Continue")
                                }
                                Button(
                                    onClick = onGoToMenu,
                                    modifier = Modifier.padding(start = 16.dp)
                                ) {
                                    Text("Main Menu")
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
} 