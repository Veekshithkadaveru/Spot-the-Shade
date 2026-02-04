package app.krafted.spottheshade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.krafted.spottheshade.data.model.GameResult
import app.krafted.spottheshade.data.model.ShapeType
import app.krafted.spottheshade.data.model.ThemeType
import app.krafted.spottheshade.data.repository.UserPreferencesRepository
import app.krafted.spottheshade.services.SoundManager
import app.krafted.spottheshade.game.GameEventManager
import app.krafted.spottheshade.game.GameLogicManager
import app.krafted.spottheshade.game.GameStateManager
import app.krafted.spottheshade.game.GameUiEvent
import app.krafted.spottheshade.game.ThemeManager
import app.krafted.spottheshade.game.TimerEvent
import app.krafted.spottheshade.game.TimerManager
import app.krafted.spottheshade.data.repository.ErrorFeedbackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject


@HiltViewModel
class GameViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val soundManager: SoundManager,
    private val gameLogicManager: GameLogicManager,
    private val themeManager: ThemeManager,
    private val gameEventManager: GameEventManager,
    private val errorFeedbackManager: ErrorFeedbackManager
) : ViewModel() {

    private val gameStateManager = GameStateManager()

    // Tap debouncing to prevent rapid tap race conditions (thread-safe)
    private val lastTapTime = AtomicLong(0L)
    private val tapDebounceMs = 200L

    // Sealed class for atomic tap result handling
    private sealed class TapResult {
        object Inactive : TapResult()
        object ItemNotFound : TapResult()
        data class Correct(val item: app.krafted.spottheshade.data.model.GridItem, val state: app.krafted.spottheshade.data.model.GameState) : TapResult()
        data class Incorrect(val item: app.krafted.spottheshade.data.model.GridItem) : TapResult()
    }
    val gameState = gameStateManager.gameState
    val uiEvents = gameEventManager.uiEvents
    val userPreferences = userPreferencesRepository.userPreferences
    val errorEvents = errorFeedbackManager.errorEvents

    private lateinit var timerManager: TimerManager

    init {
        initializeTimerManager()
        initializeSoundPreferences()
    }

    private fun initializeTimerManager() {
        timerManager = TimerManager(
            scope = viewModelScope,
            onEvent = { event ->
                viewModelScope.launch {
                    when (event) {
                        is TimerEvent.Tick -> {
                            gameStateManager.updateGameState { state ->
                                if (state.isGameActive) {
                                    state.copy(timeRemaining = event.secondsLeft)
                                } else {
                                    state
                                }
                            }
                        }
                        TimerEvent.Warning -> {
                            soundManager.playTimeoutSound()
                            gameEventManager.emitEvent(GameUiEvent.TimeWarning)
                        }
                        TimerEvent.Critical -> {
                            gameEventManager.emitEvent(GameUiEvent.TimeCritical)
                        }
                        TimerEvent.Urgent -> {
                            gameEventManager.emitEvent(GameUiEvent.TimeUrgent)
                        }
                        TimerEvent.Timeout -> {
                            var shouldHandle = false
                            gameStateManager.updateGameState { state ->
                                if (!state.isGameActive) {
                                    return@updateGameState state
                                }
                                shouldHandle = true
                                state.copy(isGameActive = false)
                            }
                            if (shouldHandle) {
                                gameEventManager.emitEvent(GameUiEvent.Timeout)
                                handleLifeLoss(GameResult.Timeout)
                            }
                        }
                    }
                }
            }
        )
    }

    private fun initializeSoundPreferences() {
        viewModelScope.launch {
            try {
                val prefs = userPreferencesRepository.userPreferences.first()
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
            userPreferencesRepository.incrementGamesPlayed()

            val grid = gameLogicManager.generateGrid(level = 1)
            val currentShape = grid.firstOrNull()?.shape ?: ShapeType.CIRCLE

            gameStateManager.updateGameState {
                app.krafted.spottheshade.data.model.GameState(
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
            // Atomic check-and-set for debouncing to prevent race conditions
            val currentTime = System.currentTimeMillis()
            val previousTapTime = lastTapTime.getAndSet(currentTime)
            if (currentTime - previousTapTime < tapDebounceMs) {
                return@launch
            }

            // Single atomic operation to check state, find item, and update isGameActive
            var tapResult: TapResult = TapResult.Inactive
            gameStateManager.updateGameState { state ->
                if (!state.isGameActive) {
                    tapResult = TapResult.Inactive
                    return@updateGameState state
                }

                val item = gameLogicManager.findTappedItem(state.grid, itemId)
                    ?: run {
                        tapResult = TapResult.ItemNotFound
                        return@updateGameState state
                    }

                // Cancel timer inside the atomic block to prevent race
                timerManager.cancelTimer()

                return@updateGameState if (item.isTarget) {
                    tapResult = TapResult.Correct(item, state)
                    state.copy(isGameActive = false)
                } else {
                    tapResult = TapResult.Incorrect(item)
                    state.copy(isGameActive = false)
                }
            }

            // Process result outside the lock
            when (val result = tapResult) {
                is TapResult.Inactive -> {
                    // No action needed
                    return@launch
                }

                is TapResult.ItemNotFound -> {
                    // Item not found in grid - restart timer and continue
                    timerManager.startTimer(gameLogicManager.getTimerDuration(gameState.value.level))
                    return@launch
                }

                is TapResult.Correct -> {
                    soundManager.stopTimeoutSound()
                    soundManager.playCorrectSound()
                    gameEventManager.emitEvent(GameUiEvent.CorrectTap(itemId))

                    val updatedState = gameLogicManager.processCorrectAnswer(result.state)

                    userPreferencesRepository.incrementCorrectAnswers()
                    userPreferencesRepository.updateHighScore(updatedState.score)
                    userPreferencesRepository.updateHighestLevel(updatedState.level)

                    gameStateManager.updateGameState { updatedState }
                    delay(400)
                    nextLevel()
                }

                is TapResult.Incorrect -> {
                    soundManager.playWrongSound()
                    gameEventManager.emitEvent(GameUiEvent.IncorrectTap(itemId))
                    // ShakeGrid is emitted by GameEventManager after IncorrectTap
                    delay(500)
                    handleLifeLoss(GameResult.Wrong)
                }
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
            // Atomic check-and-update to prevent double-tap exploits
            var didUseExtraTime = false
            gameStateManager.updateGameState { currentState ->
                if (gameLogicManager.canUseExtraTime(currentState)) {
                    didUseExtraTime = true
                    gameLogicManager.useExtraTime(currentState)
                } else {
                    currentState
                }
            }

            if (didUseExtraTime) {
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

            userPreferencesRepository.updateHighScore(currentScore)
            userPreferencesRepository.updateHighestLevel(currentLevel)
            themeManager.checkThemeUnlockMilestones()

            targetId?.let {
                gameEventManager.emitEvent(GameUiEvent.RevealAnswer(it))
                delay(500)
                delay(2500)
                soundManager.playGameOverSound()
                gameEventManager.emitEvent(GameUiEvent.GameOver)
                delay(200)
            } ?: run {
                soundManager.playGameOverSound()
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
            // Atomic check-and-update to prevent double-tap exploits
            var didContinue = false
            var levelForTimer = 1

            gameStateManager.updateGameState { currentState ->
                if (gameLogicManager.canContinue(currentState)) {
                    didContinue = true
                    levelForTimer = currentState.level
                    gameLogicManager.continueAfterLifeLoss(currentState)
                } else {
                    currentState
                }
            }

            if (didContinue) {
                timerManager.cancelTimer()
                timerManager.startTimer(gameLogicManager.getTimerDuration(levelForTimer))
            }
        }
    }


    fun toggleSound() {
        viewModelScope.launch {
            try {
                val currentPrefs = userPreferencesRepository.userPreferences.first()
                val newSoundState = !currentPrefs.soundEnabled
                userPreferencesRepository.setSoundEnabled(newSoundState)
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

    override fun onCleared() {
        super.onCleared()
        timerManager.cancelTimer()
        soundManager.stopTimeoutSound()
    }
}
