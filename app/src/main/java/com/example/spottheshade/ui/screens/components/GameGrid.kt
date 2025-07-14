package com.example.spottheshade.ui.screens.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.spottheshade.data.model.ShapeType

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
    Box(
        modifier = Modifier
            .size(itemSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .clickable(onClick = onTapped)
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

    val itemPadding = 4.dp * 2 // Padding inside each grid item
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
            val scale = 0.85f // Visually scale down triangle
            val scaledSize = size * scale
            val offset = (size - scaledSize) / 2f

            val path = Path().apply {
                moveTo(offset + scaledSize / 2f, offset) // Top center
                lineTo(offset + scaledSize, offset + scaledSize) // Bottom right
                lineTo(offset, offset + scaledSize) // Bottom left
                close()
            }
            drawPath(path, color)
        }
    }
} 