package com.example.spottheshade.game

import com.example.spottheshade.services.SoundManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

sealed class GameUiEvent {
    data class CorrectTap(val itemId: Int) : GameUiEvent()
    data class IncorrectTap(val itemId: Int) : GameUiEvent()
    object ShakeGrid : GameUiEvent()
    object Timeout : GameUiEvent()
    object LevelUp : GameUiEvent()
    object GameOver : GameUiEvent()
    object TimeWarning : GameUiEvent()
    object TimeCritical : GameUiEvent()
    object TimeUrgent : GameUiEvent()
    data class RevealAnswer(val targetId: Int) : GameUiEvent()
}

class GameEventManager @Inject constructor(
    private val soundManager: SoundManager
) {
    private val _uiEvents = MutableSharedFlow<GameUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()
    
    suspend fun emitEvent(event: GameUiEvent) {
        when (event) {
            is GameUiEvent.CorrectTap -> {
                soundManager.playCorrectSound()
                _uiEvents.emit(event)
                _uiEvents.emit(GameUiEvent.LevelUp)
            }
            is GameUiEvent.IncorrectTap -> {
                soundManager.playWrongSound()
                _uiEvents.emit(event)
                _uiEvents.emit(GameUiEvent.ShakeGrid)
            }
            GameUiEvent.Timeout -> {
                _uiEvents.emit(event)
            }
            GameUiEvent.TimeWarning -> {
                soundManager.playTimeoutSound()
                _uiEvents.emit(event)
            }
            GameUiEvent.TimeCritical,
            GameUiEvent.TimeUrgent -> {
                _uiEvents.emit(event)
            }
            GameUiEvent.GameOver -> {
                soundManager.playGameOverSound()
                _uiEvents.emit(event)
            }
            is GameUiEvent.RevealAnswer -> {
                _uiEvents.emit(event)
            }
            else -> {
                _uiEvents.emit(event)
            }
        }
    }
    
    fun stopTimeoutSound() {
        soundManager.stopTimeoutSound()
    }
}