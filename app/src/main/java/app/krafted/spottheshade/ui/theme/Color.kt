package app.krafted.spottheshade.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Default theme colors (vibrant but harmonious)
val GradientOrange = Color(0xFFFF6B6B)
val GradientYellow = Color(0xFFFFE66D)
val GradientGreen = Color(0xFF4ECDC4)

// Button gradient colors
val PlayButtonStart = Color(0xFF667EEA)
val PlayButtonEnd = Color(0xFF764BA2)
val DailyChallengeStart = Color(0xFFFF6B6B)
val DailyChallengeEnd = Color(0xFFFFE66D)

// Locked button gradient colors
val LockedButtonStart = Color(0xFFB0B0B0)
val LockedButtonEnd = Color(0xFF8D8D8D)


// Text colors
val TitleTeal = Color(0xFF4ECDC4)
val White = Color(0xFFFFFFFF)
val PurpleBackground = Color(0xFF8B7CE8)

// FOREST Theme Colors - Like a misty forest morning
val ForestDeepShadow = Color(0xFF0D1B2A)      // Deep forest shadows
val ForestDark = Color(0xFF1B4332)           // Dark forest green
val ForestMedium = Color(0xFF2D5016)         // Rich moss green
val ForestLight = Color(0xFF52B788)          // Fresh leaf green
val ForestBright = Color(0xFF74C69D)         // Bright foliage
val ForestMist = Color(0xFFB7E4C7)           // Misty forest light
val ForestSunlight = Color(0xFFD8F3DC)       // Dappled sunlight

// OCEAN Theme Colors - Like ocean depths to surface
val OceanAbyssal = Color(0xFF001219)         // Deep ocean abyss
val OceanDeep = Color(0xFF001D3D)            // Deep ocean blue
val OceanMedium = Color(0xFF003566)          // Ocean blue
val OceanLight = Color(0xFF0077B6)           // Bright ocean blue
val OceanSurface = Color(0xFF00B4D8)         // Ocean surface
val OceanFoam = Color(0xFF90E0EF)            // Ocean foam
val OceanShallow = Color(0xFFCAF0F8)         // Shallow water

// SUNSET Theme Colors - Like a golden hour sunset
val SunsetNight = Color(0xFF2D1B69)          // Twilight purple
val SunsetDark = Color(0xFF6F1D1B)           // Deep sunset red
val SunsetMedium = Color(0xFFBB3E03)         // Sunset orange
val SunsetBright = Color(0xFFFF8500)         // Bright sunset orange
val SunsetGold = Color(0xFFFFB700)           // Golden sunset
val SunsetYellow = Color(0xFFFFD60A)         // Sunset yellow
val SunsetHorizon = Color(0xFFFFF3B0)        // Horizon glow

// WINTER Theme Colors - Like a snowy winter landscape
val WinterNight = Color(0xFF1A1B2E)          // Deep winter night
val WinterDark = Color(0xFF16213E)           // Dark winter blue
val WinterMedium = Color(0xFF0F3460)         // Winter storm blue
val WinterBright = Color(0xFF533E85)         // Aurora purple
val WinterAurora = Color(0xFF7209B7)         // Aurora bright
val WinterSnow = Color(0xFFE7E7E7)           // Fresh snow
val WinterIce = Color(0xFFF4F4F4)            // Ice crystals

// SPRING Theme Colors - Like cherry blossoms and fresh growth
val SpringNight = Color(0xFF2C1810)          // Rich earth
val SpringDark = Color(0xFF6B4423)           // Tree bark
val SpringMedium = Color(0xFF8B6F47)         // Warm earth
val SpringBright = Color(0xFFE8A87C)         // Peach blossoms
val SpringPink = Color(0xFFF8BBD9)           // Cherry blossoms
val SpringGreen = Color(0xFFB8E6B8)          // Fresh leaves
val SpringLight = Color(0xFFE8F5E8)          // Spring morning

// NEON_CYBER Theme Colors - Like a cyberpunk cityscape
val CyberDark = Color(0xFF0D0D0D)            // Deep black
val CyberPurple = Color(0xFF1A0B2E)          // Dark purple
val CyberBlue = Color(0xFF0F1A2E)            // Neon blue base
val CyberElectric = Color(0xFF00D4FF)        // Electric blue
val CyberPink = Color(0xFFFF0080)            // Neon pink
val CyberGreen = Color(0xFF00FF88)           // Neon green
val CyberYellow = Color(0xFFFFFF00)          // Neon yellow

// VOLCANIC Theme Colors - Like lava and volcanic activity
val VolcanicDark = Color(0xFF1A0E0E)         // Deep volcanic rock
val VolcanicRock = Color(0xFF2D1B1B)         // Volcanic rock
val VolcanicEmber = Color(0xFF5D1A1A)        // Glowing embers
val VolcanicLava = Color(0xFF8B0000)         // Molten lava
val VolcanicBright = Color(0xFFFF4500)       // Bright lava
val VolcanicGold = Color(0xFFFFD700)         // Lava gold
val VolcanicYellow = Color(0xFFFFFF99)       // Lava sparks

// ROYAL_GOLD Theme Colors - Premium, luxurious, and exclusive
val RoyalDark = Color(0xFF121212)            // Rich black base
val RoyalPurple = Color(0xFF240046)          // Imperial deep purple
val RoyalVelvet = Color(0xFF3C096C)          // Rich velvet
val RoyalGoldDark = Color(0xFF9D4EDD)        // Deep gold/purple bridge
val RoyalGold = Color(0xFFFFD700)            // Pure gold
val RoyalBright = Color(0xFFFFEA00)          // Bright gold
val RoyalShine = Color(0xFFFFFACD)           // Lemon chiffon shine

// Theme Color Data Class - Extended for wallpaper-like gradients
data class ThemeColors(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    // Extended colors for natural gradients
    val gradientColors: List<Color> = emptyList(),
    val surfaceGradient: List<Color> = emptyList(),
    // Theme-specific UI colors
    val titleColor: Color = White,
    val buttonPrimary: List<Color> = emptyList(),
    val buttonSecondary: List<Color> = emptyList(),
    val textOnButton: Color = White,
    val iconColor: Color = White,
    val overlayColor: Color = Color.Black.copy(alpha = 0.3f)
)
