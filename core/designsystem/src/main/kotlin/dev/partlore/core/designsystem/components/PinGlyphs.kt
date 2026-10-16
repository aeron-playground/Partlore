package dev.partlore.core.designsystem.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import dev.partlore.core.designsystem.theme.PinKind

private const val GLYPH = 0.42f
private const val STROKE = 0.08f
private const val GROUND_STEP = 0.18f
private const val WAVE = 0.5f
private const val MARK_STEM_TOP = 0.4f
private const val MARK_STEM_BOTTOM = 0.64f
private const val MARK_DOT = 0.8f
private const val MARK_STROKE = 0.12f
private const val MARK_EDGE = 0.06f

/** The kinds whose glyph is drawn; the others use text (I²C, SPI, TX, T). */
internal val DRAWN_GLYPHS = setOf(PinKind.Power, PinKind.Ground, PinKind.Adc, PinKind.Pwm, PinKind.DoNotUse)

internal fun DrawScope.drawPinGlyph(kind: PinKind, ink: Color) {
    val c = center
    val h = size.minDimension * GLYPH / 2
    val w = size.minDimension * STROKE
    fun line(from: Offset, to: Offset) = drawLine(ink, from, to, w, StrokeCap.Round)
    when (kind) {
        PinKind.Power -> {
            line(Offset(c.x - h, c.y), Offset(c.x + h, c.y))
            line(Offset(c.x, c.y - h), Offset(c.x, c.y + h))
        }

        PinKind.Ground -> {
            line(Offset(c.x, c.y - h), Offset(c.x, c.y))
            for (i in 0..2) {
                val half = h * (1 - i * GROUND_STEP * 2)
                val y = c.y + i * h * GROUND_STEP * 2
                line(Offset(c.x - half, y), Offset(c.x + half, y))
            }
        }

        PinKind.Adc -> drawPath(wave(c, h), ink, style = Stroke(w, cap = StrokeCap.Round))

        PinKind.Pwm -> drawPath(square(c, h), ink, style = Stroke(w, cap = StrokeCap.Round))

        PinKind.DoNotUse -> {
            line(Offset(c.x - h, c.y - h), Offset(c.x + h, c.y + h))
            line(Offset(c.x - h, c.y + h), Offset(c.x + h, c.y - h))
        }

        else -> Unit
    }
}

private fun wave(c: Offset, h: Float) = Path().apply {
    moveTo(c.x - h, c.y)
    cubicTo(c.x - h * WAVE, c.y - h, c.x, c.y - h, c.x, c.y)
    cubicTo(c.x, c.y + h, c.x + h * WAVE, c.y + h, c.x + h, c.y)
}

private fun square(c: Offset, h: Float) = Path().apply {
    moveTo(c.x - h, c.y + h * WAVE)
    lineTo(c.x - h * WAVE, c.y + h * WAVE)
    lineTo(c.x - h * WAVE, c.y - h * WAVE)
    lineTo(c.x + h * WAVE, c.y - h * WAVE)
    lineTo(c.x + h * WAVE, c.y + h * WAVE)
    lineTo(c.x + h, c.y + h * WAVE)
}

/** A strapping pin's ⚠: a triangle in [fill] with a "!" in [ink]. The ink edge keeps it visible on light screens. */
internal fun DrawScope.drawWarningMark(fill: Color, ink: Color) {
    val w = size.width
    val h = size.height
    val triangle =
        Path().apply {
            moveTo(w / 2, 0f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
    drawPath(triangle, fill)
    drawPath(triangle, ink, style = Stroke(size.minDimension * MARK_EDGE))
    val stroke = size.minDimension * MARK_STROKE
    drawLine(ink, Offset(w / 2, h * MARK_STEM_TOP), Offset(w / 2, h * MARK_STEM_BOTTOM), stroke, StrokeCap.Round)
    drawCircle(ink, stroke / 2, Offset(w / 2, h * MARK_DOT))
}
