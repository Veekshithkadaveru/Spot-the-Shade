package app.krafted.spottheshade.game

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

class GameEventManager @Inject constructor() {
    private val _uiEvents = MutableSharedFlow<GameUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    suspend fun emitEvent(event: GameUiEvent) {
        when (event) {
            is GameUiEvent.CorrectTap -> {
                _uiEvents.emit(event)
                _uiEvents.emit(GameUiEvent.LevelUp)
            }
            is GameUiEvent.IncorrectTap -> {
                _uiEvents.emit(event)
                _uiEvents.emit(GameUiEvent.ShakeGrid)
            }
            GameUiEvent.Timeout -> {
                _uiEvents.emit(event)
            }
            GameUiEvent.TimeWarning -> {
                _uiEvents.emit(event)
            }
            GameUiEvent.TimeCritical,
            GameUiEvent.TimeUrgent -> {
                _uiEvents.emit(event)
            }
            GameUiEvent.GameOver -> {
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
}
