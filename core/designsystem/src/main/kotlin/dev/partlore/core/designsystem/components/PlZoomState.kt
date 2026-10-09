package dev.partlore.core.designsystem.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreSprings
import dev.partlore.core.designsystem.theme.PartloreTheme
import kotlinx.coroutines.launch

/** Zoom and pan of the pinout board. Below 1× pins are smaller than a finger, so taps zoom instead of picking. */
@Stable
class PlZoomState(initialScale: Float = 1f) {
    var scale by mutableFloatStateOf(initialScale)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set

    val tapsPick: Boolean get() = scale >= 1f

    internal fun transform(zoom: Float, pan: Offset) {
        scale = (scale * zoom).coerceIn(PartloreLayout.PIN_MIN_ZOOM, PartloreLayout.PIN_MAX_ZOOM)
        offset += pan
    }

    internal suspend fun zoomTo(target: Float, spec: AnimationSpec<Float>) {
        animate(scale, target, animationSpec = spec) { value, _ -> scale = value }
    }
}

@Composable
fun rememberPlZoomState(initialScale: Float = 1f): PlZoomState = remember { PlZoomState(initialScale) }

/** Pinch, drag and double-tap zoom; reduced motion jumps instead of animating. */
@Composable
internal fun Modifier.zoomable(state: PlZoomState): Modifier {
    val scope = rememberCoroutineScope()
    val motion = PartloreTheme.motion
    val transform = rememberTransformableState { zoom, pan, _ -> state.transform(zoom, pan) }
    return this
        .transformable(transform)
        .pointerInput(state) {
            detectTapGestures(onDoubleTap = {
                val target = if (state.scale == 1f) PartloreLayout.PIN_DOUBLE_TAP_ZOOM else 1f
                scope.launch { state.zoomTo(target, motion.spring(PartloreSprings.glide)) }
            })
        }.graphicsLayer {
            scaleX = state.scale
            scaleY = state.scale
            translationX = state.offset.x
            translationY = state.offset.y
        }
}
