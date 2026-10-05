package dev.partlore.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import dev.partlore.core.designsystem.theme.PartloreLayout

/** Small drawn symbols that go next to words, never instead of them. */
enum class PlGlyph { Check, Half, Alert, Dot }

private const val STROKE = 0.16f
private const val CHECK_START_X = 0.18f
private const val CHECK_START_Y = 0.55f
private const val CHECK_MID_X = 0.42f
private const val CHECK_MID_Y = 0.78f
private const val CHECK_END_X = 0.84f
private const val CHECK_END_Y = 0.24f
private const val HALF_START = -90f
private const val HALF_SWEEP = 180f
private const val ALERT_TOP = 0.24f
private const val ALERT_BOTTOM = 0.58f
private const val ALERT_DOT = 0.76f
private const val DOT = 0.25f

/** Decorative: the words next to it carry the meaning. */
@Composable
fun PlGlyphIcon(glyph: PlGlyph, color: Color, modifier: Modifier = Modifier, cutout: Color = Color.Transparent) {
    Canvas(modifier.size(PartloreLayout.glyph)) {
        val w = size.width
        val h = size.height
        val stroke = size.minDimension * STROKE
        when (glyph) {
            PlGlyph.Check -> {
                drawLine(
                    color,
                    Offset(w * CHECK_START_X, h * CHECK_START_Y),
                    Offset(w * CHECK_MID_X, h * CHECK_MID_Y),
                    stroke,
                    StrokeCap.Round,
                )
                drawLine(
                    color,
                    Offset(w * CHECK_MID_X, h * CHECK_MID_Y),
                    Offset(w * CHECK_END_X, h * CHECK_END_Y),
                    stroke,
                    StrokeCap.Round,
                )
            }

            PlGlyph.Half -> {
                drawCircle(color, radius = size.minDimension / 2 - stroke / 2, style = Stroke(stroke))
                drawArc(color, HALF_START, HALF_SWEEP, useCenter = true)
            }

            PlGlyph.Alert -> {
                drawCircle(color)
                drawLine(cutout, Offset(w / 2, h * ALERT_TOP), Offset(w / 2, h * ALERT_BOTTOM), stroke, StrokeCap.Round)
                drawCircle(cutout, radius = stroke / 2, center = Offset(w / 2, h * ALERT_DOT))
            }

            PlGlyph.Dot -> drawCircle(color, radius = size.minDimension * DOT)
        }
    }
}
