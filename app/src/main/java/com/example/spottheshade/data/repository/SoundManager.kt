package com.example.spottheshade.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.example.spottheshade.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    private val context: Context,
    private val scope: CoroutineScope
) {
    
    private var soundEnabled = true
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<SoundType, Int>()
    private var currentTimeoutStreamId: Int? = null

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds()
    }

    private fun loadSounds() {
        try {
            soundMap[SoundType.CORRECT] = soundPool.load(context, R.raw.correct_sound, 1)
            soundMap[SoundType.WRONG] = soundPool.load(context, R.raw.wrong_sound, 1)
            soundMap[SoundType.TIMEOUT] = soundPool.load(context, R.raw.timeout_sound, 1)
            soundMap[SoundType.GAME_OVER] = soundPool.load(context, R.raw.game_over_sound, 1)
        } catch (e: android.content.res.Resources.NotFoundException) {
            // Sound file not found in resources
            android.util.Log.e("SoundManager", "Sound file not found in resources", e)
        } catch (e: OutOfMemoryError) {
            // Not enough memory to load sounds
            android.util.Log.e("SoundManager", "Out of memory while loading sounds", e)
        } catch (e: Exception) {
            // Fallback for any other unexpected exceptions
            android.util.Log.e("SoundManager", "Unexpected error loading sounds", e)
        }
    }

    private fun playSound(soundType: SoundType, volume: Float = 1.0f, rate: Float = 1.0f) {
        if (!soundEnabled) return

        val streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (streamVolume == 0) return

        scope.launch(Dispatchers.IO) {
            soundMap[soundType]?.let { soundId ->
                val streamId = soundPool.play(soundId, volume, volume, 1, 0, rate)
                if (soundType == SoundType.TIMEOUT) {
                    currentTimeoutStreamId = streamId
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
    
    fun isSoundEnabled(): Boolean = soundEnabled
    
    fun stopTimeoutSound() {
        currentTimeoutStreamId?.let { streamId ->
            soundPool.stop(streamId)
            currentTimeoutStreamId = null
        }
    }
    
    fun release() {
        soundPool.release()
    }
    
    enum class SoundType {
        CORRECT,
        WRONG, 
        TIMEOUT,
        GAME_OVER
    }
} 