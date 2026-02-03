package app.krafted.spottheshade.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages error events in a framework-agnostic way.
 * Emits errors via Flow - UI layer handles display (Toast, Snackbar, etc.)
 */
@Singleton
class ErrorFeedbackManager @Inject constructor() {

    private val _errorEvents = MutableSharedFlow<UserError>(extraBufferCapacity = 1)
    val errorEvents: SharedFlow<UserError> = _errorEvents.asSharedFlow()

    fun emitError(error: UserError) {
        _errorEvents.tryEmit(error)
    }
}

sealed class UserError(val message: String) {
    object SoundLoadFailed : UserError("Sound effects couldn't be loaded")
    object SoundPlaybackFailed : UserError("Sound playback failed")
    object ThemeLoadFailed : UserError("Theme couldn't be applied")
    object ThemeCorrupted : UserError("Theme settings were corrupted and reset")
    object PreferencesSaveFailed : UserError("Settings couldn't be saved")
    object NavigationFailed : UserError("Navigation failed")
    object TimerFailed : UserError("Game timer encountered an error")
}
