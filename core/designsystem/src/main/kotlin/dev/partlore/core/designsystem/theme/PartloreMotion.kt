package dev.partlore.core.designsystem.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable

@Immutable
data class SpringToken(val dampingRatio: Float, val stiffness: Float) {
    fun <T> spec(): SpringSpec<T> = spring(dampingRatio = dampingRatio, stiffness = stiffness)
}

/** Most springs don't bounce. Only pop and celebrate do, on purpose. */
object PartloreSprings {
    val snap = SpringToken(dampingRatio = 1f, stiffness = 1500f) // press, toggles, chips
    val glide = SpringToken(dampingRatio = 1f, stiffness = 400f) // expand, sheets, layout
    val drift = SpringToken(dampingRatio = 0.95f, stiffness = 200f) // page transitions
    val pop = SpringToken(dampingRatio = 0.3f, stiffness = 300f) // short "boop" on icons
    val celebrate = SpringToken(dampingRatio = 0.5f, stiffness = 250f) // spark burst
}

private const val FADE_FAST_MS = 120
private const val FADE_MS = 200
private const val REDUCED_MS = 150

enum class MotionPreference { System, Reduced, Full }

/** The app setting wins; "System" follows the animator duration scale (0 = animations off). */
fun resolveReducedMotion(preference: MotionPreference, systemAnimatorScale: Float): Boolean = when (preference) {
    MotionPreference.System -> systemAnimatorScale == 0f
    MotionPreference.Reduced -> true
    MotionPreference.Full -> false
}

/**
 * Motion for the current settings. With reduced motion, springs become short fades:
 * components that move or scale things should cross-fade instead when [reduced] is true.
 */
@Immutable
data class PartloreMotion(val reduced: Boolean) {
    fun <T> spring(token: SpringToken): FiniteAnimationSpec<T> = if (reduced) tween(REDUCED_MS) else token.spec()

    fun <T> fadeFast(): FiniteAnimationSpec<T> = tween(FADE_FAST_MS)

    fun <T> fade(): FiniteAnimationSpec<T> = tween(if (reduced) REDUCED_MS else FADE_MS)
}
