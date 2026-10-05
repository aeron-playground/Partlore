package dev.partlore.app.shell

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import dev.partlore.core.designsystem.theme.PartloreMotion
import org.junit.Assert.assertEquals
import org.junit.Test

class PageTransitionTest {
    private val full = PartloreMotion(reduced = false)
    private val reduced = PartloreMotion(reduced = true)

    @Test
    fun goingBackKeepsTheLeavingPageOnTop() {
        assertEquals(-1f, pageTransition(full, forward = false).targetContentZIndex)
        assertEquals(0f, pageTransition(full, forward = true).targetContentZIndex)
    }

    @Test
    fun reducedMotionOnlyFades() {
        val transition = pageTransition(reduced, forward = true)
        assertEquals(fadeIn(reduced.fade()), transition.targetContentEnter)
        assertEquals(fadeOut(reduced.fade()), transition.initialContentExit)
    }
}
