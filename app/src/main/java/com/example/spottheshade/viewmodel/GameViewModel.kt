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
import javax.inject.Inject

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

@HiltViewModel
class GameViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val gridGenerator = GridGenerator()
    private var timerJob: Job? = null

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<GameUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    val userPreferences = preferencesManager.userPreferences

    init {
        // Initialize sound state with user preferences
        viewModelScope.launch {
            val prefs = preferencesManager.userPreferences.first()
            soundManager.setSoundEnabled(prefs.soundEnabled)
        }
    }

    fun startGame() {
        viewModelScope.launch {

            timerJob?.cancel()

            preferencesManager.incrementGamesPlayed()

            val grid = gridGenerator.generateGrid(level = 1)
            val currentShape = if (grid.isNotEmpty()) grid.first().shape else ShapeType.CIRCLE
            _gameState.value = GameState(
                grid = grid,
                isGameActive = true,
                score = 0,
                level = 1,
                gameResult = null,
                hasUsedExtraTime = false,
                lives = 3,
                currentShape = currentShape
            )

            // Start countdown timer with dynamic duration
            startTimer(gridGenerator.getTimerDuration(1))
        }
    }

    fun nextLevel() {
        val currentState = _gameState.value
        viewModelScope.launch {
            timerJob?.cancel()

            val grid = gridGenerator.generateGrid(level = currentState.level)
            val currentShape = if (grid.isNotEmpty()) grid.first().shape else ShapeType.CIRCLE
            _gameState.value = currentState.copy(
                grid = grid,
                isGameActive = true,
                gameResult = null,
                hasUsedExtraTime = false,
                currentShape = currentShape
            )

            // Start countdown timer with dynamic duration
            startTimer(gridGenerator.getTimerDuration(currentState.level))
        }
    }

    private fun startTimer(totalSeconds: Int = gridGenerator.getTimerDuration(1)) {

        _gameState.value = _gameState.value.copy(timeRemaining = totalSeconds)

        timerJob = viewModelScope.launch {

            for (timeLeft in (totalSeconds - 1) downTo 0) {
                delay(1000)

                val currentState = _gameState.value
                if (!currentState.isGameActive) return@launch

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

                _gameState.value = currentState.copy(timeRemaining = timeLeft)
            }

            val currentState = _gameState.value
            if (currentState.isGameActive) {

                _uiEvents.emit(GameUiEvent.Timeout)
                handleLifeLoss(GameResult.Timeout)
            }
        }
    }

    fun onGridItemTapped(itemId: Int) {
        val currentState = _gameState.value
        if (!currentState.isGameActive) return

        // Immediately set game inactive to prevent multiple rapid clicks
        _gameState.value = currentState.copy(isGameActive = false)

        timerJob?.cancel()

        val tappedItem = currentState.grid.find { it.id == itemId }
        if (tappedItem == null) {
            // Restore game state if item not found
            _gameState.value = currentState.copy(isGameActive = true)
            return
        }

        viewModelScope.launch {
            if (tappedItem.isTarget) {

                _uiEvents.emit(GameUiEvent.CorrectTap(itemId))
                _uiEvents.emit(GameUiEvent.LevelUp)
                soundManager.playCorrectSound()

                val currentState = _gameState.value
                val newLevel = currentState.level + 1
                val newScore = currentState.score + (10 * currentState.level)

                preferencesManager.incrementCorrectAnswers()
                preferencesManager.updateHighScore(newScore)
                preferencesManager.updateHighestLevel(newLevel)

                _gameState.value = currentState.copy(
                    score = newScore,
                    level = newLevel,
                    isGameActive = false
                )

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
        timerJob?.cancel()
        _gameState.value = GameState()
    }

    fun playAgain() {
        startGame()
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
        val currentState = _gameState.value
        if (currentState.gameResult == GameResult.Timeout && !currentState.hasUsedExtraTime) {
            // TODO: Replace direct grant with rewarded ad flow

            val restoredLives = currentState.lives + 1

            _gameState.value = currentState.copy(
                isGameActive = true,
                gameResult = null,
                hasUsedExtraTime = true,
                lives = restoredLives
            )

            startTimer(5)
        }
    }

    private suspend fun handleLifeLoss(resultType: GameResult) {
        val currentState = _gameState.value
        val newLives = currentState.lives - 1

        // Stop timer during life loss handling
        timerJob?.cancel()

        if (newLives < 0) {
            endGame()
            return
        }

        if (newLives == 0 && resultType == GameResult.Wrong) {
            endGame()
        } else {
            _gameState.value = currentState.copy(
                gameResult = resultType,
                isGameActive = false,
                lives = newLives,
                timeRemaining = if (resultType == GameResult.Timeout) 0 else currentState.timeRemaining
            )
        }
    }

    fun declineExtraTime() {
        val currentState = _gameState.value
        if (currentState.lives > 0) {
            _gameState.value = currentState.copy(gameResult = GameResult.OfferContinue)
        } else {
            endGame()
        }
    }

    fun endGame() {
        val currentState = _gameState.value
        viewModelScope.launch {
            // Stop the timer immediately for answer reveal
            timerJob?.cancel()

            preferencesManager.updateHighScore(currentState.score)
            preferencesManager.updateHighestLevel(currentState.level)

            // Check if any themes should be unlocked based on achievements
            checkThemeUnlockMilestones()

            // Show professional answer reveal sequence
            val targetId = currentState.grid.find { it.isTarget }?.id
            targetId?.let {

                _uiEvents.emit(GameUiEvent.RevealAnswer(it))
                delay(500)

                // Play game over sound after reveal starts
                soundManager.playGameOverSound()

                // Hold the reveal for learning - timer is stopped
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

            _gameState.value = currentState.copy(
                gameResult = GameResult.GameOver,
                isGameActive = false,
                lives = 0,
                timeRemaining = 0,
                lastEndingReason = currentState.gameResult,
                revealTargetId = targetId
            )
        }
    }

    fun continueAfterLifeLoss() {
        val currentState = _gameState.value
        if (currentState.lives > 0 && currentState.gameResult != GameResult.GameOver) {

            viewModelScope.launch {
                timerJob?.cancel()

                _gameState.value = currentState.copy(
                    isGameActive = true,
                    gameResult = null,
                    hasUsedExtraTime = false
                )

                startTimer(gridGenerator.getTimerDuration(_gameState.value.level))
            }
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundManager.setSoundEnabled(enabled)
    }

    fun toggleSound() {
        viewModelScope.launch {
            val currentPrefs = preferencesManager.userPreferences.first()
            val newSoundState = !currentPrefs.soundEnabled

            preferencesManager.setSoundEnabled(newSoundState)
            soundManager.setSoundEnabled(newSoundState)
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
        }
    }

    fun getHapticManager(): HapticManager = hapticManager

    override fun onCleared() {
        super.onCleared()
    }
}

 