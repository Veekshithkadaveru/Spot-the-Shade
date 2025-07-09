package com.example.spottheshade.data.repository

import androidx.compose.ui.graphics.Color
import com.example.spottheshade.data.model.GridItem
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GridGenerator {
    
    fun generateGrid(level: Int): List<GridItem> {
        val size = when {
            level <= 3 -> 2     // Levels 1-3: 2x2
            level <= 7 -> 3     // Levels 4-7: 3x3  
            level <= 20 -> 4    // Levels 8-20: 4x4
            level <= 40 -> 5    // Levels 21-40: 5x5
            level <= 65 -> 6    // Levels 41-65: 6x6
            level <= 90 -> 7    // Levels 66-90: 7x7
            else -> 8           // Levels 91+: 8x8 (max)
        }
        val total = size * size
        
        // Generate random HSL color for each round with better parameters
        val hue = Random.nextFloat() * 360
        val saturation = 0.6f + Random.nextFloat() * 0.3f
        val baseLight = 0.45f + Random.nextFloat() * 0.3f

        val diff = when {
            level <= 10 -> 0.08f // Easy: Levels 1-10 - very noticeable difference
            level <= 25 -> 0.05f - (level - 10) * 0.001f // Medium: 0.05 → 0.035
            level <= 40 -> 0.025f - (level - 25) * 0.0007f // Hard: 0.025 → 0.015
            level <= 55 -> 0.015f - (level - 40) * 0.0005f // Expert: 0.015 → 0.0075
            level <= 70 -> 0.0075f - (level - 55) * 0.0002f // Master: 0.0075 → 0.0045
            else -> max(0.003f, 0.0045f - (level - 70) * 0.0001f) // Legendary: 0.0045 → 0.003
        }
        
        val baseColor = Color.hsl(hue, saturation, baseLight)
        val targetColor = Color.hsl(hue, saturation, baseLight - diff)
        
        val targetPosition = Random.nextInt(total)
        
        return List(total) { index ->
            GridItem(
                id = index,
                color = if (index == targetPosition) targetColor else baseColor,
                isTarget = index == targetPosition
            )
        }
    }

    @Deprecated("Use generateGrid(level) instead")
    fun generate2x2Grid(): List<GridItem> {
        return generateGrid(level = 1)
    }
} 