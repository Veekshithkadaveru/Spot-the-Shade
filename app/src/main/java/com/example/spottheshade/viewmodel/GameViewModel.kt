package com.example.spottheshade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spottheshade.data.model.GameResult
import com.example.spottheshade.data.model.ShapeType
import com.example.spottheshade.data.model.ThemeType
import com.example.spottheshade.services.HapticManager
import com.example.spottheshade.data.repository.PreferencesManager
import com.example.spottheshade.services.SoundManager
import com.example.spottheshade.game.GameEventManager
import com.example.spottheshade.game.GameLogicManager
import com.example.spottheshade.game.GameStateManager
import com.example.spottheshade.game.GameUiEvent
import com.example.spottheshade.game.ThemeManager
import com.example.spottheshade.game.TimerManager
import com.example.spottheshade.navigation.NavigationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class GameViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager,
    private val gameLogicManager: GameLogicManager,
    private val themeManager: ThemeManager,
    private val gameEventManager: GameEventManager,
    val navigationHelper: NavigationHelper
) : ViewModel() {

    private val gameStateManager = GameStateManager()
    val gameState = gameStateManager.gameState
    val uiEvents = gameEventManager.uiEvents
    val userPreferences = preferencesManager.userPreferences

    private lateinit var timerManager: TimerManager

    init {
        initializeTimerManager()
        initializeSoundPreferences()
    }

    private fun initializeTimerManager() {
        timerManager = TimerManager(
            scope = viewModelScope,
            onTimeUpdate = { timeLeft ->
                viewModelScope.launch {
                    gameStateManager.updateGameState { state ->
                        if (state.isGameActive) state.copy(timeRemaining = timeLeft) else state
                    }
                }
            },
            onTimeWarning = {
                viewModelScope.launch { gameEventManager.emitEvent(GameUiEvent.TimeWarning) }
            },
            onTimeCritical = {
                viewModelScope.launch { gameEventManager.emitEvent(GameUiEvent.TimeCritical) }
            },
            onTimeUrgent = {
                viewModelScope.launch { gameEventManager.emitEvent(GameUiEvent.TimeUrgent) }
            },
            onTimeout = {
                viewModelScope.launch {
                    val shouldHandle = gameStateManager.withCurrentState { it.isGameActive }
                    if (shouldHandle) {
                        gameEventManager.emitEvent(GameUiEvent.Timeout)
                        handleLifeLoss(GameResult.Timeout)
                    }
                }
            }
        )
    }

    private fun initializeSoundPreferences() {
        viewModelScope.launch {
            try {
                val prefs = preferencesManager.userPreferences.first()
                soundManager.setSoundEnabled(prefs.soundEnabled)
            } catch (e: Exception) {
                android.util.Log.w("GameViewModel", "Failed to initialize sound preferences", e)
                soundManager.setSoundEnabled(true)
            }
        }
    }


    fun startGame() {
        viewModelScope.launch {
            timerManager.cancelTimer()
            preferencesManager.incrementGamesPlayed()

            val grid = gameLogicManager.generateGrid(level = 1)
            val currentShape = grid.firstOrNull()?.shape ?: ShapeType.CIRCLE

            gameStateManager.updateGameState {
                com.example.spottheshade.data.model.GameState(
                    grid = grid,
                    isGameActive = true,
                    score = 0,
                    level = 1,
                    gameResult = null,
                    hasUsedExtraTime = false,
                    lives = 3,
                    currentShape = currentShape
                )
            }

            timerManager.startTimer(gameLogicManager.getTimerDuration(1))
        }
    }

    fun nextLevel() {
        viewModelScope.launch {
            timerManager.cancelTimer()

            val (currentLevel, grid, currentShape) = gameStateManager.withCurrentState { currentState ->
                val grid = gameLogicManager.generateGrid(level = currentState.level)
                val shape = grid.firstOrNull()?.shape ?: ShapeType.CIRCLE
                Triple(currentState.level, grid, shape)
            }

            gameStateManager.updateGameState { currentState ->
                currentState.copy(
                    grid = grid,
                    isGameActive = true,
                    gameResult = null,
                    hasUsedExtraTime = false,
                    currentShape = currentShape
                )
            }

            timerManager.startTimer(gameLogicManager.getTimerDuration(currentLevel))
        }
    }


    fun onGridItemTapped(itemId: Int) {
        viewModelScope.launch {
            val (shouldProcess, tappedItem) = gameStateManager.withCurrentState { currentState ->
                if (!currentState.isGameActive) {
                    false to null
                } else {
                    val item = gameLogicManager.findTappedItem(currentState.grid, itemId)
                    true to item
                }
            }

            if (!shouldProcess) return@launch

            val wasActive = gameStateManager.withCurrentState { state ->
                if (state.isGameActive) {
                    timerManager.cancelTimer()
                    true
                } else {
                    false
                }
            }

            if (!wasActive) return@launch

            gameStateManager.updateGameState { it.copy(isGameActive = false) }

            if (tappedItem == null) {
                gameStateManager.updateGameState { it.copy(isGameActive = true) }
                timerManager.startTimer(gameLogicManager.getTimerDuration(gameState.value.level))
                return@launch
            }

            if (tappedItem.isTarget) {
                gameEventManager.stopTimeoutSound()
                gameEventManager.emitEvent(GameUiEvent.CorrectTap(itemId))

                val updatedState = gameStateManager.withCurrentState { currentState ->
                    gameLogicManager.processCorrectAnswer(currentState)
                }

                preferencesManager.incrementCorrectAnswers()
                preferencesManager.updateHighScore(updatedState.score)
                preferencesManager.updateHighestLevel(updatedState.level)

                gameStateManager.updateGameState { updatedState }
                delay(400)
                nextLevel()
            } else {
                gameEventManager.emitEvent(GameUiEvent.IncorrectTap(itemId))
                delay(500)
                handleLifeLoss(GameResult.Wrong)
            }
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            timerManager.cancelTimer()
            gameStateManager.resetState()
        }
    }

    fun useExtraTime() {
        viewModelScope.launch {
            val canUseExtraTime = gameStateManager.withCurrentState { currentState ->
                gameLogicManager.canUseExtraTime(currentState)
            }

            if (canUseExtraTime) {
                gameStateManager.updateGameState { currentState ->
                    gameLogicManager.useExtraTime(currentState)
                }
                timerManager.startTimer(5)
            }
        }
    }

    private suspend fun handleLifeLoss(resultType: GameResult) {
        timerManager.cancelTimer()

        val shouldEndGame = gameStateManager.withCurrentState { currentState ->
            gameLogicManager.shouldEndGame(currentState, resultType)
        }

        if (shouldEndGame) {
            endGame()
            return
        }

        gameStateManager.updateGameState { currentState ->
            gameLogicManager.processLifeLoss(currentState, resultType)
        }
    }

    fun declineExtraTime() {
        viewModelScope.launch {
            val shouldEndGame = gameStateManager.withCurrentState { it.lives <= 0 }

            if (shouldEndGame) {
                endGame()
            } else {
                gameStateManager.updateGameState { it.copy(gameResult = GameResult.OfferContinue) }
            }
        }
    }

    fun endGame() {
        viewModelScope.launch {
            timerManager.cancelTimer()

            val (currentScore, currentLevel, targetId, lastResult) = gameStateManager.withCurrentState { currentState ->
                val targetItem = gameLogicManager.findTargetItem(currentState.grid)
                Tuple4(
                    currentState.score,
                    currentState.level,
                    targetItem?.id,
                    currentState.gameResult
                )
            }

            preferencesManager.updateHighScore(currentScore)
            preferencesManager.updateHighestLevel(currentLevel)
            themeManager.checkThemeUnlockMilestones()

            targetId?.let {
                gameEventManager.emitEvent(GameUiEvent.RevealAnswer(it))
                delay(500)
                delay(2500)
                gameEventManager.emitEvent(GameUiEvent.GameOver)
                delay(200)
            } ?: run {
                gameEventManager.emitEvent(GameUiEvent.GameOver)
                delay(300)
            }

            gameStateManager.updateGameState { currentState ->
                gameLogicManager.createFinalGameState(currentState, lastResult, targetId)
            }
        }
    }

    private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    fun continueAfterLifeLoss() {
        viewModelScope.launch {
            val (canContinue, currentLevel) = gameStateManager.withCurrentState { currentState ->
                gameLogicManager.canContinue(currentState) to currentState.level
            }

            if (canContinue) {
                timerManager.cancelTimer()
                gameStateManager.updateGameState { currentState ->
                    gameLogicManager.continueAfterLifeLoss(currentState)
                }
                timerManager.startTimer(gameLogicManager.getTimerDuration(currentLevel))
            }
        }
    }


    fun toggleSound() {
        viewModelScope.launch {
            try {
                val currentPrefs = preferencesManager.userPreferences.first()
                val newSoundState = !currentPrefs.soundEnabled
                preferencesManager.setSoundEnabled(newSoundState)
                soundManager.setSoundEnabled(newSoundState)
            } catch (e: Exception) {
                android.util.Log.w("GameViewModel", "Failed to toggle sound preferences", e)
                soundManager.setSoundEnabled(!soundManager.isSoundEnabled)
            }
        }
    }

    fun setCurrentTheme(theme: ThemeType) {
        viewModelScope.launch {
            themeManager.setCurrentTheme(theme)
        }
    }

    fun unlockThemeWithRewardedAd(theme: ThemeType) {
        viewModelScope.launch {
            themeManager.unlockThemeWithRewardedAd(theme)
        }
    }

    fun getHapticManager(): HapticManager = hapticManager

    override fun onCleared() {
        super.onCleared()
        timerManager.cancelTimer()
        gameEventManager.stopTimeoutSound()
    }
}

 