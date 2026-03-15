package app.krafted.spottheshade.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import app.krafted.spottheshade.R
import app.krafted.spottheshade.data.model.GameResult

import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun GameResultOverlay(
    gameResult: GameResult?,
    lives: Int,
    onContinue: () -> Unit,
    onDeclineExtraTime: () -> Unit,
    onUseExtraTime: (android.content.Context) -> Unit,
    onGoToMenu: () -> Unit
) {
    val context = LocalContext.current
    var isAdLoading by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = gameResult in listOf(GameResult.Wrong, GameResult.Timeout, GameResult.OfferContinue),
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)) + scaleOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Main game dialog with modern styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF1A1A2E),
                                Color(0xFF16213E),
                                Color(0xFF0F3460)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .padding(32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Icon and title section
                    when (gameResult) {
                        GameResult.Wrong -> {
                            // Pulsing error effect
                            val infiniteTransition = rememberInfiniteTransition(label = "errorPulse")
                            val pulseScale = infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600),
                                    repeatMode = RepeatMode.Reverse
                                ), label = "pulse"
                            ).value

                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.wrong_shade_icon_description),
                                tint = Color(0xFFFF4757),
                                modifier = Modifier
                                    .size(64.dp)
                                    .graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                    }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(R.string.wrong_shade_overlay),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF4757),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = stringResource(R.string.dont_give_up),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        GameResult.Timeout -> {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.times_up_icon_description),
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(R.string.times_up),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD700),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = stringResource(R.string.need_more_time),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        GameResult.OfferContinue -> {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = stringResource(R.string.continue_icon_description),
                                tint = Color(0xFF4ECDC4),
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(R.string.continue_question),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4ECDC4),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = stringResource(R.string.lives_remaining_text, lives, if (lives == 1) stringResource(R.string.life_singular) else stringResource(R.string.lives_plural)),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        else -> {}
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Action buttons with game-like styling
                    when (gameResult) {
                        GameResult.Wrong -> {
                            GameButton(
                                text = stringResource(R.string.try_again),
                                onClick = onContinue,
                                gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
                                icon = Icons.Default.Refresh
                            )
                        }

                        GameResult.Timeout -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                GameButton(
                                    text = if (isAdLoading) "Loading..." else stringResource(R.string.watch_ad),
                                    onClick = {
                                        if (!isAdLoading) {
                                            isAdLoading = true
                                            onUseExtraTime(context)
                                            // Ideally, reset loading state when ad completes or fails
                                        }
                                    },
                                    gradientColors = listOf(Color(0xFF00D4AA), Color(0xFF00C9FF)),
                                    icon = null
                                )

                                GameButton(
                                    text = stringResource(R.string.no_thanks),
                                    onClick = onDeclineExtraTime,
                                    gradientColors = listOf(Color(0xFF6C7B7F), Color(0xFF4A5568)),
                                    icon = Icons.Default.Close,
                                    isSecondary = true
                                )
                            }
                        }

                        GameResult.OfferContinue -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                GameButton(
                                    text = stringResource(R.string.continue_button),
                                    onClick = onContinue,
                                    gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
                                    icon = Icons.Default.Star
                                )

                                GameButton(
                                    text = stringResource(R.string.main_menu),
                                    onClick = onGoToMenu,
                                    gradientColors = listOf(Color(0xFF6C7B7F), Color(0xFF4A5568)),
                                    icon = Icons.Default.Close,
                                    isSecondary = true
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun GameButton(
    text: String,
    onClick: () -> Unit,
    gradientColors: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    isSecondary: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = if (isSecondary) 1.dp else 2.dp,
                    color = Color.White.copy(alpha = if (isSecondary) 0.2f else 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(0.95f)
                )
            }
        }
    }
}
