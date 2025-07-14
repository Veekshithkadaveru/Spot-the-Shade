package com.example.spottheshade.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spottheshade.data.model.GameResult
import com.example.spottheshade.data.model.GameState
import com.example.spottheshade.data.model.ShapeType
import com.example.spottheshade.data.model.UserPreferences
import com.example.spottheshade.data.repository.GridGenerator
import com.example.spottheshade.data.repository.PreferencesManager
import com.example.spottheshade.data.repository.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class GameUiEvent {
    data class CorrectTap(val itemId: Int) : GameUiEvent()
    data class IncorrectTap(val itemId: Int) : GameUiEvent()
    object ShakeGrid : GameUiEvent()
}

class GameViewModel(
    private val preferencesManager: PreferencesManager,
    private val soundManager: SoundManager
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
            
            // Start countdown timer
            startTimer(10)
        }
    }
    
    fun nextLevel() {
        val currentState = _gameState.value
        viewModelScope.launch {
            // Cancel any existing timer
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
            
            // Start countdown timer
            startTimer(10)
        }
    }
    
    private fun startTimer(totalSeconds: Int = 10) {
        // Set the initial time immediately
        _gameState.value = _gameState.value.copy(timeRemaining = totalSeconds)

        timerJob = viewModelScope.launch {
            // Loop from the second before down to 0
            for (timeLeft in (totalSeconds - 1) downTo 0) {
                delay(1000)
                
                val currentState = _gameState.value
                if (!currentState.isGameActive) return@launch

                // Play a warning sound when 5 seconds are left on the clock
                if (timeLeft == 5) {
                    soundManager.playTimeoutSound()
                }

                _gameState.value = currentState.copy(timeRemaining = timeLeft)
            }

            // Time's up!
            val currentState = _gameState.value
            if (currentState.isGameActive) {
                // Sound has already been played as a warning.
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
    
    fun useExtraTime() {
        val currentState = _gameState.value
        if (currentState.gameResult == GameResult.Timeout && !currentState.hasUsedExtraTime) {
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
            
            soundManager.playGameOverSound()
            delay(300)
            
            _gameState.value = currentState.copy(
                gameResult = GameResult.GameOver,
                isGameActive = false,
                lives = 0,
                timeRemaining = 0
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
                
                startTimer(10)
            }
        }
    }
    
    fun setSoundEnabled(enabled: Boolean) {
        soundManager.setSoundEnabled(enabled)
    }
    
    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }

}

// ViewModelFactory to inject PreferencesManager
class GameViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(
                PreferencesManager(context),
                SoundManager(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 