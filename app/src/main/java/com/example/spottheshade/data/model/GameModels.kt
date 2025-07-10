package com.example.spottheshade.data.model

import androidx.compose.ui.graphics.Color

data class GridItem(
    val id: Int,
    val color: Color,
    val isTarget: Boolean
)

data class GameState(
    val grid: List<GridItem> = emptyList(),
    val isGameActive: Boolean = false,
    val score: Int = 0,
    val level: Int = 1,
    val gameResult: GameResult? = null,
    val timeRemaining: Int = 10,
    val hasUsedExtraTime: Boolean = false,
    val lives: Int = 3
)

sealed class GameResult {
    object Correct : GameResult()
    object Wrong : GameResult()
    object Timeout : GameResult()
    object GameOver : GameResult() // When all lives are lost
}

enum class GameStatus {
    NOT_STARTED,
    PLAYING,
    GAME_OVER
}

// Data classes for persistence
data class UserPreferences(
    val highScore: Int = 0,
    val highestLevel: Int = 1,
    val unlockedThemes: Set<ThemeType> = setOf(ThemeType.DEFAULT),
    val currentTheme: ThemeType = ThemeType.DEFAULT,
    val soundEnabled: Boolean = true,
    val totalGamesPlayed: Int = 0,
    val totalCorrectAnswers: Int = 0
)

enum class ThemeType(val displayName: String) {
    DEFAULT("Default"),
    NEON("Neon"),
    PASTEL("Pastel"),
    RETRO("Retro"),
    MONOCHROME("Monochrome")
}

enum class ShapeType {
    CIRCLE,
    SQUARE,
    TRIANGLE
} 