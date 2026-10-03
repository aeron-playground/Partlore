package dev.partlore.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastTest {
    @Test
    fun blackOnWhiteIsTwentyOne() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
    }

    @Test
    fun lightColorsMeetTheirTargets() = assertScheme("light", LightColors)

    @Test
    fun darkColorsMeetTheirTargets() = assertScheme("dark", DarkColors)

    @Test
    fun benchColorsMeetTheirTargets() = assertScheme("bench", BenchColors)

    private fun assertScheme(name: String, c: PartloreColors) {
        val backgrounds = listOf(
            "bg" to c.bg,
            "surface" to c.surface,
            "surfaceRaised" to c.surfaceRaised,
            "surfaceSunken" to c.surfaceSunken,
        )
        val texts =
            listOf(
                Triple("textPrimary", c.textPrimary, AAA),
                Triple("textSecondary", c.textSecondary, AA),
                Triple("primary", c.primary, AA),
                Triple("accent", c.accent, AA),
                Triple("success", c.success, AA),
                Triple("warning", c.warning, AA),
                Triple("danger", c.danger, AA),
                Triple("info", c.info, AA),
            )
        for ((fgName, fg, minimum) in texts) {
            for ((bgName, bg) in backgrounds) assertPair("$name $fgName on $bgName", fg, bg, minimum)
        }
        assertPair("$name onPrimary on primary", c.onPrimary, c.primary, AA)
        assertPair("$name primary on primaryContainer", c.primary, c.primaryContainer, AA)
        assertPair("$name textPrimary on primaryContainer", c.textPrimary, c.primaryContainer, AA)
        assertPair("$name warning on warningContainer", c.warning, c.warningContainer, AA)
        assertPair("$name danger on dangerContainer", c.danger, c.dangerContainer, AA)
        assertPair("$name textPrimary on warningContainer", c.textPrimary, c.warningContainer, AA)
        assertPair("$name textPrimary on dangerContainer", c.textPrimary, c.dangerContainer, AA)
    }

    private fun assertPair(label: String, fg: Color, bg: Color, minimum: Float) {
        val ratio = contrastRatio(fg, bg)
        assertTrue("$label is ${"%.2f".format(ratio)}:1, needs $minimum:1", ratio >= minimum)
    }

    private companion object {
        const val AA = 4.5f
        const val AAA = 7f
    }
}
