package app.krafted.spottheshade.game

import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class TimerManager(
    private val scope: CoroutineScope,
    private val onTimeUpdate: (Int) -> Unit,
    private val onTimeWarning: () -> Unit,
    private val onTimeCritical: () -> Unit,
    private val onTimeUrgent: () -> Unit,
    private val onTimeout: () -> Unit
) {
    private var timerJob: Job? = null

    fun startTimer(totalSeconds: Int) {
        cancelTimer()

        onTimeUpdate(totalSeconds)

        timerJob = scope.launch {
            try {
                for (timeLeft in (totalSeconds - 1) downTo 0) {
                    delay(1000)

                    when (timeLeft) {
                        5 -> onTimeWarning()
                        3 -> onTimeCritical()
                        1 -> onTimeUrgent()
                    }

                    onTimeUpdate(timeLeft)
                }

                onTimeout()
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
