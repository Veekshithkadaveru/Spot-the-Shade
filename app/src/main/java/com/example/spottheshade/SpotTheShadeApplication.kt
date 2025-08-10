package com.example.spottheshade

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.spottheshade.services.SoundManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SpotTheShadeApplication : Application(), DefaultLifecycleObserver {
    
    @Inject
    lateinit var soundManager: SoundManager
    
    override fun onCreate() {
        super<Application>.onCreate()
        
        // Register for app lifecycle events
        try {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        } catch (e: Exception) {
            // ProcessLifecycleOwner not available on older devices
            android.util.Log.w("SpotTheShadeApp", "ProcessLifecycleOwner not available", e)
        }
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStop(owner)
        // App going to background - cleanup sounds
        if (::soundManager.isInitialized) {
            soundManager.onAppBackground()
        }
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onDestroy(owner)
        // App being destroyed - release resources
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }
}