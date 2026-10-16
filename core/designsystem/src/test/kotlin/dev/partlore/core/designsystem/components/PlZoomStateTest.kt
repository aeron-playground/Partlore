package dev.partlore.core.designsystem.components

import androidx.compose.ui.geometry.Offset
import dev.partlore.core.designsystem.theme.PartloreLayout
import org.junit.Assert.assertEquals
import org.junit.Test

// A board point p shows at p × scale + offset. Zooming must keep the point under the fingers where it is.
class PlZoomStateTest {
    @Test
    fun zoomingKeepsThePointUnderTheFingersStill() {
        val zoom = PlZoomState()
        zoom.transform(centroid = Offset(100f, 40f), zoom = 2f, pan = Offset.Zero)
        assertEquals(2f, zoom.scale)
        assertEquals(Offset(-100f, -40f), zoom.offset)
    }

    @Test
    fun panMovesTheBoardWithTheFingers() {
        val zoom = PlZoomState()
        zoom.transform(centroid = Offset(10f, 10f), zoom = 1f, pan = Offset(5f, -3f))
        assertEquals(Offset(5f, -3f), zoom.offset)
    }

    @Test
    fun zoomStopsAtItsLimitAndThePointStillHolds() {
        val zoom = PlZoomState()
        zoom.transform(centroid = Offset(100f, 0f), zoom = 10f, pan = Offset.Zero)
        assertEquals(PartloreLayout.PIN_MAX_ZOOM, zoom.scale)
        assertEquals(Offset(100f - 100f * PartloreLayout.PIN_MAX_ZOOM, 0f), zoom.offset)
    }

    // The viewer lays the board out top-centred; a board wider than the screen opens whole.
    @Test
    fun aWideBoardFitsTheWidthAndStaysCentred() {
        val zoom = PlZoomState()
        zoom.fitWidth(boardWidth = 800, areaWidth = 400)
        assertEquals(0.5f, zoom.scale)
        assertEquals(Offset(200f, 0f), zoom.offset)
    }

    @Test
    fun aBoardThatFitsStaysAtNormalSize() {
        val zoom = PlZoomState(initialScale = 2f)
        zoom.fitWidth(boardWidth = 300, areaWidth = 400)
        assertEquals(1f, zoom.scale)
        assertEquals(Offset.Zero, zoom.offset)
    }
}
