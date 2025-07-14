package com.example.spottheshade.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.example.spottheshade.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundManager(private val context: Context) {
    
    private var soundEnabled = true
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<SoundType, Int>()

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
        soundMap[SoundType.CORRECT] = soundPool.load(context, R.raw.correct_sound, 1)
        soundMap[SoundType.WRONG] = soundPool.load(context, R.raw.wrong_sound, 1)
        soundMap[SoundType.TIMEOUT] = soundPool.load(context, R.raw.timeout_sound, 1)
        soundMap[SoundType.GAME_OVER] = soundPool.load(context, R.raw.game_over_sound, 1)
    }

    private fun playSound(soundType: SoundType, volume: Float = 1.0f, rate: Float = 1.0f) {
        if (!soundEnabled) return

        val streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (streamVolume == 0) return

        CoroutineScope(Dispatchers.IO).launch {
            soundMap[soundType]?.let { soundId ->
                soundPool.play(soundId, volume, volume, 1, 0, rate)
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