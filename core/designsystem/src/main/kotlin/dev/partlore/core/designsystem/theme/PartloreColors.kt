package dev.partlore.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Every color the UI may use. Screens read these through PartloreTheme.colors, never as raw hex. */
@Immutable
data class PartloreColors(
    val isDark: Boolean,
    val bg: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceSunken: Color,
    val outline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val warningContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val info: Color,
    val shadowTint: Color,
)

internal val LightColors =
    PartloreColors(
        isDark = false,
        bg = Color(0xFFFBF8F3),
        surface = Color(0xFFFFFFFF),
        // Light mode shows elevation with shadows, so raised surfaces stay white.
        surfaceRaised = Color(0xFFFFFFFF),
        surfaceSunken = Color(0xFFF3EEE6),
        outline = Color(0xFFDDD5C8),
        textPrimary = Color(0xFF1C1B22),
        textSecondary = Color(0xFF55525E),
        primary = Color(0xFF5B3FD9),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE9E3FF),
        accent = Color(0xFFAB4F0E),
        success = Color(0xFF1F7A4D),
        warning = Color(0xFF8A5A00),
        warningContainer = Color(0xFFFFF4D6),
        danger = Color(0xFFC2273D),
        dangerContainer = Color(0xFFFDE8EB),
        info = Color(0xFF0068A3),
        shadowTint = Color(0xFF413663),
    )

internal val DarkColors =
    PartloreColors(
        isDark = true,
        bg = Color(0xFF121019),
        surface = Color(0xFF1B1824),
        surfaceRaised = Color(0xFF24202F),
        surfaceSunken = Color(0xFF0D0B12),
        outline = Color(0xFF3A3548),
        textPrimary = Color(0xFFF2EFF7),
        textSecondary = Color(0xFFB9B4C6),
        primary = Color(0xFFA897FF),
        onPrimary = Color(0xFF1C1050),
        primaryContainer = Color(0xFF2E2650),
        accent = Color(0xFFF0A060),
        success = Color(0xFF5FD39A),
        warning = Color(0xFFFFC94D),
        warningContainer = Color(0xFF3A2C0A),
        danger = Color(0xFFFF7A8A),
        dangerContainer = Color(0xFF3D1620),
        info = Color(0xFF56B4E9),
        // Dark mode shows elevation with lighter surfaces and a top highlight, not shadows.
        shadowTint = Color(0xFF000000),
    )

/** Darker background for use at the bench. Other colors follow the dark theme for now. */
internal val BenchColors =
    DarkColors.copy(
        bg = Color(0xFF0B0A0F),
        textPrimary = Color(0xFFE6E1EE),
    )

/** Token names and values, in display order. Used by the Design Catalog. */
fun PartloreColors.namedColors(): List<Pair<String, Color>> = listOf(
    "bg" to bg,
    "surface" to surface,
    "surfaceRaised" to surfaceRaised,
    "surfaceSunken" to surfaceSunken,
    "outline" to outline,
    "textPrimary" to textPrimary,
    "textSecondary" to textSecondary,
    "primary" to primary,
    "onPrimary" to onPrimary,
    "primaryContainer" to primaryContainer,
    "accent" to accent,
    "success" to success,
    "warning" to warning,
    "warningContainer" to warningContainer,
    "danger" to danger,
    "dangerContainer" to dangerContainer,
    "info" to info,
)
