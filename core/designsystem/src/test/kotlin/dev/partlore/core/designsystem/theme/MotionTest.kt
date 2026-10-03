package dev.partlore.core.designsystem.theme

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionTest {
    @Test
    fun systemPreferenceFollowsAnimatorScale() {
        assertTrue(resolveReducedMotion(MotionPreference.System, systemAnimatorScale = 0f))
        assertFalse(resolveReducedMotion(MotionPreference.System, systemAnimatorScale = 1f))
        assertFalse(resolveReducedMotion(MotionPreference.System, systemAnimatorScale = 0.5f))
    }

    @Test
    fun appPreferenceWinsOverSystem() {
        assertTrue(resolveReducedMotion(MotionPreference.Reduced, systemAnimatorScale = 1f))
        assertFalse(resolveReducedMotion(MotionPreference.Full, systemAnimatorScale = 0f))
    }

    @Test
    fun reducedMotionTurnsSpringsIntoShortFades() {
        val spec = PartloreMotion(reduced = true).spring<Float>(PartloreSprings.glide)
        assertEquals(150, (spec as TweenSpec).durationMillis)
    }

    @Test
    fun fullMotionUsesTheSpring() {
        val spec = PartloreMotion(reduced = false).spring<Float>(PartloreSprings.glide)
        assertEquals(1f, (spec as SpringSpec).dampingRatio)
        assertEquals(400f, spec.stiffness)
    }
}
