package com.balajitechlabs.quickdash.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.balajitechlabs.quickdash.core.data.dataStore
import com.balajitechlabs.quickdash.core.ui.components.getCustomShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.font.FontFamily
import com.balajitechlabs.quickdash.R


val LocalBorderWidth = staticCompositionLocalOf { 1f }
val LocalShowShadow = staticCompositionLocalOf { true }
val LocalCustomShape = staticCompositionLocalOf<Shape> { RoundedCornerShape(16.dp) }

/** Rotate a Color's hue by [degrees] in HSV space, preserving saturation and value. */
private fun Color.rotateHue(degrees: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt(), hsv
    )
    hsv[0] = (hsv[0] + degrees + 360f) % 360f
    return Color(android.graphics.Color.HSVToColor(hsv))
}

private fun generateColorScheme(seed: Color, isDark: Boolean): ColorScheme {
    val primary = seed
    val onPrimary = if (isLightColor(seed)) Color.Black else Color.White
    val primaryContainer = primary.copy(alpha = 0.2f)
    val onPrimaryContainer = primary

    // Derive secondary/tertiary by rotating hue (+60°/+120°)
    val secondary = seed.rotateHue(60f)
    val tertiary  = seed.rotateHue(120f)

    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = if (isLightColor(secondary)) Color.Black else Color.White,
            secondaryContainer = secondary.copy(alpha = 0.25f),
            onSecondaryContainer = secondary,
            tertiary = tertiary,
            onTertiary = if (isLightColor(tertiary)) Color.Black else Color.White,
            tertiaryContainer = tertiary.copy(alpha = 0.25f),
            onTertiaryContainer = tertiary,
            background = Color(0xFF000000),
            onBackground = Color(0xFFE6E1E5),
            surface = Color(0xFF0D0D0D),
            onSurface = Color(0xFFE6E1E5),
            surfaceVariant = Color(0xFF1A1A1A),
            onSurfaceVariant = Color(0xFFBBB5BF),
            surfaceContainerLowest = Color(0xFF050505),
            surfaceContainerLow = Color(0xFF141414),
            surfaceContainer = Color(0xFF1A1A1A),
            surfaceContainerHigh = Color(0xFF222222),
            surfaceContainerHighest = Color(0xFF2D2D2D),
            outline = Color(0xFF4D4550),
            outlineVariant = Color(0xFF3A3A3A),
            scrim = Color(0xFF000000)
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primary.copy(alpha = 0.12f),
            onPrimaryContainer = primary,
            secondary = secondary,
            onSecondary = if (isLightColor(secondary)) Color.Black else Color.White,
            secondaryContainer = secondary.copy(alpha = 0.12f),
            onSecondaryContainer = secondary,
            tertiary = tertiary,
            onTertiary = if (isLightColor(tertiary)) Color.Black else Color.White,
            tertiaryContainer = tertiary.copy(alpha = 0.12f),
            onTertiaryContainer = tertiary,
            background = Color(0xFFFFFBFE),
            onBackground = Color(0xFF1C1B1F),
            surface = Color(0xFFFFFBFE),
            onSurface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFFECE5EE),
            onSurfaceVariant = Color(0xFF49454E),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF7F2FA),
            surfaceContainer = Color(0xFFF1EBF6),
            surfaceContainerHigh = Color(0xFFEBE5F0),
            surfaceContainerHighest = Color(0xFFE5E0EB),
            outline = Color(0xFF7A757F),
            outlineVariant = Color(0xFFCBC4CF),
            scrim = Color(0xFF000000)
        )
    }
}

private fun isLightColor(color: Color): Boolean {
    val luminance = 0.2126 * color.red + 0.7152 * color.green + 0.0722 * color.blue
    return luminance > 0.5
}

fun ColorScheme.toAmoled(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainer = Color(0xFF0A0A0A),
    surfaceContainerLow = Color.Black
)

private val provider = try {
    GoogleFont.Provider(
        "com.google.android.gms.fonts",
        "com.google.android.gms",
        R.array.com_google_android_gms_fonts_certs
    )
} catch (e: Exception) {
    null
}

fun getGoogleFontFamily(name: String): FontFamily {
    return try {
        val p = provider ?: return FontFamily.Default
        val fontName = GoogleFont(name)
        FontFamily(
            Font(googleFont = fontName, fontProvider = p)
        )
    } catch (e: Exception) {
        FontFamily.Default
    }
}

@Composable
fun QuickDashTheme(
    themeMode: String = "SYSTEM",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    // Use context.dataStore directly (it's the app-wide singleton via the `by preferencesDataStore`
    // delegate) rather than creating a new UserStore instance here. Multiple UserStore instances
    // wrapping the same DataStore file cause IllegalStateException crashes in release builds.
    val prefs by context.dataStore.data.collectAsState(initial = androidx.datastore.preferences.core.emptyPreferences())

    val seedColorHex = prefs[stringPreferencesKey("seed_color")] ?: "#1E88E5"
    val shapeStyle = prefs[stringPreferencesKey("shape_style")] ?: "Rounded"
    val cornerRadius = prefs[floatPreferencesKey("corner_radius")] ?: 16f
    val borderWidth = prefs[floatPreferencesKey("border_width")] ?: 1f
    val fontScale = prefs[floatPreferencesKey("font_scale")] ?: 1f
    val showShadow = prefs[booleanPreferencesKey("show_shadow")] ?: true
    val fontFamilyName = prefs[stringPreferencesKey("font_family_key")] ?: "system"

    val selectedFontFamily = remember(fontFamilyName) {
        when (fontFamilyName.uppercase()) {
            "SANSSERIF" -> FontFamily.SansSerif
            "SERIF" -> FontFamily.Serif
            "MONOSPACE" -> FontFamily.Monospace
            "CURSIVE" -> FontFamily.Cursive
            "NUNITO" -> getGoogleFontFamily("Nunito")
            "POPPINS" -> getGoogleFontFamily("Poppins")
            "SPACE_GROTESK" -> getGoogleFontFamily("Space Grotesk")
            else -> FontFamily.Default
        }
    }


    val seedColor = remember(seedColorHex) {
        try {
            Color(android.graphics.Color.parseColor(seedColorHex))
        } catch (e: Exception) {
            Color(0xFF1E88E5)
        }
    }

    val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        "AMOLED" -> true
        else -> darkTheme
    }

    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> generateColorScheme(seedColor, isDark)
    }

    if (themeMode == "AMOLED") {
        colorScheme = colorScheme.toAmoled()
    }

    val customShape = getCustomShape(shapeStyle, cornerRadius)

    val currentDensity = LocalDensity.current
    val customDensity = Density(density = currentDensity.density, fontScale = fontScale)

    // Build a custom Shapes using the foundation shape directly.
    // Note: We wrap all size tiers with our customShape so that every Material3
    // component (Card, TextField, Dialog, Button) inherits the user's corner-radius.
    val customShapes = androidx.compose.material3.Shapes(
        extraSmall = customShape as? androidx.compose.foundation.shape.CornerBasedShape
            ?: androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius.dp),
        small = customShape as? androidx.compose.foundation.shape.CornerBasedShape
            ?: androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius.dp),
        medium = customShape as? androidx.compose.foundation.shape.CornerBasedShape
            ?: androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius.dp),
        large = customShape as? androidx.compose.foundation.shape.CornerBasedShape
            ?: androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius.dp),
        extraLarge = customShape as? androidx.compose.foundation.shape.CornerBasedShape
            ?: androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius.dp)
    )

    CompositionLocalProvider(
        LocalDensity provides customDensity,
        LocalBorderWidth provides borderWidth,
        LocalShowShadow provides showShadow,
        LocalCustomShape provides customShape
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = customShapes,
            typography = getTypography(selectedFontFamily),
            content = content
        )
    }
}
