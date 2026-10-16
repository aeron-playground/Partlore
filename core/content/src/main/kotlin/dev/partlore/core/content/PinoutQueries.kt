package dev.partlore.core.content

import androidx.sqlite.SQLiteConnection
import dev.partlore.core.model.FileStatus
import dev.partlore.core.model.PinFunction
import dev.partlore.core.model.PinGotcha
import dev.partlore.core.model.PinHeader
import dev.partlore.core.model.Pinout
import dev.partlore.core.model.Severity
import dev.partlore.core.model.SourceItem

/** A part's headers and pins. Null when the part has no pins file. */
internal fun SQLiteConnection.pinout(
    partId: String,
    partName: String,
    sources: List<SourceItem>,
    status: FileStatus?,
    packVersion: String,
): Pinout? {
    val args = listOf(partId)
    val headers = headers(args)
    if (headers.isEmpty()) return null
    val functions =
        query(
            "SELECT pin_id, type, signal, is_default FROM pin_function WHERE part_id = ? ORDER BY pin_id, seq",
            args,
        ) {
            it.text() to PinFunction(it.text(), it.textOrNull(), it.int() == 1)
        }.groupBy({ it.first }, { it.second })
    val cites =
        query(
            "SELECT pin_id, source_id, page, section, ref FROM pin_cite WHERE part_id = ? ORDER BY pin_id, seq",
            args,
        ) {
            it.text() to it.cite()
        }.groupBy({ it.first }, { it.second })
    val aliases =
        query("SELECT pin_id, label FROM pin_alias WHERE part_id = ? ORDER BY pin_id, label", args) {
            it.text() to it.text()
        }.groupBy({ it.first }, { it.second })
    val gotchas = pinGotchas(args)
    val pins =
        query(
            "SELECT id, header_id, row, idx, chip_name, module_pad, board_label, arduino, direction, voltage, " +
                "five_v_tolerant, safe, strapping_role, strapping_must_be, strapping_at, note " +
                "FROM pin WHERE part_id = ?",
            args,
        ) { PinRow.read(it) }.map { it.toInfo(functions, cites, aliases, gotchas) }
    return Pinout(partId, partName, headers, pins, sources, status, packVersion)
}

private fun SQLiteConnection.headers(args: List<String>): List<PinHeader> {
    val runs =
        query(
            "SELECT header_id, edge, run, pins, first, row1, ord, source_id, page, section, ref FROM header_edge " +
                "WHERE part_id = ? ORDER BY header_id, seq",
            args,
        ) { it.text() to RunRow.read(it) }.groupBy({ it.first }, { it.second })
    return query(
        "SELECT id, label, type, rows, pins_per_row, pitch_mm, source_id, page, section, ref FROM header " +
            "WHERE part_id = ?",
        args,
    ) { HeaderRow.read(it) }.map { it.toHeader(runs[it.id].orEmpty()) }
}

private fun SQLiteConnection.pinGotchas(args: List<String>): Map<String, List<PinGotcha>> = query(
    "SELECT gp.pin_id, g.id, g.title, g.severity FROM gotcha_pin gp JOIN gotcha g " +
        "ON g.part_id = gp.part_id AND g.id = gp.gotcha_id WHERE gp.part_id = ? ORDER BY g.id",
    args,
) { it.text() to PinGotcha(it.text(), it.text(), Severity.fromPack(it.text())) }.groupBy({ it.first }, { it.second })
