package dev.partlore.tools.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class RawColorTest {
    private val rule = RawColor(Config.empty)

    @Test
    fun aHexColourIsFlagged() {
        val findings =
            rule.lint(
                """
                import androidx.compose.ui.graphics.Color
                val brand = Color(0xFF5B3FD9)
                val faded = Color(color = 0x805B3FD9)
                """.trimIndent(),
            )
        assertEquals(listOf("RawColor", "RawColor"), findings.map { it.id })
    }

    @Test
    fun tokensAndColourMathAreAllowed() {
        val findings =
            rule.lint(
                """
                val a = PartloreTheme.colors.primary
                val b = Color.Transparent
                val c = pin.fill.copy(alpha = 0.5f)
                val d = Color(red, green, blue)
                """.trimIndent(),
            )
        assertEquals(emptyList<String>(), findings.map { it.id })
    }
}
