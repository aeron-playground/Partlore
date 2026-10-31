package dev.partlore.tools.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class DpLiteralTest {
    private val rule = DpLiteral(Config.empty)

    @Test
    fun aNumberInDpIsFlagged() {
        val findings =
            rule.lint(
                """
                import androidx.compose.ui.unit.dp
                val a = 16.dp
                val b = 0.5.dp
                val c = (-4).dp
                val d = -8.dp
                """.trimIndent(),
            )
        assertEquals(List(4) { "DpLiteral" }, findings.map { it.id })
    }

    @Test
    fun tokensAndWorkedOutSizesAreAllowed() {
        val findings =
            rule.lint(
                """
                val a = PartloreTheme.spacing.md
                val b = PartloreLayout.artSmall * 2
                val c = size.dp
                val d = (width / columns).dp
                val e = MIN_TOUCH.dp
                """.trimIndent(),
            )
        assertEquals(emptyList<String>(), findings.map { it.id })
    }
}
