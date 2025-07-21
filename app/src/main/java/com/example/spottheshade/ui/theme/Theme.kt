package com.example.spottheshade.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.spottheshade.data.model.ThemeType

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

// Theme-specific color schemes
fun getThemeColors(themeType: ThemeType): ThemeColors {
    return when (themeType) {
        ThemeType.DEFAULT -> ThemeColors(
            primary = GradientOrange,
            secondary = GradientYellow,
            accent = GradientGreen,
            background = White,
            surface = White,
            onSurface = Purple40,
            gradientColors = listOf(GradientOrange, GradientYellow, GradientGreen),
            surfaceGradient = listOf(White, White),
            titleColor = Color.White,
            buttonPrimary = listOf(PlayButtonStart, PlayButtonEnd),
            buttonSecondary = listOf(DailyChallengeStart, DailyChallengeEnd),
            textOnButton = Color.White,
            iconColor = Color.White,
            overlayColor = Color.Black.copy(alpha = 0.3f)
        )
        ThemeType.FOREST -> ThemeColors(
            primary = ForestDark,
            secondary = ForestMedium,
            accent = ForestLight,
            background = ForestSunlight,
            surface = ForestMist,
            onSurface = ForestDark,
            // Forest morning mist gradient - from deep shadows to dappled sunlight
            gradientColors = listOf(
                ForestDeepShadow,
                ForestDark,
                ForestMedium,
                ForestLight,
                ForestBright,
                ForestMist,
                ForestSunlight
            ),
            surfaceGradient = listOf(ForestMist, ForestSunlight),
            titleColor = Color.White,
            buttonPrimary = listOf(ForestDark, ForestLight),
            buttonSecondary = listOf(ForestMedium, ForestBright),
            textOnButton = Color.White,
            iconColor = Color.White,
            overlayColor = ForestDeepShadow.copy(alpha = 0.4f)
        )
        ThemeType.OCEAN -> ThemeColors(
            primary = OceanDeep,
            secondary = OceanMedium,
            accent = OceanLight,
            background = OceanShallow,
            surface = OceanFoam,
            onSurface = OceanDeep,
            // Ocean depth gradient - from abyss to surface
            gradientColors = listOf(
                OceanAbyssal,
                OceanDeep,
                OceanMedium,
                OceanLight,
                OceanSurface,
                OceanFoam,
                OceanShallow
            ),
            surfaceGradient = listOf(OceanFoam, OceanShallow),
            titleColor = Color.White,
            buttonPrimary = listOf(OceanDeep, OceanSurface),
            buttonSecondary = listOf(OceanMedium, OceanLight),
            textOnButton = Color.White,
            iconColor = Color.White,
            overlayColor = OceanAbyssal.copy(alpha = 0.4f)
        )
        ThemeType.SUNSET -> ThemeColors(
            primary = SunsetDark,
            secondary = SunsetMedium,
            accent = SunsetBright,
            background = SunsetHorizon,
            surface = SunsetYellow,
            onSurface = SunsetDark,
            // Sunset sky gradient - from twilight to golden horizon
            gradientColors = listOf(
                SunsetNight,
                SunsetDark,
                SunsetMedium,
                SunsetBright,
                SunsetGold,
                SunsetYellow,
                SunsetHorizon
            ),
            surfaceGradient = listOf(SunsetYellow, SunsetHorizon),
            titleColor = Color.White,
            buttonPrimary = listOf(SunsetDark, SunsetGold),
            buttonSecondary = listOf(SunsetMedium, SunsetYellow),
            textOnButton = Color.White,
            iconColor = Color.White,
            overlayColor = SunsetNight.copy(alpha = 0.4f)
        )
        ThemeType.WINTER -> ThemeColors(
            primary = WinterDark,
            secondary = WinterMedium,
            accent = WinterBright,
            background = WinterIce,
            surface = WinterSnow,
            onSurface = WinterDark,
            // Winter aurora gradient - from deep night to ice crystals
            gradientColors = listOf(
                WinterNight,
                WinterDark,
                WinterMedium,
                WinterBright,
                WinterAurora,
                WinterSnow,
                WinterIce
            ),
            surfaceGradient = listOf(WinterSnow, WinterIce),
            titleColor = Color.White,
            buttonPrimary = listOf(WinterDark, WinterAurora),
            buttonSecondary = listOf(WinterMedium, WinterBright),
            textOnButton = Color.White,
            iconColor = Color.White,
            overlayColor = WinterNight.copy(alpha = 0.5f)
        )
        ThemeType.SPRING -> ThemeColors(
            primary = SpringDark,
            secondary = SpringMedium,
            accent = SpringBright,
            background = SpringLight,
            surface = SpringGreen,
            onSurface = SpringDark,
            // Spring bloom gradient - from earth to blossoms
            gradientColors = listOf(
                SpringNight,
                SpringDark,
                SpringMedium,
                SpringBright,
                SpringPink,
                SpringGreen,
                SpringLight
            ),
            surfaceGradient = listOf(SpringGreen, SpringLight),
            titleColor = Color.White,
            buttonPrimary = listOf(SpringDark, SpringPink),
            buttonSecondary = listOf(SpringMedium, SpringBright),
            textOnButton = Color.White,
            iconColor = Color.White,
            overlayColor = SpringNight.copy(alpha = 0.4f)
        )
        ThemeType.NEON_CYBER -> ThemeColors(
            primary = CyberPurple,
            secondary = CyberPurple,
            accent = CyberElectric,
            background = CyberDark,
            surface = CyberPurple,
            onSurface = CyberElectric,
            // Cyberpunk neon gradient - electric colors
            gradientColors = listOf(
                CyberDark,
                CyberPurple,
                CyberBlue,
                CyberElectric,
                CyberPink,
                CyberGreen,
                CyberYellow
            ),
            surfaceGradient = listOf(CyberPurple, CyberBlue),
            titleColor = Color.White,
            buttonPrimary = listOf(CyberElectric, CyberPink),
            buttonSecondary = listOf(CyberGreen, CyberYellow),
            textOnButton = Color.White,
            iconColor = Color.White,
            overlayColor = CyberDark.copy(alpha = 0.7f)
        )
        ThemeType.VOLCANIC -> ThemeColors(
            primary = VolcanicDark,
            secondary = VolcanicRock,
            accent = VolcanicLava,
            background = VolcanicYellow,
            surface = VolcanicGold,
            onSurface = VolcanicDark,
            // Volcanic gradient - from deep rock to molten lava
            gradientColors = listOf(
                VolcanicDark,
                VolcanicRock,
                VolcanicEmber,
                VolcanicLava,
                VolcanicBright,
                VolcanicGold,
                VolcanicYellow
            ),
            surfaceGradient = listOf(VolcanicGold, VolcanicYellow),
            titleColor = Color.White,
            buttonPrimary = listOf(VolcanicLava, VolcanicBright),
            buttonSecondary = listOf(VolcanicEmber, VolcanicGold),
            textOnButton = Color.White,
            iconColor = Color.White,
            overlayColor = VolcanicDark.copy(alpha = 0.5f)
        )
    }
}

// Composition Local for theme colors
val LocalThemeColors = staticCompositionLocalOf { 
    getThemeColors(ThemeType.DEFAULT) 
}

@Composable
fun SpotTheShadeTheme(
    themeType: ThemeType = ThemeType.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+, but disabled for custom themes
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Disable dynamic colors when using custom themes to preserve theme aesthetics
    val useDynamicColors = dynamicColor && themeType == ThemeType.DEFAULT
    
    val colorScheme = when {
        useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val themeColors = getThemeColors(themeType)

    CompositionLocalProvider(LocalThemeColors provides themeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}