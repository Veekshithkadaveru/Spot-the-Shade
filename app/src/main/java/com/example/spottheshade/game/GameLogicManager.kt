package com.example.spottheshade.game

import com.example.spottheshade.data.model.GameResult
import com.example.spottheshade.data.model.GameState
import com.example.spottheshade.data.model.GridItem
import com.example.spottheshade.game.GridGenerator

class GameLogicManager(
    private val gridGenerator: GridGenerator = GridGenerator()
) {
    
    fun calculateScore(currentScore: Int, level: Int): Int {
        return currentScore + (10 * level)
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
        return gameState.lives > 0 && gameState.gameResult != GameResult.GameOver
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