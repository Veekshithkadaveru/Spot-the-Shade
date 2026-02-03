package app.krafted.spottheshade.data.model

/**
 * Framework-agnostic HSL color representation for the data layer.
 * Conversion to Compose Color should only happen in the UI layer.
 *
 * @property hue Hue component (0-360 degrees)
 * @property saturation Saturation component (0-1)
 * @property lightness Lightness component (0-1)
 */
data class HSLColor(
    val hue: Float,
    val saturation: Float,
    val lightness: Float
) {
    init {
        require(hue in 0f..360f) { "Hue must be between 0 and 360, was $hue" }
        require(saturation in 0f..1f) { "Saturation must be between 0 and 1, was $saturation" }
        require(lightness in 0f..1f) { "Lightness must be between 0 and 1, was $lightness" }
    }

    companion object {
        /**
         * Creates an HSLColor with values clamped to valid ranges.
         * Use this when values may be slightly out of range due to calculations.
         */
        fun fromComponents(hue: Float, saturation: Float, lightness: Float): HSLColor {
            return HSLColor(
                hue = hue.coerceIn(0f, 360f),
                saturation = saturation.coerceIn(0f, 1f),
                lightness = lightness.coerceIn(0f, 1f)
            )
        }
    }
}
