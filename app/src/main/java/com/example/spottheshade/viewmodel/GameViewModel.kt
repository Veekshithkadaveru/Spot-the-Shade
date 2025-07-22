package com.example.spottheshade.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
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
    
    // Expose user preferences as StateFlow
    val userPreferences = preferencesManager.userPreferences
    
    fun startGame() {
        viewModelScope.launch {
            // Cancel any existing timer
            timerJob?.cancel()
            
            // Increment games played counter
            preferencesManager.incrementGamesPlayed()
            
            // Generate grid with original random colors (not theme-specific)
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
            // Cancel any existing timer
            timerJob?.cancel()
            
            // Generate grid with original random colors (not theme-specific)
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
        // Set the initial time immediately
        _gameState.value = _gameState.value.copy(timeRemaining = totalSeconds)

        timerJob = viewModelScope.launch {
            // Loop from the second before down to 0
            for (timeLeft in (totalSeconds - 1) downTo 0) {
                delay(1000)
                
                val currentState = _gameState.value
                if (!currentState.isGameActive) return@launch

                // Play warning sound and emit escalating haptic events
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

            // Time's up!
            val currentState = _gameState.value
            if (currentState.isGameActive) {
                // Sound has already been played as a warning.
                _uiEvents.emit(GameUiEvent.Timeout)
                handleLifeLoss(GameResult.Timeout)
            }
        }
    }
    
    fun onGridItemTapped(itemId: Int) {
        val currentState = _gameState.value
        if (!currentState.isGameActive) return
        
        // Stop the timer since user made a selection
        timerJob?.cancel()

         val tappedItem = currentState.grid.find { it.id == itemId }
         if (tappedItem == null) return

        viewModelScope.launch {
         if (tappedItem.isTarget) {
             // Correct selection! Progress to next level
                _uiEvents.emit(GameUiEvent.CorrectTap(itemId))
                _uiEvents.emit(GameUiEvent.LevelUp)
                soundManager.playCorrectSound()
                
                val currentState = _gameState.value // get latest state
             val newLevel = currentState.level + 1
             val newScore = currentState.score + (10 * currentState.level)

            // Update persistent data
            preferencesManager.incrementCorrectAnswers()
            preferencesManager.updateHighScore(newScore)
            preferencesManager.updateHighestLevel(newLevel)

            // Update state for score and level, but don't pause
            _gameState.value = currentState.copy(
                score = newScore,
                level = newLevel
            )
             
            // Wait for animation, then proceed
            delay(400)
            nextLevel()

         } else {
             // Wrong selection!
                _uiEvents.emit(GameUiEvent.IncorrectTap(itemId))
                _uiEvents.emit(GameUiEvent.ShakeGrid)
             soundManager.playWrongSound()
                delay(500) // Allow animation to play
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
            // Current implementation grants extra time immediately for testing
            
            // Restore the life that was lost due to timeout
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

        if (newLives < 0) {
            // This should not happen, but as a safeguard:
            endGame()
            return
        }

        // If the last life was lost on a wrong answer, end the game immediately.
        // For timeouts, we show the 'extra time' dialog first.
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
                preferencesManager.updateHighScore(currentState.score)
                preferencesManager.updateHighestLevel(currentState.level)
                
                // Check if any themes should be unlocked based on achievements
                checkThemeUnlockMilestones()
            
            soundManager.playGameOverSound()
            _uiEvents.emit(GameUiEvent.GameOver)
            delay(300)
            
            _gameState.value = currentState.copy(
                gameResult = GameResult.GameOver,
                isGameActive = false,
                lives = 0,
                timeRemaining = 0,
                lastEndingReason = currentState.gameResult
            )
        }
    }
    
    // Add function to continue after losing a life
    fun continueAfterLifeLoss() {
        val currentState = _gameState.value
        if (currentState.lives > 0 && currentState.gameResult != GameResult.GameOver) {
            // Keep the same grid, just reset game state and timer
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
            soundManager.setSoundEnabled(newSoundState)
            preferencesManager.setSoundEnabled(newSoundState)
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

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}

 