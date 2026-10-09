package dev.partlore.core.model

enum class Rotation {
    R0,
    R90,
    R180,
    R270,
    ;

    /** A quarter turn clockwise. */
    fun next(): Rotation = entries[(ordinal + 1) % entries.size]
}

/** A pin's cell in the grid and the side its label goes on. */
data class PlacedPin(val pinId: String, val x: Int, val y: Int, val labelSide: Side)

data class PlacedBoard(
    val columns: Int,
    val rows: Int,
    val pins: List<PlacedPin>,
    val unplacedHeaders: List<String>,
    val rotation: Rotation,
)

/**
 * Places pins around the board edges, in cells. Column 0 and the last column are the left and right
 * edges, row 0 and the last row the top and bottom edges; runs on one edge are one empty cell apart.
 */
object PinoutLayout {
    private const val MIN_INNER = 2

    fun place(pinout: Pinout, rotation: Rotation): PlacedBoard {
        val (usable, unplaced) = pinout.headers.partition { h ->
            h.runs.isNotEmpty() &&
                h.runs.sumOf { it.pins } == h.pinsPerRow
        }
        val runs = usable.flatMap { h -> h.runs.mapIndexed { i, r -> RunRef(h, r, h.runs.take(i).sumOf { it.pins }) } }
        val grid = Grid.of(runs)
        val placed =
            runs.flatMap { ref ->
                val pins = pinout.pins.filter { it.headerId == ref.header.id && it.index - 1 in ref.range }
                pins.map { grid.place(ref, it) }
            }
        return turn(PlacedBoard(grid.columns, grid.rows, placed, unplaced.map { it.id }, Rotation.R0), rotation)
    }

    /** Turned 90° when more pins sit on the top and bottom edges, so the long headers run down a phone. */
    fun defaultRotation(pinout: Pinout): Rotation {
        val runs = pinout.headers.flatMap { it.runs }
        val horizontal = runs.filter { it.edge == BoardEdge.Top || it.edge == BoardEdge.Bottom }.sumOf { it.pins }
        val vertical = runs.filter { it.edge == BoardEdge.Left || it.edge == BoardEdge.Right }.sumOf { it.pins }
        return if (horizontal > vertical) Rotation.R90 else Rotation.R0
    }

    private fun turn(board: PlacedBoard, rotation: Rotation): PlacedBoard {
        var turned = board
        repeat(rotation.ordinal) { turned = quarterTurn(turned) }
        return turned.copy(rotation = rotation)
    }

    // Clockwise: the left edge becomes the top edge.
    private fun quarterTurn(b: PlacedBoard) = b.copy(
        columns = b.rows,
        rows = b.columns,
        pins = b.pins.map { it.copy(x = b.rows - 1 - it.y, y = it.x, labelSide = clockwise(it.labelSide)) },
    )

    private fun clockwise(side: Side): Side = when (side) {
        Side.Left -> Side.Top
        Side.Top -> Side.Right
        Side.Right -> Side.Bottom
        Side.Bottom -> Side.Left
    }

    internal class RunRef(val header: PinHeader, val run: HeaderRun, start: Int) {
        val range = start until start + run.pins
    }

    internal class Grid(val columns: Int, val rows: Int, private val starts: Map<RunRef, Pair<Int, Int>>) {
        fun place(ref: RunRef, pin: PinInfo): PlacedPin {
            val run = ref.run
            val k = pin.index - 1 - ref.range.first
            val along = if (run.first == Side.Top || run.first == Side.Left) k else run.pins - 1 - k
            val (sx, sy) = starts.getValue(ref)
            // Line 0 is row 1's line: the outer line of an edge run, the row1 side of an inside run.
            val line = if (pin.row == 1) row1Line(run) else 1 - row1Line(run)
            val step = line * inward(run.edge)
            val vertical = run.axis == RunAxis.Vertical
            val x = if (vertical) sx + step else sx + along
            val y = if (vertical) sy + along else sy + step
            return PlacedPin(pin.id, x, y, labelSide(run, line))
        }

        private fun row1Line(run: HeaderRun): Int =
            if (run.edge == BoardEdge.Inside && (run.row1 == Side.Right || run.row1 == Side.Bottom)) 1 else 0

        // The direction of the second line: into the board for edge runs, right/down for inside runs.
        private fun inward(edge: BoardEdge): Int = if (edge == BoardEdge.Right || edge == BoardEdge.Bottom) -1 else 1

        private fun labelSide(run: HeaderRun, line: Int): Side {
            val outer =
                when (run.edge) {
                    BoardEdge.Left -> Side.Left
                    BoardEdge.Right -> Side.Right
                    BoardEdge.Top -> Side.Top
                    BoardEdge.Bottom -> Side.Bottom
                    BoardEdge.Inside -> if (run.axis == RunAxis.Vertical) Side.Left else Side.Top
                }
            return if (line == 1) opposite(outer) else outer
        }

        private fun opposite(side: Side): Side = when (side) {
            Side.Left -> Side.Right
            Side.Right -> Side.Left
            Side.Top -> Side.Bottom
            Side.Bottom -> Side.Top
        }

        companion object {
            fun of(runs: List<RunRef>): Grid {
                val byEdge = runs.groupBy { it.run.edge }.mapValues { (_, list) -> list.sortedBy { it.run.order ?: 0 } }
                fun length(edge: BoardEdge) = byEdge[edge].orEmpty().let { list ->
                    list.sumOf { it.run.pins } +
                        (list.size - 1).coerceAtLeast(0)
                }
                val inside = byEdge[BoardEdge.Inside].orEmpty()
                val insideW = inside.sumOf { width(it) } + (inside.size - 1).coerceAtLeast(0)
                val insideH = inside.maxOfOrNull { height(it) } ?: 0
                val innerW = maxOf(length(BoardEdge.Top), length(BoardEdge.Bottom), insideW + 2, MIN_INNER)
                val innerH = maxOf(length(BoardEdge.Left), length(BoardEdge.Right), insideH + 2, MIN_INNER)
                val starts = mutableMapOf<RunRef, Pair<Int, Int>>()
                fun line(edge: BoardEdge, fixed: (Int) -> Pair<Int, Int>) {
                    var at = 1
                    byEdge[edge].orEmpty().forEach { ref ->
                        starts[ref] = fixed(at)
                        at += ref.run.pins + 1
                    }
                }
                line(BoardEdge.Left) { 0 to it }
                line(BoardEdge.Right) { innerW + 1 to it }
                line(BoardEdge.Top) { it to 0 }
                line(BoardEdge.Bottom) { it to innerH + 1 }
                var x = 1 + (innerW - insideW) / 2
                inside.forEach { ref ->
                    starts[ref] = x to 1 + (innerH - height(ref)) / 2
                    x += width(ref) + 1
                }
                return Grid(innerW + 2, innerH + 2, starts)
            }

            private fun width(ref: RunRef) = if (ref.run.axis == RunAxis.Vertical) ref.header.rows else ref.run.pins

            private fun height(ref: RunRef) = if (ref.run.axis == RunAxis.Vertical) ref.run.pins else ref.header.rows
        }
    }
}
