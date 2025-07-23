package com.example.spottheshade.ui.screens.components

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.spottheshade.data.model.ShapeType
import com.example.spottheshade.data.repository.HapticManager
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
    item: com.example.spottheshade.data.model.GridItem,
    itemSize: Dp,
    scale: Float,
    onTapped: () -> Unit,
    isRevealing: Boolean = false,
    hapticManager: HapticManager? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val pressScale = remember { Animatable(1f) }
    val haptic = LocalHapticFeedback.current

    // Multiple animation layers for beautiful reveal effect
    val infiniteTransition = rememberInfiniteTransition(label = "reveal")

    // Pulsing border animation
    val revealAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "revealAlpha"
    )

    // Breathing scale animation
    val revealScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "revealScale"
    )

    // Shimmer rotation effect
    val revealRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Restart
        ),
        label = "revealRotation"
    )

    // Glow intensity
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowIntensity"
    )

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
                drawShape(item.shape, item.color, size.minDimension)
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

    val baseSize = when {
        columns <= 3 -> 100.dp
        columns <= 4 -> 90.dp
        columns <= 5 -> 80.dp
        columns <= 6 -> 70.dp
        columns <= 7 -> 60.dp
        else -> 50.dp
    }

    val itemSize = minOf(baseSize, maxItemSize)
    val gridSize = (itemSize + itemPadding) * columns

    return Pair(gridSize, itemSize)
}

fun DrawScope.drawShape(shape: ShapeType, color: Color, size: Float) {
    when (shape) {
        ShapeType.CIRCLE -> {
            drawCircle(color, radius = size / 2f)
        }

        ShapeType.SQUARE -> {
            drawRect(color, size = androidx.compose.ui.geometry.Size(size, size))
        }

        ShapeType.TRIANGLE -> {
            val scale = 0.85f
            val scaledSize = size * scale
            val offset = (size - scaledSize) / 2f

            val path = Path().apply {
                moveTo(offset + scaledSize / 2f, offset)
                lineTo(offset + scaledSize, offset + scaledSize)
                lineTo(offset, offset + scaledSize)
                close()
            }
            drawPath(path, color)
        }

        ShapeType.HEXAGON -> {
            val scale = 0.9f
            val scaledSize = size * scale
            val offset = (size - scaledSize) / 2f
            val radius = scaledSize / 2f
            val centerX = offset + radius
            val centerY = offset + radius

            val path = Path().apply {

                moveTo(centerX, centerY - radius)
                lineTo(centerX + radius * 0.866f, centerY - radius * 0.5f)
                lineTo(centerX + radius * 0.866f, centerY + radius * 0.5f)
                lineTo(centerX, centerY + radius)
                lineTo(centerX - radius * 0.866f, centerY + radius * 0.5f)
                lineTo(centerX - radius * 0.866f, centerY - radius * 0.5f)
                close()
            }
            drawPath(path, color)
        }

        ShapeType.DIAMOND -> {
            val scale = 0.85f
            val scaledSize = size * scale
            val offset = (size - scaledSize) / 2f
            val centerX = offset + scaledSize / 2f
            val centerY = offset + scaledSize / 2f
            val halfSize = scaledSize / 2f

            val path = Path().apply {
                moveTo(centerX, centerY - halfSize)  // Top point
                lineTo(centerX + halfSize, centerY)  // Right point
                lineTo(centerX, centerY + halfSize)  // Bottom point
                lineTo(centerX - halfSize, centerY)  // Left point
                close()
            }
            drawPath(path, color)
        }
    }
}


@Composable
fun EnhancedParticleBurst(visible: Boolean, itemSize: Dp) {
    if (!visible) return
    val particleCount = 20
    val maxRadius = itemSize.value * 0.8f
    val colors = listOf(
        Color(0xFF4CAF50), Color(0xFF81C784), Color(0xFFC8E6C9), 
        Color(0xFF66BB6A), Color.White, Color(0xFFA5D6A7)
    )
    val animatables = remember { List(particleCount) { Animatable(0f) } }
    val scaleAnimatables = remember { List(particleCount) { Animatable(1f) } }
    
    LaunchedEffect(visible) {
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