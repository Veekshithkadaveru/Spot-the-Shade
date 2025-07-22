package com.example.spottheshade.data.repository

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HapticManager @Inject constructor() {
    
    /**
     * Provides different haptic feedback patterns for various game events
     */
    fun correctTap(haptic: HapticFeedback) {
        // Light, satisfying feedback for correct answers
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    fun wrongTap(haptic: HapticFeedback) {
        // More intense feedback for wrong answers
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun timeout(haptic: HapticFeedback) {
        // Double pulse for timeout events
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun levelUp(haptic: HapticFeedback) {
        // Celebratory feedback for progressing levels
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    fun gameOver(haptic: HapticFeedback) {
        // Strong feedback for game over
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun themeSelect(haptic: HapticFeedback) {
        // Current theme selection feedback
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun buttonPress(haptic: HapticFeedback) {
        // Generic button press feedback
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    fun gridShake(haptic: HapticFeedback) {
        // Feedback for grid shake animation on wrong answer
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}