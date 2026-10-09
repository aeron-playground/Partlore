package dev.partlore.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import dev.partlore.core.designsystem.R
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.PinColor
import dev.partlore.core.designsystem.theme.PinKind

private const val MAX_GROWTH = 2f
private const val HATCH_LINES = 4
private const val HATCH_ALPHA = 0.5f

/**
 * One pin: a dot in its kind's colour with its glyph (colour is never the only signal). Strapping
 * pins get a dashed ring, do-not-use pins a hatch. Decorative for TalkBack: the pin's button says it all.
 */
@Composable
fun PlPinDot(
    kind: PinKind,
    modifier: Modifier = Modifier,
    strapping: Boolean = false,
    caution: Boolean = false,
    inputOnly: Boolean = false,
    large: Boolean = false,
) {
    val colors = PartloreTheme.colors
    val pin = colors.pins.of(kind)
    val look = DotLook(
        pin,
        colors.bg,
        colors.pins.strapping.fill,
        if (kind ==
            PinKind.Pwm
        ) {
            colors.textPrimary
        } else {
            pin.ink
        },
    )
    Box(modifier.size(dotSize(large)).clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) { drawDot(kind, look, strapping) }
        glyphText(kind)?.let { Text(it, style = PartloreTheme.typography.label, color = pin.ink, maxLines = 1) }
        if (caution) Badge("!", Modifier.align(Alignment.TopEnd))
        if (inputOnly) Badge(stringResource(R.string.pl_pin_glyph_input), Modifier.align(Alignment.BottomEnd))
    }
}

private data class DotLook(val pin: PinColor, val bg: Color, val strap: Color, val glyph: Color)

private fun DrawScope.drawDot(kind: PinKind, look: DotLook, strapping: Boolean) {
    val ring = PartloreLayout.pinRing.toPx()
    val r = size.minDimension / 2 - ring
    if (kind == PinKind.Pwm) {
        drawCircle(look.bg, r)
        drawCircle(look.pin.fill, r, style = Stroke(ring))
    } else {
        drawCircle(look.pin.fill, r)
    }
    if (kind == PinKind.DoNotUse) hatch(look.pin.ink, r, ring / 2)
    if (strapping) {
        val dash = PartloreLayout.pinDash.toPx()
        val effect = PathEffect.dashPathEffect(floatArrayOf(dash, dash))
        drawCircle(look.strap, size.minDimension / 2 - ring / 2, style = Stroke(ring, pathEffect = effect))
    }
    if (kind in DRAWN_GLYPHS) drawPinGlyph(kind, look.glyph)
}

// Diagonal lines across the dot, kept inside the circle.
private fun DrawScope.hatch(ink: Color, r: Float, width: Float) {
    clipPath(Path().apply { addOval(Rect(center, r)) }) {
        val step = size.minDimension / HATCH_LINES
        for (i in -HATCH_LINES..HATCH_LINES) {
            val x = i * step
            drawLine(ink.copy(alpha = HATCH_ALPHA), Offset(x, size.height), Offset(x + size.height, 0f), width)
        }
    }
}

@Composable
private fun dotSize(large: Boolean): Dp {
    val base = if (large) PartloreLayout.pinDotLarge else PartloreLayout.pinDot
    return base * LocalDensity.current.fontScale.coerceIn(1f, MAX_GROWTH)
}

@Composable
private fun glyphText(kind: PinKind): String? = when (kind) {
    PinKind.I2c -> stringResource(R.string.pl_pin_i2c)
    PinKind.Spi -> stringResource(R.string.pl_pin_spi)
    PinKind.Uart -> stringResource(R.string.pl_pin_glyph_uart)
    PinKind.Touch -> stringResource(R.string.pl_pin_glyph_touch)
    else -> null
}

@Composable
private fun Badge(text: String, modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    Text(
        text,
        style = PartloreTheme.typography.caption,
        color = colors.textPrimary,
        maxLines = 1,
        modifier =
        modifier
            .clip(PartloreTheme.shapes.full)
            .background(colors.warningContainer)
            .padding(horizontal = PartloreTheme.spacing.space2),
    )
}
