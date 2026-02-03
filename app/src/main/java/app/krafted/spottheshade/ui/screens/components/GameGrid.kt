package app.krafted.spottheshade.ui.screens.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.krafted.spottheshade.data.model.ShapeType
import app.krafted.spottheshade.services.HapticManager
import app.krafted.spottheshade.ui.util.toComposeColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StaggeredGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val itemWidth = constraints.maxWidth / columns
        val itemConstraints = constraints.copy(
            minWidth = itemWidth,
            maxWidth = itemWidth
        )
        val placeables = measurables.map { measurable -> measurable.measure(itemConstraints) }
        val height = placeables.maxOfOrNull { placeable -> placeable.height } ?: 0

        val rows = (placeables.size + columns - 1) / columns
        layout(constraints.maxWidth, height * rows) {
            var x = 0
            var y = 0
            placeables.forEachIndexed { index, placeable ->
                placeable.placeRelative(x, y)
                if ((index + 1) % columns == 0) {
                    x = 0
                    y += height
                } else {
                    x += itemWidth
                }
            }
        }
    }
}

@Composable
fun GridItem(
    item: app.krafted.spottheshade.data.model.GridItem,
    itemSize: Dp,
    scale: Float,
    onTapped: () -> Unit,
    isRevealing: Boolean = false,
    hapticManager: HapticManager? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val pressScale = remember { Animatable(1f) }
    val haptic = LocalHapticFeedback.current

    // Multiple animation layers for beautiful reveal effect - only when revealing
    val infiniteTransition = rememberInfiniteTransition(label = "reveal")

    // Conditional animations to prevent memory leaks when not revealing
    val revealAlpha by if (isRevealing) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "revealAlpha"
        )
    } else {
        remember { mutableFloatStateOf(0.3f) }
    }

    val revealScale by if (isRevealing) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200),
                repeatMode = RepeatMode.Reverse
            ),
            label = "revealScale"
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }

    // Conditional shimmer rotation effect - only when revealing
    val revealRotation by if (isRevealing) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000),
                repeatMode = RepeatMode.Restart
            ),
            label = "revealRotation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    // Conditional glow intensity - only when revealing
    val glowIntensity by if (isRevealing) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowIntensity"
        )
    } else {
        remember { mutableFloatStateOf(0.4f) }
    }

    Box(
        modifier = Modifier.size(itemSize)
    ) {
        if (isRevealing) {
            // Multi-color animated glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = revealScale * 1.3f
                        scaleY = revealScale * 1.3f
                        alpha = glowIntensity * 0.5f
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E676).copy(alpha = glowIntensity),
                                Color(0xFF00C9FF).copy(alpha = glowIntensity * 0.7f),
                                Color(0xFFFFD700).copy(alpha = glowIntensity * 0.5f),
                                Color.Transparent
                            ),
                            radius = itemSize.value * 1.1f
                        ),
                        shape = CircleShape
                    )
            )
            // Enhanced particle burst effect
            EnhancedParticleBurst(visible = isRevealing, itemSize = itemSize)
        }

        // Main item container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val combinedScale =
                        scale * pressScale.value * (if (isRevealing) revealScale else 1f)
                    scaleX = combinedScale
                    scaleY = combinedScale
                    if (isRevealing) {
                        rotationZ = revealRotation * 0.02f
                    }
                }
                .clip(CircleShape)
                .then(
                    if (isRevealing) {
                        // Multi-layer border effect
                        Modifier
                            .border(
                                width = 6.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFF00E676).copy(alpha = revealAlpha),
                                        Color(0xFF69F0AE).copy(alpha = revealAlpha * 0.7f),
                                        Color(0xFF00E676).copy(alpha = revealAlpha),
                                        Color(0xFF69F0AE).copy(alpha = revealAlpha * 0.7f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = Color.White.copy(alpha = revealAlpha * 0.8f),
                                shape = CircleShape
                            )
                    } else {
                        Modifier
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {

                        if (hapticManager != null) {
                            hapticManager.buttonPress(haptic)
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onTapped()
                        coroutineScope.launch {
                            pressScale.animateTo(0.8f, animationSpec = tween(100))
                            pressScale.animateTo(
                                1f,
                                animationSpec = spring(stiffness = Spring.StiffnessLow)
                            )
                        }
                    }
                )
                .padding(if (isRevealing) 8.dp else 4.dp)
        ) {
            // Inner shimmer effect (only when revealing)
            if (isRevealing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = revealRotation * 0.5f
                        }
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.3f),
                                    Color(0xFF00E676).copy(alpha = 0.2f),
                                    Color(0xFF00C9FF).copy(alpha = 0.15f),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }

            // Main shape content
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawShape(item.shape, item.color.toComposeColor(), size.minDimension)
            }
        }
    }
}


@Composable
fun calculateGridAndItemSize(columns: Int): Pair<Dp, Dp> {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val gridPadding = 32.dp
    val availableWidth = screenWidth - gridPadding

    val itemPadding = 4.dp * 2
    val maxItemSize = (availableWidth / columns) - itemPadding

    // Use more screen space with improved base sizes and better space utilization
    val itemSize = when {
        columns <= 3 -> {
            // For very small grids, use almost all available space but cap at reasonable size
            minOf(maxItemSize, 140.dp)
        }
        columns <= 4 -> {
            // For 4x4 grids, use generous sizing
            minOf(maxItemSize, 110.dp)
        }
        columns <= 5 -> {
            // For 5x5 grids, use generous sizing for better visibility
            minOf(maxItemSize, 115.dp)
        }
        columns <= 6 -> {
            // Slightly smaller for 6x6
            minOf(maxItemSize, 75.dp)
        }
        columns <= 7 -> {
            // Reasonable size for 7x7
            minOf(maxItemSize, 65.dp)
        }
        else -> {
            // For very large grids, keep compact
            minOf(maxItemSize, 50.dp)
        }
    }

    val gridSize = (itemSize + itemPadding) * columns

    return Pair(gridSize, itemSize)
}

fun DrawScope.drawShape(shape: ShapeType, color: Color, size: Float) {
    // Subtle gradient that preserves color accuracy for gameplay
    fun gameplayGradient(base: Color): Brush = Brush.radialGradient(
        colors = listOf(
            base.copy(alpha = 1.0f),
            base.copy(alpha = 0.95f),
            base.copy(alpha = 0.9f)
        ),
        center = Offset(center.x - size * 0.08f, center.y - size * 0.1f),
        radius = size * 0.6f
    )

    // Minimal shadow for depth without color interference
    fun subtleShadow(base: Color) = Color.Black.copy(alpha = 0.15f)

    // Very light highlight that doesn't affect color perception
    fun lightHighlight(): Color = Color.White.copy(alpha = 0.12f)
    when (shape) {
        ShapeType.CIRCLE -> {
            // Minimal shadow for subtle depth
            drawCircle(
                color = subtleShadow(color),
                radius = size * 0.48f,
                center = Offset(center.x + size * 0.02f, center.y + size * 0.05f)
            )

            // Main shape with color-preserving gradient
            drawCircle(brush = gameplayGradient(color), radius = size * 0.47f)

            // Very subtle highlight that doesn't interfere with color
            drawCircle(
                color = lightHighlight(),
                radius = size * 0.15f,
                center = Offset(center.x - size * 0.1f, center.y - size * 0.1f)
            )

            // Clean, minimal border
            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                radius = size * 0.47f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = size * 0.02f)
            )
        }
        ShapeType.SQUARE -> {
            val corner = size * 0.12f
            val squareSize = size * 0.94f
            val offset = (size - squareSize) / 2f

            // Simple shadow for depth
            drawRoundRect(
                color = subtleShadow(color),
                size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
                topLeft = Offset(offset + squareSize * 0.02f, offset + squareSize * 0.05f)
            )

            // Main shape with color-preserving gradient
            drawRoundRect(
                brush = gameplayGradient(color),
                size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
                topLeft = Offset(offset, offset)
            )

            // Minimal highlight that preserves color accuracy
            drawRoundRect(
                color = lightHighlight(),
                topLeft = Offset(offset + squareSize * 0.12f, offset + squareSize * 0.1f),
                size = androidx.compose.ui.geometry.Size(squareSize * 0.6f, squareSize * 0.12f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner * 0.5f, corner * 0.5f)
            )

            // Clean border
            drawRoundRect(
                color = Color.White.copy(alpha = 0.2f),
                size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
                topLeft = Offset(offset, offset),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = size * 0.02f)
            )
        }
        ShapeType.TRIANGLE -> {
            val scale = 0.92f  // Increased from 0.85f to use more space
            val scaledSize = size * scale
            val offset = (size - scaledSize) / 2f

            // Create main triangle path with better space usage
            val mainPath = Path().apply {
                moveTo(offset + scaledSize / 2f, offset + scaledSize * 0.05f)  // Moved closer to top
                lineTo(offset + scaledSize * 0.95f, offset + scaledSize * 0.93f)  // Wider base
                lineTo(offset + scaledSize * 0.05f, offset + scaledSize * 0.93f)
                close()
            }

            // Simple shadow path matching the larger triangle
            val shadowPath = Path().apply {
                moveTo(offset + scaledSize / 2f + scaledSize * 0.02f, offset + scaledSize * 0.05f + scaledSize * 0.05f)
                lineTo(offset + scaledSize * 0.95f + scaledSize * 0.02f, offset + scaledSize * 0.93f + scaledSize * 0.05f)
                lineTo(offset + scaledSize * 0.05f + scaledSize * 0.02f, offset + scaledSize * 0.93f + scaledSize * 0.05f)
                close()
            }

            // Draw shadow
            drawPath(
                path = shadowPath,
                color = subtleShadow(color)
            )

            // Main triangle with color-preserving gradient
            drawPath(mainPath, brush = gameplayGradient(color))

            // Simple highlight that doesn't interfere with color, scaled for larger triangle
            val topHighlight = Path().apply {
                moveTo(offset + scaledSize / 2f, offset + scaledSize * 0.1f)
                lineTo(offset + scaledSize * 0.65f, offset + scaledSize * 0.35f)
                lineTo(offset + scaledSize * 0.35f, offset + scaledSize * 0.35f)
                close()
            }
            drawPath(
                path = topHighlight,
                color = lightHighlight()
            )

            // Clean border
            drawPath(
                path = mainPath,
                color = Color.White.copy(alpha = 0.2f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = size * 0.02f,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
    }
}


@Composable
fun EnhancedParticleBurst(visible: Boolean, itemSize: Dp) {
    if (!visible) return

    // Reduced particle count for better performance and memory usage
    val particleCount = 12 // Reduced from 20 to 12
    val maxRadius = itemSize.value * 0.8f
    val colors = listOf(
        Color(0xFF4CAF50), Color(0xFF81C784), Color(0xFFC8E6C9),
        Color(0xFF66BB6A), Color.White, Color(0xFFA5D6A7)
    )
    val animatables = remember { List(particleCount) { Animatable(0f) } }
    val scaleAnimatables = remember { List(particleCount) { Animatable(1f) } }

    // Cleanup animations when component disposes
    DisposableEffect(Unit) {
        onDispose {
            // No need to stop animations explicitly - they will be garbage collected
            // when the remember scope is disposed. This is just a marker for cleanup.
        }
    }

    LaunchedEffect(visible) {
        if (!visible) {
            // Reset animations when not visible
            animatables.forEach { it.snapTo(0f) }
            scaleAnimatables.forEach { it.snapTo(1f) }
            return@LaunchedEffect
        }

        animatables.forEachIndexed { i, anim ->
            launch {
                anim.animateTo(1f, animationSpec = tween(durationMillis = 1000, delayMillis = i * 15))
            }
        }
        scaleAnimatables.forEachIndexed { i, scaleAnim ->
            launch {
                delay(i * 15L)
                scaleAnim.animateTo(1.5f, animationSpec = tween(durationMillis = 300))
                scaleAnim.animateTo(0f, animationSpec = tween(durationMillis = 700))
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        for (i in 0 until particleCount) {
            val angle = (2 * Math.PI * i / particleCount).toFloat()
            val progress = animatables[i].value
            val scale = scaleAnimatables[i].value
            val radius = maxRadius * progress
            val particleOffset = Offset(
                center.x + radius * cos(angle),
                center.y + radius * sin(angle)
            )

            // Main particle
            drawCircle(
                color = colors[i % colors.size].copy(alpha = (1f - progress) * 0.8f),
                radius = size.minDimension * 0.08f * scale,
                center = particleOffset
            )

            // Trailing glow
            drawCircle(
                color = colors[i % colors.size].copy(alpha = (1f - progress) * 0.3f),
                radius = size.minDimension * 0.12f * scale,
                center = particleOffset
            )
        }
    }
}
