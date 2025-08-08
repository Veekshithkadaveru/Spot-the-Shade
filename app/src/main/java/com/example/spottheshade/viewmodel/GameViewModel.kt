package com.example.spottheshade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spottheshade.data.model.GameResult
import com.example.spottheshade.data.model.GameState
import com.example.spottheshade.data.model.ShapeType
import com.example.spottheshade.data.model.ThemeType
import com.example.spottheshade.data.model.UserPreferences
import com.example.spottheshade.data.repository.GridGenerator
import com.example.spottheshade.data.repository.HapticManager
import com.example.spottheshade.data.repository.PreferencesManager
import com.example.spottheshade.data.repository.SoundManager
import com.example.spottheshade.navigation.NavigationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

// TODO: REWARDED AD INTEGRATION - OVERALL STRATEGY
// MONETIZATION OPPORTUNITIES IN THIS GAME:
// 1. THEME UNLOCKS: Show rewarded ads for premium themes (unlockThemeWithRewardedAd)
// 2. EXTRA TIME: Show ads to get 5 extra seconds when time runs out (useExtraTime)
// 3. CONTINUE GAME: Show ads to continue after game over with extra lives (future feature)
// 4. DAILY REWARDS: Show ads for bonus coins/themes (future feature)
// 5. REMOVE ADS: Offer premium purchase to remove all ads (future feature)
//
// AD IMPLEMENTATION CHECKLIST:
// □ Add Google AdMob dependency to build.gradle
// □ Configure AdMob App ID in AndroidManifest.xml
// □ Create AdManager singleton class
// □ Initialize ads in Application class
// □ Add rewarded ad loading and showing logic
// □ Implement proper ad callbacks and error handling
// □ Add loading states for better UX
// □ Test ad integration thoroughly
// □ Add analytics for ad performance tracking

sealed class GameUiEvent {
    data class CorrectTap(val itemId: Int) : GameUiEvent()
    data class IncorrectTap(val itemId: Int) : GameUiEvent()
    object ShakeGrid : GameUiEvent()
    object Timeout : GameUiEvent()
    object LevelUp : GameUiEvent()
    object GameOver : GameUiEvent()
    object TimeWarning : GameUiEvent()      // 5 seconds left
    object TimeCritical : GameUiEvent()     // 3 seconds left  
    object TimeUrgent : GameUiEvent()       // 1 second left
    data class RevealAnswer(val targetId: Int) :
        GameUiEvent()
}

// Helper data class for atomic state extraction
private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager,
    val navigationHelper: NavigationHelper
) : ViewModel() {

    private val gridGenerator = GridGenerator()
    private var timerJob: Job? = null

    private val gameStateMutex = Mutex()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<GameUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    val userPreferences = preferencesManager.userPreferences

    init {
            // Initialize sound state with user preferences
        viewModelScope.launch {
            try {
                val prefs = preferencesManager.userPreferences.first()
                soundManager.setSoundEnabled(prefs.soundEnabled)
            } catch (e: Exception) {
                android.util.Log.w("GameViewModel", "Failed to initialize sound preferences", e)
                // Use default sound enabled state
                soundManager.setSoundEnabled(true)
            }
        }
    }

    /**
     * Thread-safe state update function that prevents race conditions
     */
    private suspend fun updateGameState(transform: (GameState) -> GameState) {
        gameStateMutex.withLock {
            _gameState.value = transform(_gameState.value)
        }
    }

    /**
     * Get current state safely within a mutex lock
     */
    private suspend fun <T> withCurrentState(action: (GameState) -> T): T {
        return gameStateMutex.withLock {
            action(_gameState.value)
        }
    }

    fun startGame() {
        viewModelScope.launch {
            timerJob?.cancel()

            preferencesManager.incrementGamesPlayed()

            val grid = gridGenerator.generateGrid(level = 1)
            val currentShape = grid.firstOrNull()?.shape ?: ShapeType.CIRCLE

            updateGameState {
                GameState(
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

            // Start countdown timer with dynamic duration
            startTimer(gridGenerator.getTimerDuration(1))
        }
    }

    fun nextLevel() {
        viewModelScope.launch {
            timerJob?.cancel()

            val (currentLevel, grid, currentShape) = withCurrentState { currentState ->
                val grid = gridGenerator.generateGrid(level = currentState.level)
                val shape = grid.firstOrNull()?.shape ?: ShapeType.CIRCLE
                Triple(currentState.level, grid, shape)
            }

            updateGameState { currentState ->
                currentState.copy(
                    grid = grid,
                    isGameActive = true,
                    gameResult = null,
                    hasUsedExtraTime = false,
                    currentShape = currentShape
                )
            }

            // Start countdown timer with dynamic duration
            startTimer(gridGenerator.getTimerDuration(currentLevel))
        }
    }

    private fun startTimer(totalSeconds: Int = gridGenerator.getTimerDuration(1)) {
        viewModelScope.launch {
            // Cancel any existing timer job to prevent multiple timers
            timerJob?.cancel()
            
            // Atomically set initial timer state
            updateGameState { it.copy(timeRemaining = totalSeconds) }

            timerJob = viewModelScope.launch {
                try {
                    for (timeLeft in (totalSeconds - 1) downTo 0) {
                        delay(1000)

                        // Atomic check-and-update pattern
                        val shouldContinue = withCurrentState { state ->
                            if (state.isGameActive) {
                                true
                            } else {
                                false
                            }
                        }
                        
                        if (!shouldContinue) return@launch

                        when (timeLeft) {
                            5 -> {
                                soundManager.playTimeoutSound()
                                _uiEvents.emit(GameUiEvent.TimeWarning)
                            }

                            3 -> {
                                _uiEvents.emit(GameUiEvent.TimeCritical)
                            }

                            1 -> {
                                _uiEvents.emit(GameUiEvent.TimeUrgent)
                            }
                        }

                        // Atomically update timer state only if still active
                        updateGameState { currentState ->
                            if (currentState.isGameActive) {
                                currentState.copy(timeRemaining = timeLeft)
                            } else {
                                currentState
                            }
                        }
                    }

                    // Handle timeout with double-check pattern
                    val shouldHandleTimeout = withCurrentState { state ->
                        if (state.isGameActive) {
                            true
                        } else {
                            false
                        }
                    }

                    if (shouldHandleTimeout) {
                        _uiEvents.emit(GameUiEvent.Timeout)
                        handleLifeLoss(GameResult.Timeout)
                    }
                } catch (e: CancellationException) {
                    // Expected when timer is cancelled - don't handle as error
                    throw e
                } catch (e: Exception) {
                    // Log unexpected errors but don't crash the game
                    android.util.Log.e("GameViewModel", "Timer error", e)
                }
            }
        }
    }

    fun onGridItemTapped(itemId: Int) {
        viewModelScope.launch {
            // Double-check pattern: atomically verify and mark state as processing
            val (shouldProcess, tappedItem) = withCurrentState { currentState ->
                if (!currentState.isGameActive) {
                    false to null
                } else {
                    val item = currentState.grid.find { it.id == itemId }
                    true to item
                }
            }

            if (!shouldProcess) return@launch

            // Atomically transition to processing state to prevent concurrent taps
            val wasActive = withCurrentState { state ->
                if (state.isGameActive) {
                    timerJob?.cancel()
                    true
                } else {
                    false
                }
            }

            if (!wasActive) return@launch

            updateGameState { it.copy(isGameActive = false) }

            if (tappedItem == null) {
                // Restore game state if item not found - rare edge case
                updateGameState { it.copy(isGameActive = true) }
                startTimer(gridGenerator.getTimerDuration(gameState.value.level))
                return@launch
            }

            if (tappedItem.isTarget) {
                soundManager.stopTimeoutSound()

                _uiEvents.emit(GameUiEvent.CorrectTap(itemId))
                _uiEvents.emit(GameUiEvent.LevelUp)
                soundManager.playCorrectSound()

                // Calculate new values atomically
                val (newLevel, newScore) = withCurrentState { currentState ->
                    val level = currentState.level + 1
                    val score = currentState.score + (10 * currentState.level)
                    level to score
                }

                // Update preferences
                preferencesManager.incrementCorrectAnswers()
                preferencesManager.updateHighScore(newScore)
                preferencesManager.updateHighestLevel(newLevel)

                // Atomically update game state with new values
                updateGameState { currentState ->
                    currentState.copy(
                        score = newScore,
                        level = newLevel,
                        isGameActive = false
                    )
                }

                delay(400)
                nextLevel()

            } else {
                // Wrong selection!
                _uiEvents.emit(GameUiEvent.IncorrectTap(itemId))
                _uiEvents.emit(GameUiEvent.ShakeGrid)
                soundManager.playWrongSound()
                delay(500)
                handleLifeLoss(GameResult.Wrong)
            }
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            timerJob?.cancel()
            updateGameState { GameState() }
        }
    }

    // TODO: REWARDED AD INTEGRATION - Extra Time
    // This function should show a rewarded ad before granting extra time
    // IMPLEMENTATION NEEDED:
    // 1. Show rewarded ad when user clicks "Get Extra Time" button
    // 2. Only grant extra time if user successfully watches full ad
    // 3. Add proper error handling for ad load failures
    // 4. Track ad completion analytics
    // 5. Limit extra time usage to once per game session (already implemented)
    fun useExtraTime() {
        viewModelScope.launch {
            val canUseExtraTime = withCurrentState { currentState ->
                currentState.gameResult == GameResult.Timeout && !currentState.hasUsedExtraTime
            }

            if (canUseExtraTime) {
                // TODO: Replace direct grant with rewarded ad flow
                updateGameState { currentState ->
                    val restoredLives = currentState.lives + 1
                    currentState.copy(
                        isGameActive = true,
                        gameResult = null,
                        hasUsedExtraTime = true,
                        lives = restoredLives
                    )
                }

                startTimer(5)
            }
        }
    }

    private suspend fun handleLifeLoss(resultType: GameResult) {
        // Stop timer during life loss handling
        timerJob?.cancel()

        val shouldEndGame = withCurrentState { currentState ->
            val newLives = currentState.lives - 1
            newLives < 0 || (newLives == 0 && resultType == GameResult.Wrong)
        }

        if (shouldEndGame) {
            endGame()
            return
        }

        updateGameState { currentState ->
            val newLives = currentState.lives - 1
            currentState.copy(
                gameResult = resultType,
                isGameActive = false,
                lives = newLives,
                timeRemaining = if (resultType == GameResult.Timeout) 0 else currentState.timeRemaining
            )
        }
    }

    fun declineExtraTime() {
        viewModelScope.launch {
            val shouldEndGame = withCurrentState { it.lives <= 0 }

            if (shouldEndGame) {
                endGame()
            } else {
                updateGameState { it.copy(gameResult = GameResult.OfferContinue) }
            }
        }
    }

    fun endGame() {
        viewModelScope.launch {
            // Stop the timer immediately for answer reveal
            timerJob?.cancel()

            val (currentScore, currentLevel, targetId, lastResult) = withCurrentState { currentState ->
                Tuple4(
                    currentState.score,
                    currentState.level,
                    currentState.grid.find { it.isTarget }?.id,
                    currentState.gameResult
                )
            }

            preferencesManager.updateHighScore(currentScore)
            preferencesManager.updateHighestLevel(currentLevel)

            checkThemeUnlockMilestones()

            targetId?.let {
                _uiEvents.emit(GameUiEvent.RevealAnswer(it))
                delay(500)

                soundManager.playGameOverSound()

                delay(2500)

                // Emit game over after reveal completes
                _uiEvents.emit(GameUiEvent.GameOver)
                delay(200)
            } ?: run {
                // Fallback if no target found
                soundManager.playGameOverSound()
                _uiEvents.emit(GameUiEvent.GameOver)
                delay(300)
            }

            // Atomically update final game state
            updateGameState { currentState ->
                currentState.copy(
                    gameResult = GameResult.GameOver,
                    isGameActive = false,
                    lives = 0,
                    timeRemaining = 0,
                    lastEndingReason = lastResult,
                    revealTargetId = targetId
                )
            }
        }
    }

    fun continueAfterLifeLoss() {
        viewModelScope.launch {
            val (canContinue, currentLevel) = withCurrentState { currentState ->
                val canContinue =
                    currentState.lives > 0 && currentState.gameResult != GameResult.GameOver
                canContinue to currentState.level
            }

            if (canContinue) {
                timerJob?.cancel()

                updateGameState { currentState ->
                    currentState.copy(
                        isGameActive = true,
                        gameResult = null,
                        hasUsedExtraTime = false
                    )
                }

                startTimer(gridGenerator.getTimerDuration(currentLevel))
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
                // Fallback: just toggle the sound manager state
                soundManager.setSoundEnabled(!soundManager.isSoundEnabled)
            }
        }
    }

    // Theme management functions
    fun unlockTheme(theme: ThemeType) {
        viewModelScope.launch {
            preferencesManager.unlockTheme(theme)
        }
    }

    fun setCurrentTheme(theme: ThemeType) {
        viewModelScope.launch {
            preferencesManager.setCurrentTheme(theme)
        }
    }

    fun isThemeUnlocked(theme: ThemeType, userPreferences: UserPreferences): Boolean {
        return userPreferences.unlockedThemes.contains(theme)
    }

    // TODO: REWARDED AD INTEGRATION - Theme Unlock
    // This function should display a rewarded ad before unlocking themes
    // IMPLEMENTATION NEEDED:
    // 1. Initialize Google AdMob rewarded ads in MainActivity/Application
    // 2. Load rewarded ad when user tries to unlock a theme
    // 3. Show ad with proper callbacks:
    //    - onAdShowedFullScreenContent: Track ad impression
    //    - onAdDismissedFullScreenContent: Handle ad close
    //    - onUserEarnedReward: Call unlockTheme(theme) only on successful completion
    //    - onAdFailedToShowFullScreenContent: Show error message to user
    // 4. Add loading states while ad is loading
    // 5. Handle ad load failures gracefully (offer alternative unlock methods)
    fun unlockThemeWithRewardedAd(theme: ThemeType) {
        // TODO: Replace this direct unlock with actual rewarded ad flow
        // Current implementation is temporary for testing
        unlockTheme(theme)
    }

    // Check if user has reached milestones to unlock themes organically
    fun checkThemeUnlockMilestones() {
        viewModelScope.launch {
            try {
                val prefs = preferencesManager.userPreferences.first()

                // Auto-unlock themes based on achievements
            when {
                prefs.highestLevel >= 10 && !prefs.unlockedThemes.contains(ThemeType.FOREST) -> {
                    preferencesManager.unlockTheme(ThemeType.FOREST)
                }

                prefs.highestLevel >= 20 && !prefs.unlockedThemes.contains(ThemeType.OCEAN) -> {
                    preferencesManager.unlockTheme(ThemeType.OCEAN)
                }

                prefs.highestLevel >= 30 && !prefs.unlockedThemes.contains(ThemeType.SUNSET) -> {
                    preferencesManager.unlockTheme(ThemeType.SUNSET)
                }

                prefs.highestLevel >= 40 && !prefs.unlockedThemes.contains(ThemeType.WINTER) -> {
                    preferencesManager.unlockTheme(ThemeType.WINTER)
                }

                prefs.highestLevel >= 50 && !prefs.unlockedThemes.contains(ThemeType.SPRING) -> {
                    preferencesManager.unlockTheme(ThemeType.SPRING)
                }

                prefs.highScore >= 1000 && !prefs.unlockedThemes.contains(ThemeType.NEON_CYBER) -> {
                    preferencesManager.unlockTheme(ThemeType.NEON_CYBER)
                }

                prefs.highScore >= 2000 && !prefs.unlockedThemes.contains(ThemeType.VOLCANIC) -> {
                    preferencesManager.unlockTheme(ThemeType.VOLCANIC)
                }
            }
            } catch (e: Exception) {
                android.util.Log.w("GameViewModel", "Failed to check theme unlock milestones", e)
                // Theme unlocks are not critical - game can continue without them
            }
        }
    }

    fun getHapticManager(): HapticManager = hapticManager

    override fun onCleared() {
        super.onCleared()

        timerJob?.cancel()

        // Stop any ongoing sounds to prevent audio glitches
        soundManager.stopTimeoutSound()

    }
}

 