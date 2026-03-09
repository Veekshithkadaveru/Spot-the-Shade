package app.krafted.spottheshade.game

import app.krafted.spottheshade.data.model.GameResult
import app.krafted.spottheshade.data.model.GameState
import app.krafted.spottheshade.data.model.GridItem
import app.krafted.spottheshade.game.GridGenerator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameLogicManager @Inject constructor(
    private val gridGenerator: GridGenerator
) {

    fun calculateScore(currentScore: Int, level: Int): Int {
        val difficultyMultiplier = when {
            level < 10 -> 1
            level < 22 -> 2
            level < 36 -> 3
            level < 50 -> 4
            level < 65 -> 5
            else -> 6
        }
        return currentScore + (10 * difficultyMultiplier)
    }

    fun processCorrectAnswer(gameState: GameState): GameState {
        val newLevel = gameState.level + 1
        val newScore = calculateScore(gameState.score, gameState.level)

        return gameState.copy(
            score = newScore,
            level = newLevel,
            isGameActive = false
        )
    }

    fun processIncorrectAnswer(gameState: GameState): GameState {
        return gameState.copy(
            isGameActive = false
        )
    }

    fun processLifeLoss(gameState: GameState, resultType: GameResult): GameState {
        val newLives = gameState.lives - 1
        return gameState.copy(
            gameResult = resultType,
            isGameActive = false,
            lives = newLives,
            timeRemaining = if (resultType == GameResult.Timeout) 0 else gameState.timeRemaining
        )
    }

    fun shouldEndGame(gameState: GameState, resultType: GameResult): Boolean {
        val newLives = gameState.lives - 1
        return newLives < 0 || (newLives == 0 && resultType == GameResult.Wrong)
    }

    fun canUseExtraTime(gameState: GameState): Boolean {
        return gameState.gameResult == GameResult.Timeout && !gameState.hasUsedExtraTime
    }

    fun useExtraTime(gameState: GameState): GameState {
        val restoredLives = gameState.lives + 1
        return gameState.copy(
            isGameActive = true,
            gameResult = null,
            hasUsedExtraTime = true,
            lives = restoredLives
        )
    }

    fun canContinue(gameState: GameState): Boolean {
        return gameState.lives > 0 && when (gameState.gameResult) {
            GameResult.Wrong,
            GameResult.Timeout,
            GameResult.OfferContinue -> true
            else -> false
        }
    }

    fun continueAfterLifeLoss(gameState: GameState): GameState {
        return gameState.copy(
            isGameActive = true,
            gameResult = null,
            hasUsedExtraTime = false
        )
    }

    fun createFinalGameState(gameState: GameState, lastResult: GameResult?, targetId: Int?): GameState {
        return gameState.copy(
            gameResult = GameResult.GameOver,
            isGameActive = false,
            lives = 0,
            timeRemaining = 0,
            lastEndingReason = lastResult,
            revealTargetId = targetId
        )
    }

    fun generateGrid(level: Int) = gridGenerator.generateGrid(level)

    fun getTimerDuration(level: Int) = gridGenerator.getTimerDuration(level)

    fun findTappedItem(grid: List<GridItem>, itemId: Int): GridItem? {
        return grid.find { it.id == itemId }
    }

    fun findTargetItem(grid: List<GridItem>): GridItem? {
        return grid.find { it.isTarget }
    }
}
