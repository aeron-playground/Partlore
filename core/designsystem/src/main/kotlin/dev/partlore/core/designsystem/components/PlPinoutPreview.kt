package dev.partlore.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import dev.partlore.core.model.PlacedBoard

/** The whole board scaled down to fit, for the part page. Not interactive. */
@Composable
fun PlPinoutPreview(board: PlacedBoard, pins: Map<String, PlBoardPin>, modifier: Modifier = Modifier) {
    Layout(content = { PlPinoutBoard(board, pins) }, modifier = modifier) { measurables, constraints ->
        val p = measurables.single().measure(Constraints())
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else p.width
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else p.height
        val fit = minOf(1f, width / p.width.toFloat(), height / p.height.toFloat())
        layout(width, height) {
            p.placeWithLayer((width - p.width) / 2, (height - p.height) / 2) {
                scaleX = fit
                scaleY = fit
            }
        }
    }
}
