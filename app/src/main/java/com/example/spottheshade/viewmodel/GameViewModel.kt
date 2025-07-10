package com.example.spottheshade.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spottheshade.data.model.GameResult
import com.example.spottheshade.data.model.GameState
import com.example.spottheshade.data.model.UserPreferences
import com.example.spottheshade.data.repository.GridGenerator
import com.example.spottheshade.data.repository.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val gridGenerator = GridGenerator()
    private var timerJob: Job? = null
    
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    // Expose user preferences as StateFlow
    val userPreferences = preferencesManager.userPreferences
    
    fun startGame() {
        viewModelScope.launch {
            // Cancel any existing timer
            timerJob?.cancel()
            
            // Increment games played counter
            preferencesManager.incrementGamesPlayed()
            
            val grid = gridGenerator.generateGrid(level = 1)
            _gameState.value = GameState(
                grid = grid,
                isGameActive = true,
                score = 0,
                level = 1,
                gameResult = null,
                timeRemaining = 10,
                hasUsedExtraTime = false,
                lives = 3
            )
            
            // Start countdown timer
            startTimer()
        }
    }
    
    fun nextLevel() {
        val currentState = _gameState.value
        viewModelScope.launch {
            // Cancel any existing timer
            timerJob?.cancel()
            
            val grid = gridGenerator.generateGrid(level = currentState.level)
            _gameState.value = currentState.copy(
                grid = grid,
                isGameActive = true,
                gameResult = null,
                timeRemaining = 10,
                hasUsedExtraTime = false
            )
            
            // Start countdown timer
            startTimer()
        }
    }
    
    private fun startTimer(totalSeconds: Int = 10) {
        timerJob = viewModelScope.launch {
            for (timeLeft in totalSeconds downTo 1) {
                delay(1000)
                val currentState = _gameState.value
                if (!currentState.isGameActive) return@launch
                
                _gameState.value = currentState.copy(timeRemaining = timeLeft)
            }
            
            // Time's up!
            val currentState = _gameState.value
            if (currentState.isGameActive) {
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
         if (tappedItem.isTarget) {
             // Correct selection! Progress to next level
             val newLevel = currentState.level + 1
             val newScore = currentState.score + (10 * currentState.level)

             viewModelScope.launch {
                 // Update persistent data
                 preferencesManager.incrementCorrectAnswers()
                 preferencesManager.updateHighScore(newScore)
                 preferencesManager.updateHighestLevel(newLevel)
             }

             _gameState.value = currentState.copy(
                 gameResult = GameResult.Correct,
                 score = newScore,
                 level = newLevel,
                 isGameActive = false
             )
         } else {
             // Wrong selection!
             handleLifeLoss(GameResult.Wrong)
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

            _gameState.value = currentState.copy(
                isGameActive = true,
                gameResult = null,
                timeRemaining = 5,
                hasUsedExtraTime = true
            )

            startTimer(5)
        }
    }
    
    private fun handleLifeLoss(resultType: GameResult) {
        val currentState = _gameState.value
        val newLives = currentState.lives - 1
        
        if (newLives <= 0) {
            // Game Over - save final score and level
            viewModelScope.launch {
                preferencesManager.updateHighScore(currentState.score)
                preferencesManager.updateHighestLevel(currentState.level)
            }
            
            _gameState.value = currentState.copy(
                gameResult = GameResult.GameOver,
                isGameActive = false,
                lives = 0,
                timeRemaining = 0
            )
        } else {
            // Still have lives - show result but keep going
            _gameState.value = currentState.copy(
                gameResult = resultType,
                isGameActive = false,
                lives = newLives,
                timeRemaining = if (resultType == GameResult.Timeout) 0 else currentState.timeRemaining
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
                    timeRemaining = 10,
                    hasUsedExtraTime = false
                )
                
                startTimer()
            }
        }
    }

}

// ViewModelFactory to inject PreferencesManager
class GameViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(PreferencesManager(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 