/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui/theme
 * File: Theme.kt
 * Description: Central Material 3 theme composable applying dynamic color, pitch-black AMOLED mode, and typography.
 * Developer: balajitechlabs
 */
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle


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

val EssentialXDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFB0C6FF),
    onPrimary = Color(0xFF192F60),
    primaryContainer = Color(0xFF2F4578),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFC5C6D0),
    onSecondary = Color(0xFF2E3036),
    secondaryContainer = Color(0xFF38393F),
    onSecondaryContainer = Color(0xFFE2E2E6),
    tertiary = Color(0xFFB39DDB),
    onTertiary = Color(0xFF381E72),
    tertiaryContainer = Color(0xFF4F378B),
    onTertiaryContainer = Color(0xFFEADDFF),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF28292E),
    onSurfaceVariant = Color(0xFFC5C6D0),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF1E1E22),
    surfaceContainer = Color(0xFF2C2D33),
    surfaceContainerHigh = Color(0xFF38393F),
    surfaceContainerHighest = Color(0xFF45464F),
    surfaceBright = Color(0xFF38393F),
    outline = Color(0xFF44474F),
    outlineVariant = Color(0xFF38393F),
    scrim = Color(0xFF000000)
)

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
            outlineVariant = Color(0xFF28282E),
            scrim = Color(0xFF000000)
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primary.copy(alpha = 0.14f),
            onPrimaryContainer = primary,
            secondary = secondary,
            onSecondary = if (isLightColor(secondary)) Color.Black else Color.White,
            secondaryContainer = secondary.copy(alpha = 0.12f),
            onSecondaryContainer = secondary,
            tertiary = tertiary,
            onTertiary = if (isLightColor(tertiary)) Color.Black else Color.White,
            tertiaryContainer = tertiary.copy(alpha = 0.12f),
            onTertiaryContainer = tertiary,
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1F1F1F),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1F1F1F),
            surfaceVariant = Color(0xFFF1F3F4),
            onSurfaceVariant = Color(0xFF5F6368),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF8F9FA),
            surfaceContainer = Color(0xFFF1F3F4),
            surfaceContainerHigh = Color(0xFFE8EAED),
            surfaceContainerHighest = Color(0xFFE0E2E5),
            surfaceBright = Color(0xFFFFFFFF),
            outline = Color(0xFFDADCE0),
            outlineVariant = Color(0xFFE8EAED),
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
    surfaceBright = Color(0xFF141414),
    surfaceVariant = Color(0xFF121214),
    surfaceContainer = Color.Black,
    surfaceContainerLow = Color.Black,
    surfaceContainerHigh = Color(0xFF121214),
    surfaceContainerHighest = Color(0xFF18181C),
    surfaceContainerLowest = Color.Black,
    onSurface = Color.White,
    onBackground = Color.White,
    outline = Color(0xFF444444),
    outlineVariant = Color(0xFF242428),
    scrim = Color.Black
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
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    // Use context.dataStore directly (it's the app-wide singleton via the `by preferencesDataStore`
    // delegate) rather than creating a new UserStore instance here. Multiple UserStore instances
    // wrapping the same DataStore file cause IllegalStateException crashes in release builds.
    val prefs by context.dataStore.data.collectAsStateWithLifecycle(initialValue = androidx.datastore.preferences.core.emptyPreferences())

    val seedColorHex = prefs[stringPreferencesKey("seed_color")] ?: "#1E88E5"
    val shapeStyle = prefs[stringPreferencesKey("shape_style")] ?: "Rounded"
    val cornerRadius = prefs[floatPreferencesKey("corner_radius")] ?: 16f
    val borderWidth = prefs[floatPreferencesKey("border_width")] ?: 1f
    val fontScale = prefs[floatPreferencesKey("font_scale")] ?: 1f
    val showShadow = prefs[booleanPreferencesKey("show_shadow")] ?: true
    val fontFamilyName = prefs[stringPreferencesKey("font_family_key")] ?: "SPACE_GROTESK"

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

    val colorScheme = remember(dynamicColor) {
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val dyn = dynamicDarkColorScheme(context)
                EssentialXDarkColorScheme.copy(
                    primary = dyn.primary,
                    onPrimary = dyn.onPrimary,
                    primaryContainer = dyn.primaryContainer,
                    onPrimaryContainer = dyn.onPrimaryContainer
                )
            } catch (_: Exception) {
                EssentialXDarkColorScheme
            }
        } else {
            EssentialXDarkColorScheme
        }
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
