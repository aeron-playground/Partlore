package dev.partlore.core.designsystem.theme

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class PartloreThemeMode { Light, Dark, Bench }

internal val LocalPartloreColors = staticCompositionLocalOf { LightColors }
internal val LocalPartloreTypography = staticCompositionLocalOf { DefaultTypography }
internal val LocalPartloreMotion = staticCompositionLocalOf { PartloreMotion(reduced = false) }

@Composable
fun PartloreTheme(
    mode: PartloreThemeMode = if (isSystemInDarkTheme()) PartloreThemeMode.Dark else PartloreThemeMode.Light,
    motionPreference: MotionPreference = MotionPreference.System,
    content: @Composable () -> Unit,
) {
    val colors =
        when (mode) {
            PartloreThemeMode.Light -> LightColors
            PartloreThemeMode.Dark -> DarkColors
            PartloreThemeMode.Bench -> BenchColors
        }
    val context = LocalContext.current
    val animatorScale =
        remember(context) {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }
    val motion =
        remember(motionPreference, animatorScale) {
            PartloreMotion(resolveReducedMotion(motionPreference, animatorScale))
        }

    CompositionLocalProvider(
        LocalPartloreColors provides colors,
        LocalPartloreTypography provides DefaultTypography,
        LocalPartloreMotion provides motion,
    ) {
        MaterialTheme(colorScheme = colors.toMaterialColorScheme(), content = content)
    }
}

/** The only way screens read design tokens. */
object PartloreTheme {
    val colors: PartloreColors
        @Composable @ReadOnlyComposable
        get() = LocalPartloreColors.current
    val typography: PartloreTypography
        @Composable @ReadOnlyComposable
        get() = LocalPartloreTypography.current
    val motion: PartloreMotion
        @Composable @ReadOnlyComposable
        get() = LocalPartloreMotion.current
    val spacing: PartloreSpacing get() = PartloreSpacing
    val shapes: PartloreShapes get() = PartloreShapes
    val elevation: PartloreElevation get() = PartloreElevation
}

/** Lets stock Material 3 components pick up Partlore colors. */
internal fun PartloreColors.toMaterialColorScheme(): ColorScheme =
    (if (isDark) darkColorScheme() else lightColorScheme()).copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = primary,
        // The navigation bar reads secondary (active label) and secondaryContainer (indicator).
        secondary = primary,
        onSecondary = onPrimary,
        secondaryContainer = primaryContainer,
        onSecondaryContainer = primary,
        // Containers: navigation bar, sheets, menus, dialogs.
        surfaceContainerLowest = bg,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surfaceRaised,
        surfaceContainerHighest = surfaceRaised,
        surfaceBright = surfaceRaised,
        surfaceDim = surfaceSunken,
        outlineVariant = outline,
        // Elevation comes from shadows and raised surfaces, not from tinting.
        surfaceTint = Color.Transparent,
        // Snackbars.
        inverseSurface = textPrimary,
        inverseOnSurface = bg,
        inversePrimary = if (isDark) LightColors.primary else DarkColors.primary,
        onError = if (isDark) bg else surface,
        background = bg,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceSunken,
        onSurfaceVariant = textSecondary,
        outline = outline,
        error = danger,
        errorContainer = dangerContainer,
        onErrorContainer = textPrimary,
    )
