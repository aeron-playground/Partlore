package dev.partlore.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Stock Material 3 components (navigation bar, sheets, dividers, snackbars) must use Partlore colors. */
class MaterialColorSchemeTest {
    @Test
    fun lightRolesUsePartloreColors() = assertMapped(LightColors, other = DarkColors)

    @Test
    fun darkRolesUsePartloreColors() = assertMapped(DarkColors, other = LightColors)

    @Test
    fun benchRolesUsePartloreColors() = assertMapped(BenchColors, other = LightColors)

    private fun assertMapped(c: PartloreColors, other: PartloreColors) {
        val m = c.toMaterialColorScheme()
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
        assertEquals("inversePrimary", other.primary, m.inversePrimary)
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
