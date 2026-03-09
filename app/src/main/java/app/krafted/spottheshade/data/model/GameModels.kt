package app.krafted.spottheshade.data.model

data class GridItem(
    val id: Int,
    val color: HSLColor,
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
    VOLCANIC,
    ROYAL_GOLD
}

/**
 * Single source of truth for theme unlock requirements.
 * @return Pair of (requirementType: "level" or "score", targetValue: Int)
 */
fun ThemeType.unlockRequirement(): Pair<String, Int> = when (this) {
    ThemeType.DEFAULT -> Pair("level", 0)
    ThemeType.FOREST -> Pair("level", 10)
    ThemeType.OCEAN -> Pair("level", 20)
    ThemeType.SUNSET -> Pair("level", 30)
    ThemeType.WINTER -> Pair("level", 40)
    ThemeType.SPRING -> Pair("level", 50)
    ThemeType.NEON_CYBER -> Pair("score", 1000)
    ThemeType.VOLCANIC -> Pair("score", 2000)
    ThemeType.ROYAL_GOLD -> Pair("score", 5000)
}

fun ThemeType.isUnlockConditionMet(prefs: UserPreferences): Boolean {
    val (type, target) = unlockRequirement()
    return when (type) {
        "level" -> prefs.highestLevel >= target
        "score" -> prefs.highScore >= target
        else -> false
    }
}

enum class Difficulty(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    EXPERT("Expert"),
    MASTER("Master"),
    LEGENDARY("Legendary")
}
