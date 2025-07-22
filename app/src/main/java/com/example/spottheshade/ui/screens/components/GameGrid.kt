package com.example.spottheshade.ui.screens.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.launch

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
    onTapped: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pressScale = remember { Animatable(1f) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .size(itemSize)
            .graphicsLayer {
                val combinedScale = scale * pressScale.value
                scaleX = combinedScale
                scaleY = combinedScale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // No ripple effect
                onClick = {
                    // Immediate haptic feedback on tap
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTapped()
                    coroutineScope.launch {
                        pressScale.animateTo(0.8f, animationSpec = tween(100))
                        pressScale.animateTo(1f, animationSpec = spring(stiffness = Spring.StiffnessLow))
                    }
                }
            )
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawShape(item.shape, item.color, size.minDimension)
        }
    }
}


@Composable
fun calculateGridAndItemSize(columns: Int): Pair<Dp, Dp> {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val gridPadding = 32.dp // Total padding for the grid on screen
    val availableWidth = screenWidth - gridPadding

    val itemPadding = 4.dp * 2
    val maxItemSize = (availableWidth / columns) - itemPadding

    // Dynamically adjust size based on level progression
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
                // Start at top point and draw hexagon clockwise
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