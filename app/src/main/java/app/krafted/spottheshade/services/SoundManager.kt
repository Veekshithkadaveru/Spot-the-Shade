package app.krafted.spottheshade.services

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import app.krafted.spottheshade.R
import app.krafted.spottheshade.data.repository.ErrorFeedbackManager
import app.krafted.spottheshade.data.repository.UserError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scope: CoroutineScope,
    private val errorFeedbackManager: ErrorFeedbackManager
) {

    private var soundEnabled = true
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<SoundType, Int>()
    private var currentTimeoutStreamId: Int? = null
    private var isReleased = false

    init {
        initializeSoundPool()
    }

    private fun initializeSoundPool() {
        if (soundPool != null || isReleased) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSoundsWithRetry()
    }


    private fun loadSounds() {
        val pool = soundPool ?: return
        try {
            soundMap[SoundType.CORRECT] = pool.load(context, R.raw.correct_sound, 1)
            soundMap[SoundType.WRONG] = pool.load(context, R.raw.wrong_sound, 1)
            soundMap[SoundType.TIMEOUT] = pool.load(context, R.raw.timeout_sound, 1)
            soundMap[SoundType.GAME_OVER] = pool.load(context, R.raw.game_over_sound, 1)
        } catch (e: android.content.res.Resources.NotFoundException) {
            android.util.Log.e("SoundManager", "Sound file not found in resources", e)
            throw RuntimeException("Sound files not found in resources", e)
        } catch (e: OutOfMemoryError) {
            android.util.Log.e("SoundManager", "Out of memory while loading sounds", e)
            throw RuntimeException("Out of memory while loading sounds", e)
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Unexpected error loading sounds", e)
            throw e
        }
    }

    private fun loadSoundsWithRetry(maxRetries: Int = 3) {
        scope.launch {
            repeat(maxRetries) { attempt ->
                try {
                    loadSounds()
                    return@launch // Success
                } catch (e: Exception) {
                    if (attempt == maxRetries - 1) {
                        // Final attempt failed - show user feedback and enable silent mode
                        android.util.Log.e("SoundManager", "All sound loading attempts failed", e)
                        errorFeedbackManager.emitError(UserError.SoundLoadFailed)
                        setSoundEnabled(false)
                    } else {
                        // Wait with exponential backoff before retry
                        delay(500L * (attempt + 1))
                        android.util.Log.w("SoundManager", "Sound loading attempt ${attempt + 1} failed, retrying...", e)
                    }
                }
            }
        }
    }

    private fun playSound(soundType: SoundType, volume: Float = 1.0f, rate: Float = 1.0f) {
        if (!soundEnabled || isReleased) return

        val pool = soundPool ?: return
        val streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (streamVolume == 0) return

        scope.launch(Dispatchers.IO) {
            try {
                soundMap[soundType]?.let { soundId ->
                    val streamId = pool.play(soundId, volume, volume, 1, 0, rate)
                    if (soundType == SoundType.TIMEOUT) {
                        currentTimeoutStreamId = streamId
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("SoundManager", "Sound playback failed for $soundType", e)
                // Continue silently - sound failure shouldn't crash game
                // Only show error for critical sounds or repeated failures
                if (soundType == SoundType.GAME_OVER) {
                    errorFeedbackManager.emitError(UserError.SoundPlaybackFailed)
                }
            }
        }
    }

    fun playCorrectSound() {
        playSound(SoundType.CORRECT)
    }

    fun playWrongSound() {
        playSound(SoundType.WRONG, volume = 0.8f)
    }

    fun playTimeoutSound() {
        playSound(SoundType.TIMEOUT, volume = 0.7f, rate = 1.2f)
    }

    fun playGameOverSound() {
        playSound(SoundType.GAME_OVER)
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }

    val isSoundEnabled: Boolean
        get() = soundEnabled

    fun stopTimeoutSound() {
        currentTimeoutStreamId?.let { streamId ->
            soundPool?.stop(streamId)
            currentTimeoutStreamId = null
        }
    }

    fun release() {
        if (isReleased) return

        isReleased = true
        currentTimeoutStreamId = null
        soundMap.clear()

        soundPool?.release()
        soundPool = null
    }

    /**
     * Call this method when the app goes to background to save battery
     */
    fun onAppBackground() {
        stopTimeoutSound()
    }

    enum class SoundType {
        CORRECT,
        WRONG,
        TIMEOUT,
        GAME_OVER
    }
}
