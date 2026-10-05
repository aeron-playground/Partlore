package dev.partlore.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import dev.partlore.core.designsystem.theme.PartloreTheme

private const val PIN_STEPS = 6f
private const val PIN_RADIUS = 0.22f

/** Stand-in art until real drawings exist: a board with two pin rows, other parts with pads along one edge. */
@Composable
fun PlPartArt(kind: String, modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    val board = kind == "board"
    val body = if (board) colors.primary else colors.textSecondary
    val pin = colors.onPrimary
    Canvas(modifier.clip(PartloreTheme.shapes.sm).background(body)) {
        val step = size.minDimension / PIN_STEPS
        val radius = step * PIN_RADIUS
        if (board) {
            pinColumn(step, step, radius, pin)
            pinColumn(size.width - step, step, radius, pin)
        } else {
            var x = step
            while (x < size.width - step / 2) {
                drawCircle(pin, radius, Offset(x, size.height - step))
                x += step
            }
        }
    }
}

private fun DrawScope.pinColumn(x: Float, step: Float, radius: Float, color: Color) {
    var y = step
    while (y < size.height - step / 2) {
        drawCircle(color, radius, Offset(x, y))
        y += step
    }
}
