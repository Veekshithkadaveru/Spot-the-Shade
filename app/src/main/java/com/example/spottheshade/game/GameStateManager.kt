package com.example.spottheshade.game

import com.example.spottheshade.data.model.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GameStateManager {
    private val gameStateMutex = Mutex()
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    suspend fun updateGameState(transform: (GameState) -> GameState) {
        gameStateMutex.withLock {
            _gameState.value = transform(_gameState.value)
        }
    }
    
    suspend fun <T> withCurrentState(action: (GameState) -> T): T {
        return gameStateMutex.withLock {
            action(_gameState.value)
        }
    }
    
    fun getCurrentState(): GameState = _gameState.value
    
    suspend fun resetState() {
        updateGameState { GameState() }
    }
}