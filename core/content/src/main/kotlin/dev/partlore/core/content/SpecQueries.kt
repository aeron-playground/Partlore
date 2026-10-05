package dev.partlore.core.content

import androidx.sqlite.SQLiteConnection
import dev.partlore.core.model.Cite
import dev.partlore.core.model.GotchaItem
import dev.partlore.core.model.I2cRow
import dev.partlore.core.model.Severity
import dev.partlore.core.model.SpecRow

/** Reads the four citation columns that come next: source_id, page, section, ref. */
internal fun Columns.cite(): Cite = Cite(text(), intOrNull(), textOrNull(), textOrNull())

internal fun SQLiteConnection.specs(partId: String, absolute: Boolean): List<SpecRow> = query(
    "SELECT label, unit, min, typ, max, value_number, value_text, condition, source_id, page, section, ref " +
        "FROM spec WHERE part_id = ? AND is_absolute_max = ${if (absolute) 1 else 0} ORDER BY seq",
    listOf(partId),
) {
    // Named arguments are evaluated in the order written, which is the SELECT order.
    SpecRow(
        label = it.text(),
        unit = it.textOrNull(),
        min = it.doubleOrNull(),
        typ = it.doubleOrNull(),
        max = it.doubleOrNull(),
        number = it.doubleOrNull(),
        text = it.textOrNull(),
        condition = it.textOrNull(),
        cite = it.cite(),
    )
}

internal fun SQLiteConnection.i2c(partId: String): List<I2cRow> = query(
    "SELECT address, is_default, select_note, source_id, page, section, ref FROM i2c_address " +
        "WHERE part_id = ? ORDER BY address",
    listOf(partId),
) { I2cRow(it.int(), it.int() == 1, it.textOrNull(), it.cite()) }

/** Gotchas, worst first. Pins show as their board label, else chip name, else module pad. */
internal fun SQLiteConnection.gotchas(partId: String): List<GotchaItem> {
    val args = listOf(partId)
    val labels =
        query("SELECT id, coalesce(board_label, chip_name, module_pad, id) FROM pin WHERE part_id = ?", args) {
            it.text() to it.text()
        }.toMap()
    val pins =
        query("SELECT gotcha_id, pin_id FROM gotcha_pin WHERE part_id = ?", args) { it.text() to it.text() }
            .groupBy({ it.first }, { labels[it.second] ?: it.second })
    val cites =
        query(
            "SELECT gotcha_id, source_id, page, section, ref FROM gotcha_cite " +
                "WHERE part_id = ? ORDER BY gotcha_id, seq",
            args,
        ) { it.text() to it.cite() }.groupBy({ it.first }, { it.second })
    return query("SELECT id, severity, title, body FROM gotcha WHERE part_id = ?", args) {
        GotchaItem(it.text(), Severity.fromPack(it.text()), it.text(), it.text(), emptyList(), emptyList())
    }.map { it.copy(pins = pins[it.id].orEmpty(), cites = cites[it.id].orEmpty()) }
        .sortedWith(compareBy({ it.severity.ordinal }, { it.id }))
}
