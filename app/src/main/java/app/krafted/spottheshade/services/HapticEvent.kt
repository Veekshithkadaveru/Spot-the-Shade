package app.krafted.spottheshade.services

/**
 * Sealed class representing haptic feedback events.
 * ViewModel emits these events, UI layer handles the actual haptic feedback.
 */
sealed class HapticEvent {
    object CorrectTap : HapticEvent()
    object WrongTap : HapticEvent()
    object Timeout : HapticEvent()
    object LevelUp : HapticEvent()
    object GameOver : HapticEvent()
    object ThemeSelect : HapticEvent()
    object ButtonPress : HapticEvent()
    object GridShake : HapticEvent()
    object TimeWarning : HapticEvent()
    object TimeCritical : HapticEvent()
    object TimeUrgent : HapticEvent()
    object AnswerReveal : HapticEvent()
}
