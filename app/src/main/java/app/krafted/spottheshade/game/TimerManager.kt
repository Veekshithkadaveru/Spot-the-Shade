package app.krafted.spottheshade.game

import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed class TimerEvent {
    data class Tick(val secondsLeft: Int) : TimerEvent()
    object Warning : TimerEvent()
    object Critical : TimerEvent()
    object Urgent : TimerEvent()
    object Timeout : TimerEvent()
}

class TimerManager(
    private val scope: CoroutineScope,
    private val onEvent: (TimerEvent) -> Unit
) {
    private var timerJob: Job? = null

    fun startTimer(totalSeconds: Int) {
        cancelTimer()

        onEvent(TimerEvent.Tick(totalSeconds))

        timerJob = scope.launch {
            try {
                for (timeLeft in (totalSeconds - 1) downTo 0) {
                    delay(1000)

                    when (timeLeft) {
                        5 -> onEvent(TimerEvent.Warning)
                        3 -> onEvent(TimerEvent.Critical)
                        1 -> onEvent(TimerEvent.Urgent)
                    }

                    onEvent(TimerEvent.Tick(timeLeft))
                }

                onEvent(TimerEvent.Timeout)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("TimerManager", "Timer error", e)
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun isActive(): Boolean = timerJob?.isActive == true
}
