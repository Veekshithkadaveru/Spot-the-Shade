package com.example.spottheshade.viewmodel

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameTimer(
    private val scope: CoroutineScope,
    private val onTick: (Int) -> Unit,
    private val onFinish: () -> Unit
) {
    private var job: Job? = null
    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining = _timeRemaining.asStateFlow()

    fun start(totalSeconds: Int) {
        stop()
        _timeRemaining.value = totalSeconds
        job = scope.launch {
            for (i in totalSeconds downTo 1) {
                delay(1000)
                _timeRemaining.value = i - 1
                onTick(i - 1)
            }
            onFinish()
        }
    }

    fun stop() {
        job?.cancel()
    }
} 