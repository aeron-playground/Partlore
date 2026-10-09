package dev.partlore.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Stock components in red light. The usual mapping takes the snackbar action colour from the light theme
 * (purple) and leaves Material's own tertiary colours: both are replaced with reds.
 */
internal val RedLightMaterialColors: ColorScheme =
    RedLightColors.toMaterialColorScheme().copy(
        inversePrimary = RedLightColors.primaryContainer,
        tertiary = RedLightColors.primary,
        onTertiary = RedLightColors.onPrimary,
        tertiaryContainer = RedLightColors.primaryContainer,
        onTertiaryContainer = RedLightColors.primary,
    )

/** Red on black for dark rooms. Used by the pinout viewer only; motion and type stay as they are. */
@Composable
fun PartloreRedLight(enabled: Boolean, content: @Composable () -> Unit) {
    if (enabled) {
        CompositionLocalProvider(LocalPartloreColors provides RedLightColors) {
            MaterialTheme(colorScheme = RedLightMaterialColors, content = content)
        }
    } else {
        content()
    }
}
