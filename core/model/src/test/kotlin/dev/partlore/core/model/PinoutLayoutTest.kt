package dev.partlore.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PinoutLayoutTest {
    private val cite = Cite("s1", 1, null, null)

    private fun run(
        edge: BoardEdge,
        pins: Int,
        first: Side,
        order: Int? = null,
        axis: RunAxis? = null,
        row1: Side? = null,
    ) = HeaderRun(
        edge = edge,
        axis =
        axis ?: if (edge == BoardEdge.Left || edge == BoardEdge.Right) RunAxis.Vertical else RunAxis.Horizontal,
        pins = pins,
        first = first,
        row1 = row1,
        order = order,
        cite = cite,
    )

    private fun pinout(vararg headers: PinHeader): Pinout {
        val pins =
            headers.flatMap { h ->
                (1..h.rows).flatMap { row ->
                    (1..h.pinsPerRow).map { index -> pin("${h.id}-$row-$index", h.id, row, index) }
                }
            }
        return Pinout("t/part", "Part", headers.toList(), pins, emptyList(), null, "0.1.0")
    }

    private fun header(id: String, pins: Int, runs: List<HeaderRun>, rows: Int = 1) =
        PinHeader(id, id.uppercase(), "header", rows, pins, 2.54, runs, cite)

    private fun pin(id: String, header: String, row: Int, index: Int) = PinInfo(
        id, header, row, index, null, null, id, null, emptyList(), PinDirection.Io, null, null, emptyList(),
        PinSafety.Ok, null, null, emptyList(), emptyList(),
    )

    private fun PlacedBoard.at(id: String): Pair<Int, Int> = pins.single { it.pinId == id }.let { it.x to it.y }

    private fun PlacedBoard.side(id: String): Side = pins.single { it.pinId == id }.labelSide

    // ESP32-DevKitC V4: J2 left and J3 right, both with pin 1 at the top.
    private val devKit =
        pinout(
            header("j2", 19, listOf(run(BoardEdge.Left, 19, Side.Top))),
            header("j3", 19, listOf(run(BoardEdge.Right, 19, Side.Top))),
        )

    @Test
    fun leftAndRightHeadersStartAtTheTop() {
        val board = PinoutLayout.place(devKit, Rotation.R0)
        assertEquals(0 to 1, board.at("j2-1-1"))
        assertEquals(0 to 19, board.at("j2-1-19"))
        assertEquals(board.columns - 1 to 1, board.at("j3-1-1"))
        assertEquals(Side.Left, board.side("j2-1-1"))
        assertEquals(Side.Right, board.side("j3-1-1"))
    }

    // ESP32-WROOM-32E: pads wrap around three sides; the right side counts upward.
    @Test
    fun aWrappedHeaderFollowsItsRunsInPinOrder() {
        val pads =
            pinout(
                header(
                    "pads",
                    38,
                    listOf(
                        run(BoardEdge.Left, 14, Side.Top),
                        run(BoardEdge.Bottom, 10, Side.Left),
                        run(BoardEdge.Right, 14, Side.Bottom),
                    ),
                ),
            )
        val board = PinoutLayout.place(pads, Rotation.R0)
        assertEquals(0 to 1, board.at("pads-1-1"))
        assertEquals(0 to 14, board.at("pads-1-14"))
        assertEquals(1 to board.rows - 1, board.at("pads-1-15"))
        assertEquals(10 to board.rows - 1, board.at("pads-1-24"))
        assertEquals(board.columns - 1 to 14, board.at("pads-1-25"))
        assertEquals(board.columns - 1 to 1, board.at("pads-1-38"))
    }

    @Test
    fun runsSharingAnEdgeFollowTheirOrderWithAGap() {
        val uno =
            pinout(
                header("ioh", 10, listOf(run(BoardEdge.Top, 10, Side.Right, order = 1))),
                header("iol", 8, listOf(run(BoardEdge.Top, 8, Side.Right, order = 2))),
            )
        val board = PinoutLayout.place(uno, Rotation.R0)
        assertEquals(10 to 0, board.at("ioh-1-1"))
        assertEquals(1 to 0, board.at("ioh-1-10"))
        assertEquals(19 to 0, board.at("iol-1-1"))
        assertEquals(12 to 0, board.at("iol-1-8"))
    }

    @Test
    fun aTwoRowInsideHeaderPutsRow1OnItsSide() {
        val icsp =
            pinout(
                header("j2", 4, listOf(run(BoardEdge.Left, 4, Side.Top))),
                header(
                    "icsp",
                    3,
                    listOf(run(BoardEdge.Inside, 3, Side.Top, axis = RunAxis.Vertical, row1 = Side.Left)),
                    rows = 2,
                ),
            )
        val board = PinoutLayout.place(icsp, Rotation.R0)
        val (x1, y1) = board.at("icsp-1-1")
        assertEquals(x1 + 1 to y1, board.at("icsp-2-1"))
        assertEquals(x1 to y1 + 2, board.at("icsp-1-3"))
        assertEquals(Side.Left, board.side("icsp-1-1"))
        assertEquals(Side.Right, board.side("icsp-2-1"))
    }

    @Test
    fun aTwoRowRightEdgeHeaderStaysOnTheBoard() {
        val board = PinoutLayout.place(
            pinout(header("j9", 3, listOf(run(BoardEdge.Right, 3, Side.Top, row1 = Side.Right)), rows = 2)),
            Rotation.R0,
        )
        assertEquals(board.columns - 1 to 1, board.at("j9-1-1"))
        assertEquals(board.columns - 2 to 1, board.at("j9-2-1"))
        assertEquals(Side.Right, board.side("j9-1-1"))
        assertEquals(Side.Left, board.side("j9-2-1"))
    }

    // Row 1 may sit on the inner line (a Raspberry Pi header has its odd pins inside): the data decides, not the edge.
    @Test
    fun row1OnTheInnerSidePutsRow1OnTheInnerLine() {
        val inner = mapOf(
            BoardEdge.Left to Side.Right,
            BoardEdge.Top to Side.Bottom,
            BoardEdge.Right to Side.Left,
            BoardEdge.Bottom to Side.Top,
        )
        inner.forEach { (edge, side) ->
            val first = if (edge == BoardEdge.Left || edge == BoardEdge.Right) Side.Top else Side.Left
            val board = PinoutLayout.place(
                pinout(header("h", 2, listOf(run(edge, 2, first, row1 = side)), rows = 2)),
                Rotation.R0,
            )
            val (x1, y1) = board.at("h-1-1")
            val (x2, y2) = board.at("h-2-1")
            val (outer, innerLine) =
                when (edge) {
                    BoardEdge.Left -> x2 to x1
                    BoardEdge.Right -> board.columns - 1 - x2 to board.columns - 1 - x1
                    BoardEdge.Top -> y2 to y1
                    else -> board.rows - 1 - y2 to board.rows - 1 - y1
                }
            assertEquals("$edge: row 2 on the outer line", 0, outer)
            assertEquals("$edge: row 1 on the inner line", 1, innerLine)
            assertEquals("$edge: row 1's label faces inward", side, board.side("h-1-1"))
        }
    }

    @Test
    fun aHeaderWithMoreThanTwoRowsIsUnplaced() {
        val board = PinoutLayout.place(
            pinout(header("h", 2, listOf(run(BoardEdge.Left, 2, Side.Top, row1 = Side.Left)), rows = 3)),
            Rotation.R0,
        )
        assertEquals(listOf("h"), board.unplacedHeaders)
        assertEquals(0, board.pins.size)
    }

    @Test
    fun aTwoRowHeaderWithoutRow1IsUnplaced() {
        val board = PinoutLayout.place(
            pinout(header("h", 2, listOf(run(BoardEdge.Left, 2, Side.Top)), rows = 2)),
            Rotation.R0,
        )
        assertEquals(listOf("h"), board.unplacedHeaders)
    }

    // A newer pack could send ends that don't fit the run's direction: never guess which way it runs.
    @Test
    fun endsThatDoNotFitTheRunAreUnplaced() {
        val wrongFirst = header("a", 2, listOf(run(BoardEdge.Left, 2, Side.Left)))
        val wrongRow1 = header("b", 2, listOf(run(BoardEdge.Right, 2, Side.Top, row1 = Side.Top)), rows = 2)
        val board = PinoutLayout.place(pinout(wrongFirst, wrongRow1), Rotation.R0)
        assertEquals(listOf("a", "b"), board.unplacedHeaders)
    }

    // TalkBack reads the board in this order: header by header, along each header from pin 1.
    @Test
    fun pinsComeHeaderByHeaderInPinOrder() {
        val board = PinoutLayout.place(devKit, Rotation.R0)
        val expected = (1..19).map { "j2-1-$it" } + (1..19).map { "j3-1-$it" }
        assertEquals(expected, board.pins.map { it.pinId })
        val twoRow = PinoutLayout.place(
            pinout(header("h", 2, listOf(run(BoardEdge.Left, 2, Side.Top, row1 = Side.Left)), rows = 2)),
            Rotation.R90,
        )
        assertEquals(listOf("h-1-1", "h-2-1", "h-1-2", "h-2-2"), twoRow.pins.map { it.pinId })
    }

    @Test
    fun turningKeepsNeighboursTogetherAndTurnsLabelSides() {
        val board = PinoutLayout.place(devKit, Rotation.R90)
        val (ax, ay) = board.at("j2-1-1")
        val (bx, by) = board.at("j2-1-2")
        assertEquals(1, kotlin.math.abs(ax - bx) + kotlin.math.abs(ay - by))
        assertEquals(Side.Top, board.side("j2-1-1"))
        assertEquals(0, ay)
    }

    @Test
    fun headersWithoutUsableRunsAreUnplaced() {
        val broken = pinout(header("j1", 4, listOf(run(BoardEdge.Left, 3, Side.Top))))
        val board = PinoutLayout.place(broken, Rotation.R0)
        assertEquals(listOf("j1"), board.unplacedHeaders)
        assertEquals(emptyList<PlacedPin>(), board.pins)
    }

    @Test
    fun boardsWithLongHorizontalHeadersOpenTurned() {
        val uno = pinout(header("ioh", 10, listOf(run(BoardEdge.Top, 10, Side.Right, order = 1))))
        assertEquals(Rotation.R90, PinoutLayout.defaultRotation(uno))
        assertEquals(Rotation.R0, PinoutLayout.defaultRotation(devKit))
    }
}
