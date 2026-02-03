package app.krafted.spottheshade.ui.util

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import app.krafted.spottheshade.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Composable that handles error events from the ViewModel and displays them as Toasts.
 * This keeps the Toast/UI display logic in the UI layer where it belongs.
 */
@Composable
fun ErrorEventHandler(viewModel: GameViewModel) {
    val context = LocalContext.current

    LaunchedEffect(viewModel.errorEvents) {
        viewModel.errorEvents.collectLatest { error ->
            Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
        }
    }
}
