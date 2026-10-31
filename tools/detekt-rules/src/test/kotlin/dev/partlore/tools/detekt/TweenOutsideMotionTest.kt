package dev.partlore.tools.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.Assert.assertEquals
import org.junit.Test

class TweenOutsideMotionTest {
    private val rule = TweenOutsideMotion(Config.empty)

    @Test
    fun aTweenIsFlagged() {
        val findings =
            rule.lint(
                """
                import androidx.compose.animation.core.tween
                val a = tween<Float>(300)
                val b = tween<Float>(durationMillis = 200, delayMillis = 50)
                val c = androidx.compose.animation.core.tween<Float>()
                """.trimIndent(),
            )
        assertEquals(List(3) { "TweenOutsideMotion" }, findings.map { it.id })
    }

    @Test
    fun motionTokensAreAllowed() {
        val findings =
            rule.lint(
                """
                val a = motion.fade<Float>()
                val b = motion.spring<Float>(SpringToken.Snappy)
                val c = between(1, 2)
                """.trimIndent(),
            )
        assertEquals(emptyList<String>(), findings.map { it.id })
    }
}
