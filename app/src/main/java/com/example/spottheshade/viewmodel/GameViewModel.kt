package com.example.spottheshade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spottheshade.data.model.GameResult
import com.example.spottheshade.data.model.GameState
import com.example.spottheshade.data.repository.GridGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    
    private val gridGenerator = GridGenerator()
    private var timerJob: Job? = null
    
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    fun startGame() {
        viewModelScope.launch {
            // Cancel any existing timer
            timerJob?.cancel()
            
            val grid = gridGenerator.generateGrid(level = 1)
            _gameState.value = GameState(
                grid = grid,
                isGameActive = true,
                score = 0,
                level = 1,
                gameResult = null,
                timeRemaining = 10,
                hasUsedExtraTime = false
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
                _gameState.value = currentState.copy(
                    gameResult = GameResult.Timeout,
                    isGameActive = false,
                    timeRemaining = 0
                )
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
             _gameState.value = currentState.copy(
                 gameResult = GameResult.Correct,
                 score = newScore,
                 level = newLevel,
                 isGameActive = false
             )
         } else {
             // Wrong selection!
             _gameState.value = currentState.copy(
                 gameResult = GameResult.Wrong,
                 isGameActive = false
             )
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
            // Give 5 extra seconds and resume the game
            _gameState.value = currentState.copy(
                isGameActive = true,
                gameResult = null,
                timeRemaining = 5,
                hasUsedExtraTime = true
            )

            startTimer(5)
        }
    }
    

} 