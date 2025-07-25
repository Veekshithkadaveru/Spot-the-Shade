package com.example.spottheshade.data.repository

import androidx.compose.ui.graphics.Color
import com.example.spottheshade.data.model.GridItem
import com.example.spottheshade.data.model.ShapeType
import kotlin.math.max
import kotlin.random.Random
import com.example.spottheshade.data.model.Difficulty

class GridGenerator {

    companion object {
        private const val MAX_GRID_SIZE = 8
        
        private val TIMER_DURATIONS = listOf(
            3 to 8,   // 2x2 grid: 8 seconds
            7 to 10,  // 3x3 grid: 10 seconds  
            26 to 12, // 4x4 grid: 12 seconds
            45 to 15, // 5x5 grid: 15 seconds
            70 to 15, // 6x6 grid: 15 seconds
            95 to 18  // 7x7 grid: 18 seconds
        )
        private const val MAX_TIMER_DURATION = 20 // 8x8 grid
        
        private val GRID_SIZE_LEVELS = listOf(
            3 to 2, 7 to 3, 26 to 4, 45 to 5, 70 to 6, 95 to 7
        )
        
        private val SHAPE_CYCLES = listOf(
            // Cycle 1: Basic introduction
            9 to ShapeType.CIRCLE,
            21 to ShapeType.SQUARE,
            35 to ShapeType.TRIANGLE,
            
            // Cycle 2: With harder grids/colors
            50 to ShapeType.CIRCLE,
            65 to ShapeType.SQUARE,
            80 to ShapeType.TRIANGLE,
            
            // Cycle 3: Master level
            95 to ShapeType.CIRCLE,
            110 to ShapeType.SQUARE,
            125 to ShapeType.TRIANGLE
        )
        
        private const val MIN_SATURATION = 0.6f
        private const val MAX_SATURATION = 0.9f
        private const val MIN_LIGHTNESS = 0.45f
        private const val MAX_LIGHTNESS = 0.75f

        private val COLOR_DIFFICULTY_LEVELS = listOf(
            10 to 0.08f,
            25 to 0.05f,
            30 to 0.035f,
            35 to 0.030f,
            40 to 0.025f,
            60 to 0.015f,
            75 to 0.0075f
        )
        private const val LEGENDARY_DIFFICULTY_START_LEVEL = 70
        private const val LEGENDARY_DIFFICULTY_BASE = 0.0045f
        private const val LEGENDARY_DIFFICULTY_FACTOR = 0.0001f
        private const val MIN_DIFFICULTY = 0.003f
    }

    fun getDifficulty(level: Int): Difficulty = when {
        level <= 10 -> Difficulty.EASY
        level <= 25 -> Difficulty.MEDIUM
        level <= 40 -> Difficulty.HARD
        level <= 60 -> Difficulty.EXPERT
        level <= 75 -> Difficulty.MASTER
        else -> Difficulty.LEGENDARY
    }
    
    fun getTimerDuration(level: Int): Int {
        return TIMER_DURATIONS.firstOrNull { level <= it.first }?.second ?: MAX_TIMER_DURATION
    }
    
    private fun getShapeForLevel(level: Int): ShapeType {
        // Find the current shape based on level
        val currentShape = SHAPE_CYCLES.firstOrNull { level <= it.first }?.second
        
        if (currentShape != null) {
            return currentShape
        }
        

        val cycleLength = 25
        val positionInCycle = (level - 126) % cycleLength
        
        return when (positionInCycle / 5) {
            0 -> ShapeType.CIRCLE
            1 -> ShapeType.SQUARE
            else -> ShapeType.TRIANGLE
        }
    }

    fun generateGrid(level: Int): List<GridItem> {
        val size = GRID_SIZE_LEVELS.firstOrNull { level <= it.first }?.second ?: MAX_GRID_SIZE
        val total = size * size

        val shape = getShapeForLevel(level)

        // Use original random color generation for game variety
        val hue = Random.nextFloat() * 360
        val saturation = MIN_SATURATION + Random.nextFloat() * (MAX_SATURATION - MIN_SATURATION)
        val baseLight = MIN_LIGHTNESS + Random.nextFloat() * (MAX_LIGHTNESS - MIN_LIGHTNESS)

        val diff = calculateColorDifference(level)

        val baseColor = Color.hsl(hue, saturation, baseLight)
        val targetColor = Color.hsl(hue, saturation, baseLight - diff)

        val targetPosition = Random.nextInt(total)

        return List(total) { index ->
            GridItem(
                id = index,
                color = if (index == targetPosition) targetColor else baseColor,
                isTarget = index == targetPosition,
                shape = shape
            )
        }
    }
    
    // Remove the theme-specific color generation - not needed anymore
    private fun calculateColorDifference(level: Int): Float {
        COLOR_DIFFICULTY_LEVELS.forEachIndexed { index, (maxLevel, baseDiff) ->
            if (level <= maxLevel) {
                val prevMaxLevel = if (index > 0) COLOR_DIFFICULTY_LEVELS[index - 1].first else 0
                val levelInRange = level - prevMaxLevel
                val prevBaseDiff = if (index > 0) COLOR_DIFFICULTY_LEVELS[index-1].second else baseDiff + (levelInRange * 0.001f)
                val factor = (baseDiff - prevBaseDiff) / (maxLevel - prevMaxLevel)
                return prevBaseDiff + levelInRange * factor
            }
        }

        return max(
            MIN_DIFFICULTY,
            LEGENDARY_DIFFICULTY_BASE - (level - LEGENDARY_DIFFICULTY_START_LEVEL) * LEGENDARY_DIFFICULTY_FACTOR
        )
    }
} 