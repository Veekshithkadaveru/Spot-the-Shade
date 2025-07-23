package com.example.spottheshade.data.repository

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HapticManager @Inject constructor() {
    
    /**
     * Provides different haptic feedback patterns for various game events
     */
    fun correctTap(haptic: HapticFeedback) {

        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    fun wrongTap(haptic: HapticFeedback) {

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun timeout(haptic: HapticFeedback, scope: CoroutineScope) {

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            delay(100)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    fun levelUp(haptic: HapticFeedback) {

        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    fun gameOver(haptic: HapticFeedback) {

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun themeSelect(haptic: HapticFeedback) {

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun buttonPress(haptic: HapticFeedback) {

        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    fun gridShake(haptic: HapticFeedback) {

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun timeWarning(haptic: HapticFeedback) {

        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    fun timeCritical(haptic: HapticFeedback, scope: CoroutineScope) {

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            delay(150)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    
    fun timeUrgent(haptic: HapticFeedback, scope: CoroutineScope) {

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            delay(100)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(100)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    fun answerReveal(haptic: HapticFeedback, scope: CoroutineScope) {

        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        scope.launch {
            delay(300)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(200)

            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}