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
    val hasUsedExtraTime: Boolean = false
)

sealed class GameResult {
    object Correct : GameResult()
    object Wrong : GameResult()
    object Timeout : GameResult()
}

enum class GameStatus {
    NOT_STARTED,
    PLAYING,
    GAME_OVER
} 