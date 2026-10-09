package dev.partlore.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Stock Material 3 components (navigation bar, sheets, dividers, snackbars) must use Partlore colors. */
class MaterialColorSchemeTest {
    @Test
    fun lightRolesUsePartloreColors() = assertMapped(LightColors, inversePrimary = DarkColors.primary)

    @Test
    fun darkRolesUsePartloreColors() = assertMapped(DarkColors, inversePrimary = LightColors.primary)

    @Test
    fun benchRolesUsePartloreColors() = assertMapped(BenchColors, inversePrimary = LightColors.primary)

    // Snackbar actions sit on the light red inverse surface, so they use the dark red container.
    @Test
    fun redLightRolesUsePartloreColors() =
        assertMapped(RedLightColors, inversePrimary = RedLightColors.primaryContainer, m = RedLightMaterialColors)

    // Red light keeps night vision: no stock component may show another hue.
    @Test
    fun redLightRolesAreAllRed() {
        val m = RedLightMaterialColors
        listOf(
            "primary" to m.primary, "onPrimary" to m.onPrimary, "primaryContainer" to m.primaryContainer,
            "onPrimaryContainer" to m.onPrimaryContainer, "inversePrimary" to m.inversePrimary,
            "secondary" to m.secondary, "onSecondary" to m.onSecondary, "secondaryContainer" to m.secondaryContainer,
            "onSecondaryContainer" to m.onSecondaryContainer, "tertiary" to m.tertiary, "onTertiary" to m.onTertiary,
            "tertiaryContainer" to m.tertiaryContainer, "onTertiaryContainer" to m.onTertiaryContainer,
            "background" to m.background, "onBackground" to m.onBackground, "surface" to m.surface,
            "onSurface" to m.onSurface, "surfaceVariant" to m.surfaceVariant, "onSurfaceVariant" to m.onSurfaceVariant,
            "inverseSurface" to m.inverseSurface, "inverseOnSurface" to m.inverseOnSurface, "error" to m.error,
            "onError" to m.onError, "errorContainer" to m.errorContainer, "onErrorContainer" to m.onErrorContainer,
            "outline" to m.outline, "outlineVariant" to m.outlineVariant, "scrim" to m.scrim,
            "surfaceBright" to m.surfaceBright, "surfaceDim" to m.surfaceDim, "surfaceContainer" to m.surfaceContainer,
            "surfaceContainerHigh" to m.surfaceContainerHigh, "surfaceContainerHighest" to m.surfaceContainerHighest,
            "surfaceContainerLow" to m.surfaceContainerLow, "surfaceContainerLowest" to m.surfaceContainerLowest,
        ).forEach { (name, color) ->
            assertTrue("red light $name is $color", color.red >= color.green && color.red >= color.blue)
        }
    }

    private fun assertMapped(c: PartloreColors, inversePrimary: Color, m: ColorScheme = c.toMaterialColorScheme()) {
        // Navigation bar: container + active label
        assertEquals("secondary", c.primary, m.secondary)
        assertEquals("onSecondary", c.onPrimary, m.onSecondary)
        assertEquals("surfaceContainer", c.surface, m.surfaceContainer)
        // Sheets, menus, dialogs
        assertEquals("surfaceContainerLowest", c.bg, m.surfaceContainerLowest)
        assertEquals("surfaceContainerLow", c.surface, m.surfaceContainerLow)
        assertEquals("surfaceContainerHigh", c.surfaceRaised, m.surfaceContainerHigh)
        assertEquals("surfaceContainerHighest", c.surfaceRaised, m.surfaceContainerHighest)
        assertEquals("surfaceBright", c.surfaceRaised, m.surfaceBright)
        assertEquals("surfaceDim", c.surfaceSunken, m.surfaceDim)
        // Dividers
        assertEquals("outlineVariant", c.outline, m.outlineVariant)
        // No tonal tint: elevation comes from shadows and raised surfaces
        assertEquals("surfaceTint", Color.Transparent, m.surfaceTint)
        // Snackbars
        assertEquals("inverseSurface", c.textPrimary, m.inverseSurface)
        assertEquals("inverseOnSurface", c.bg, m.inverseOnSurface)
        assertEquals("inversePrimary", inversePrimary, m.inversePrimary)
        // Error buttons
        assertEquals("error", c.danger, m.error)

        assertReadable("onError on error", m.onError, m.error)
        assertReadable("inverseOnSurface on inverseSurface", m.inverseOnSurface, m.inverseSurface)
        assertReadable("inversePrimary on inverseSurface", m.inversePrimary, m.inverseSurface)
        assertReadable("onSecondary on secondary", m.onSecondary, m.secondary)
    }

    private fun assertReadable(label: String, fg: Color, bg: Color) {
        val ratio = contrastRatio(fg, bg)
        assertTrue("$label is ${"%.2f".format(ratio)}:1, needs 4.5:1", ratio >= 4.5f)
    }
}
