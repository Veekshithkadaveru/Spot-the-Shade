package app.krafted.spottheshade.data.repository

import android.content.Context
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun showError(error: UserError) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
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
