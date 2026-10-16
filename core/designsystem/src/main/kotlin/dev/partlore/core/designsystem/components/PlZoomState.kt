package dev.partlore.core.designsystem.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreSprings
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.Rotation
import kotlinx.coroutines.launch

/**
 * Zoom and pan of the pinout board. A board point p shows at p × scale + offset (scaled from the top-left
 * corner). Below 1× pins are smaller than a finger, so taps zoom instead of picking.
 */
@Stable
class PlZoomState(initialScale: Float = 1f) {
    var scale by mutableFloatStateOf(initialScale)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set

    /** Where the last finger went down, in the board's frame; unspecified until a touch (TalkBack sends none). */
    internal var press: Offset = Offset.Unspecified

    val tapsPick: Boolean get() = scale >= 1f

    /** A pinch: zoom by [zoom] around [centroid], the point between the fingers, and move by [pan]. */
    internal fun transform(centroid: Offset, zoom: Float, pan: Offset) {
        zoomAround(centroid, scale * zoom)
        offset += pan
    }

    /**
     * For a board laid out top-centred in a zoom area [areaWidth] wide: a wider board is shown whole, [margin]
     * from each side (down to the smallest zoom; a tap then zooms to 1× around the pin), else at 1×.
     */
    fun fitWidth(boardWidth: Int, areaWidth: Int, margin: Int = 0) {
        val fit = ((areaWidth - 2f * margin) / boardWidth).coerceIn(PartloreLayout.PIN_MIN_ZOOM, 1f)
        scale = fit
        // The area scales from its top-left corner; moving right by half of what it lost keeps the board centred.
        offset = Offset(areaWidth * (1 - fit) / 2, 0f)
    }

    internal suspend fun zoomTo(target: Float, around: Offset, spec: AnimationSpec<Float>) {
        animate(scale, target, animationSpec = spec) { value, _ -> zoomAround(around, value) }
    }

    // Keeps the board point under [point] where it is: p × scale + offset stays equal to point.
    private fun zoomAround(point: Offset, target: Float) {
        val next = target.coerceIn(PartloreLayout.PIN_MIN_ZOOM, PartloreLayout.PIN_MAX_ZOOM)
        offset = point - (point - offset) * (next / scale)
        scale = next
    }
}

@Composable
fun rememberPlZoomState(initialScale: Float = 1f): PlZoomState = remember { PlZoomState(initialScale) }

/** Pinch, drag and double-tap zoom, each around the fingers; reduced motion makes the double tap short. */
@Composable
internal fun Modifier.zoomable(state: PlZoomState): Modifier {
    val scope = rememberCoroutineScope()
    val motion = PartloreTheme.motion
    val transform = rememberTransformableState { centroid, zoom, pan, _ -> state.transform(centroid, zoom, pan) }
    return this
        .transformable(transform)
        .pointerInput(state) {
            // Only watches (Initial pass, nothing consumed): the pins below still get their taps.
            awaitEachGesture {
                state.press = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial).position
            }
        }.pointerInput(state) {
            detectTapGestures(onDoubleTap = { at ->
                val target = if (state.scale == 1f) PartloreLayout.PIN_DOUBLE_TAP_ZOOM else 1f
                scope.launch { state.zoomTo(target, at, motion.spring(PartloreSprings.glide)) }
            })
        }.graphicsLayer {
            transformOrigin = TransformOrigin(0f, 0f)
            scaleX = state.scale
            scaleY = state.scale
            translationX = state.offset.x
            translationY = state.offset.y
        }
}

/**
 * The area the board is zoomed in. Gestures cover all of it, so a drag or pinch beside the board works too;
 * the area is scaled with the board top-centred inside. A board wider than the area is fitted when first
 * shown, and every board after a rotate; width changes from labels, Bench mode or a side panel keep the zoom.
 */
@Composable
internal fun ZoomArea(
    zoom: PlZoomState,
    rotation: Rotation,
    modifier: Modifier = Modifier,
    board: @Composable (Modifier) -> Unit,
) {
    var area by remember { mutableIntStateOf(0) }
    var measured by remember { mutableStateOf<Pair<Rotation, Int>?>(null) }
    var fittedFor by remember { mutableStateOf<Rotation?>(null) }
    val margin = with(LocalDensity.current) { PartloreTheme.spacing.space16.roundToPx() }
    LaunchedEffect(area, measured) {
        val (shown, width) = measured ?: return@LaunchedEffect
        if (area > 0 && shown != fittedFor) {
            if (fittedFor != null || width > area) zoom.fitWidth(width, area, margin)
            fittedFor = shown
        }
    }
    Box(modifier.onSizeChanged { area = it.width }.zoomable(zoom), contentAlignment = Alignment.TopCenter) {
        board(
            Modifier
                .wrapContentSize(Alignment.TopCenter, unbounded = true)
                .onSizeChanged { measured = rotation to it.width }
                // Hidden for the one frame before it is fitted, so a cut-off board never flashes.
                .graphicsLayer { alpha = if (fittedFor != null) 1f else 0f },
        )
    }
}
