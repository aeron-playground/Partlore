package dev.partlore.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/** One shadow layer. Light comes from the top-left, so x and y are positive and y = 2x. */
@Immutable
data class ShadowLayer(val x: Dp, val y: Dp, val blur: Dp, val alpha: Float)

@Immutable
data class Elevation(val level: Int, val layers: List<ShadowLayer>)

object PartloreElevation {
    val e0 = Elevation(0, emptyList())
    val e1 =
        Elevation(
            1,
            listOf(
                ShadowLayer(0.5.dp, 1.dp, 1.dp, 0.10f),
                ShadowLayer(1.dp, 2.dp, 2.dp, 0.08f),
                ShadowLayer(2.dp, 4.dp, 4.dp, 0.06f),
            ),
        )
    val e2 =
        Elevation(
            2,
            listOf(
                ShadowLayer(1.dp, 2.dp, 2.dp, 0.09f),
                ShadowLayer(2.dp, 4.dp, 4.dp, 0.07f),
                ShadowLayer(4.dp, 8.dp, 8.dp, 0.06f),
            ),
        )
    val e3 =
        Elevation(
            3,
            listOf(
                ShadowLayer(2.dp, 4.dp, 4.dp, 0.08f),
                ShadowLayer(4.dp, 8.dp, 8.dp, 0.06f),
                ShadowLayer(8.dp, 16.dp, 16.dp, 0.05f),
            ),
        )
    val e4 =
        Elevation(
            4,
            listOf(
                ShadowLayer(4.dp, 8.dp, 8.dp, 0.07f),
                ShadowLayer(8.dp, 16.dp, 16.dp, 0.05f),
                ShadowLayer(16.dp, 32.dp, 32.dp, 0.04f),
            ),
        )
    val all = listOf(e0, e1, e2, e3, e4)
}

private const val DARK_HIGHLIGHT_ALPHA = 0.06f

/**
 * Light themes: layered shadows tinted with [PartloreColors.shadowTint].
 * Dark themes: a 1 dp highlight along the top edge (shadows don't show on dark backgrounds).
 * Put this before the background modifier.
 */
fun Modifier.partloreElevation(elevation: Elevation, shape: Shape, colors: PartloreColors): Modifier = when {
    elevation.layers.isEmpty() -> this

    colors.isDark ->
        drawWithContent {
            drawContent()
            val outline = shape.createOutline(size, layoutDirection, this)
            clipPath(Path().apply { addOutline(outline) }) {
                drawRect(
                    color = Color.White.copy(alpha = DARK_HIGHLIGHT_ALPHA),
                    size = Size(size.width, PartloreStroke.hairline.toPx()),
                )
            }
        }

    else ->
        elevation.layers.fold(this) { modifier, layer ->
            modifier.dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = layer.blur,
                    color = colors.shadowTint,
                    offset = DpOffset(layer.x, layer.y),
                    alpha = layer.alpha,
                ),
            )
        }
}
