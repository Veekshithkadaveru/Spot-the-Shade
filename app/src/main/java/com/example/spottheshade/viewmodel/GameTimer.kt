package com.example.spottheshade.viewmodel

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.spottheshade.data.repository.ErrorFeedbackManager
import com.example.spottheshade.data.repository.UserError

class GameTimer(
    private val scope: CoroutineScope,
    private val onTick: (Int) -> Unit,
    private val onFinish: () -> Unit,
    private val errorFeedbackManager: ErrorFeedbackManager? = null
) {
    private var job: Job? = null
    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining = _timeRemaining.asStateFlow()

    fun start(totalSeconds: Int) {
        stop()
        _timeRemaining.value = totalSeconds
        job = scope.launch {
            try {
                for (i in totalSeconds downTo 1) {
                    delay(1000)
                    _timeRemaining.value = i - 1
                    onTick(i - 1)
                }
                onFinish()
            } catch (e: CancellationException) {
                // Timer was cancelled - this is expected behavior
                android.util.Log.d("GameTimer", "Timer was cancelled")
                throw e // Re-throw cancellation to properly handle it
            } catch (e: Exception) {
                android.util.Log.e("GameTimer", "Timer failed unexpectedly", e)
                errorFeedbackManager?.showError(UserError.TimerFailed)
                // Ensure game doesn't hang - trigger finish
                onFinish()
            }
        }
    }

    fun stop() {
        try {
            job?.cancel()
        } catch (e: Exception) {
            android.util.Log.w("GameTimer", "Error stopping timer", e)
            // Continue - stopping timer should not fail silently
        }
    }
} 