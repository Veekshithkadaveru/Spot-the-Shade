package app.krafted.spottheshade.data.model

import androidx.compose.ui.graphics.Color

data class GridItem(
    val id: Int,
    val color: Color,
    val isTarget: Boolean,
    val shape: ShapeType
)

data class GameState(
    val grid: List<GridItem> = emptyList(),
    val score: Int = 0,
    val level: Int = 1,
    val lives: Int = 3,
    val timeRemaining: Int = 8,
    val isGameActive: Boolean = false,
    val gameResult: GameResult? = null,
    val hasUsedExtraTime: Boolean = false,
    val currentShape: ShapeType = ShapeType.CIRCLE,
    val lastEndingReason: GameResult? = null,
    val revealTargetId: Int? = null
)

enum class ShapeType {
    CIRCLE, SQUARE, TRIANGLE
}

enum class GameResult {
    Wrong, Timeout, GameOver, OfferContinue
}

data class UserPreferences(
    val highScore: Int = 0,
    val highestLevel: Int = 1,
    val unlockedThemes: Set<ThemeType> = setOf(ThemeType.DEFAULT),
    val currentTheme: ThemeType = ThemeType.DEFAULT,
    val soundEnabled: Boolean = true,
    val totalGamesPlayed: Int = 0,
    val totalCorrectAnswers: Int = 0
)

enum class ThemeType {
    DEFAULT,
    FOREST,
    OCEAN,
    SUNSET,
    WINTER,
    SPRING,
    NEON_CYBER,
    VOLCANIC
}

enum class Difficulty(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    EXPERT("Expert"),
    MASTER("Master"),
    LEGENDARY("Legendary")
}
