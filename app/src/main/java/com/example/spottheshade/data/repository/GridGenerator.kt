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
        
        private val GRID_SIZE_LEVELS = listOf(
            3 to 2, 7 to 3, 20 to 4, 40 to 5, 65 to 6, 90 to 7
        )
        
        private val SHAPE_LEVELS = listOf(
            9 to ShapeType.CIRCLE, 19 to ShapeType.SQUARE
        )
        
        private const val MIN_SATURATION = 0.6f
        private const val MAX_SATURATION = 0.9f
        private const val MIN_LIGHTNESS = 0.45f
        private const val MAX_LIGHTNESS = 0.75f

        private val COLOR_DIFFICULTY_LEVELS = listOf(
            10 to 0.08f,
            25 to 0.05f,
            40 to 0.025f,
            55 to 0.015f,
            70 to 0.0075f
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
        level <= 55 -> Difficulty.EXPERT
        level <= 70 -> Difficulty.MASTER
        else -> Difficulty.LEGENDARY
    }

    fun generateGrid(level: Int): List<GridItem> {
        val size = GRID_SIZE_LEVELS.firstOrNull { level <= it.first }?.second ?: MAX_GRID_SIZE
        val total = size * size

        val shape = SHAPE_LEVELS.firstOrNull { level <= it.first }?.second ?: ShapeType.TRIANGLE

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