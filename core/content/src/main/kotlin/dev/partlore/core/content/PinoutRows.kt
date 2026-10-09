package dev.partlore.core.content

import dev.partlore.core.model.BoardEdge
import dev.partlore.core.model.Cite
import dev.partlore.core.model.HeaderRun
import dev.partlore.core.model.PinDirection
import dev.partlore.core.model.PinFunction
import dev.partlore.core.model.PinGotcha
import dev.partlore.core.model.PinHeader
import dev.partlore.core.model.PinInfo
import dev.partlore.core.model.PinSafety
import dev.partlore.core.model.RunAxis
import dev.partlore.core.model.Side
import dev.partlore.core.model.Strapping

internal data class HeaderRow(
    val id: String,
    val label: String,
    val type: String,
    val rows: Int,
    val pinsPerRow: Int,
    val pitchMm: Double,
    val cite: Cite,
) {
    /** One unknown run value and the whole header stays off the board: it is never drawn in a guessed order. */
    fun toHeader(runs: List<RunRow>): PinHeader {
        val read = runs.map { it.toRun() }
        val known = if (null in read) emptyList() else read.filterNotNull()
        return PinHeader(id, label, type, rows, pinsPerRow, pitchMm, known, cite)
    }

    companion object {
        fun read(c: Columns): HeaderRow =
            HeaderRow(c.text(), c.text(), c.text(), c.int(), c.int(), c.doubleOrNull() ?: 0.0, c.cite())
    }
}

internal data class RunRow(
    val edge: String,
    val run: String?,
    val pins: Int,
    val first: String,
    val row1: String?,
    val order: Int?,
    val cite: Cite,
) {
    /** Null when a value is unknown to this app (a newer pack). */
    fun toRun(): HeaderRun? {
        val side = BoardEdge.fromPack(edge)
        val start = Side.fromPack(first)
        val axis =
            when {
                side == BoardEdge.Left || side == BoardEdge.Right -> RunAxis.Vertical
                side == BoardEdge.Top || side == BoardEdge.Bottom -> RunAxis.Horizontal
                run == "vertical" -> RunAxis.Vertical
                run == "horizontal" -> RunAxis.Horizontal
                else -> null
            }
        val firstRow = Side.fromPack(row1)
        if (row1 != null && firstRow == null) return null
        return if (side != null && start != null && axis != null) {
            HeaderRun(side, axis, pins, start, firstRow, order, cite)
        } else {
            null
        }
    }

    companion object {
        fun read(c: Columns): RunRow =
            RunRow(c.text(), c.textOrNull(), c.int(), c.text(), c.textOrNull(), c.intOrNull(), c.cite())
    }
}

internal data class PinRow(
    val id: String,
    val header: String,
    val row: Int,
    val index: Int,
    val chip: String?,
    val module: String?,
    val board: String?,
    val arduino: String?,
    val direction: String,
    val voltage: Double?,
    val fiveV: Int?,
    val safe: String,
    val strappingRole: String?,
    val strappingMustBe: String?,
    val strappingAt: String?,
    val note: String?,
) {
    fun toInfo(
        functions: Map<String, List<PinFunction>>,
        cites: Map<String, List<Cite>>,
        aliases: Map<String, List<String>>,
        gotchas: Map<String, List<PinGotcha>>,
    ): PinInfo {
        // The pack stores every name as an alias too (for search); the sheet lists only the other names.
        val names = setOf(chip, module, board, arduino)
        return PinInfo(
            id = id,
            headerId = header,
            row = row,
            index = index,
            chipName = chip,
            modulePad = module,
            boardLabel = board,
            arduino = arduino,
            aliases = aliases[id].orEmpty().filter { it !in names },
            direction = PinDirection.fromPack(direction),
            voltage = voltage,
            fiveVoltTolerant = fiveV?.let { it == 1 },
            functions = functions[id].orEmpty(),
            safe = PinSafety.fromPack(safe),
            strapping = strappingRole?.let { Strapping(it, strappingMustBe, strappingAt.orEmpty()) },
            note = note,
            cites = cites[id].orEmpty(),
            gotchas = gotchas[id].orEmpty(),
        )
    }

    companion object {
        fun read(c: Columns): PinRow = PinRow(
            id = c.text(),
            header = c.text(),
            row = c.int(),
            index = c.int(),
            chip = c.textOrNull(),
            module = c.textOrNull(),
            board = c.textOrNull(),
            arduino = c.textOrNull(),
            direction = c.text(),
            voltage = c.doubleOrNull(),
            fiveV = c.intOrNull(),
            safe = c.text(),
            strappingRole = c.textOrNull(),
            strappingMustBe = c.textOrNull(),
            strappingAt = c.textOrNull(),
            note = c.textOrNull(),
        )
    }
}
