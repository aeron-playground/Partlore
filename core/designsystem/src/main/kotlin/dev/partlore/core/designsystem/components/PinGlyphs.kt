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
