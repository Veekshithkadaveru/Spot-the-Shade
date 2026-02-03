package app.krafted.spottheshade.ui.util

import androidx.compose.ui.graphics.Color
import app.krafted.spottheshade.data.model.HSLColor

/**
 * Converts HSLColor to Compose Color.
 * This extension lives in the UI layer to keep Compose dependencies out of the data layer.
 */
fun HSLColor.toComposeColor(): Color {
    return Color.hsl(hue, saturation, lightness)
}
