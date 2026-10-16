package dev.partlore.core.content

import androidx.sqlite.SQLiteConnection
import dev.partlore.core.model.FileStatus
import dev.partlore.core.model.FiveVoltTolerance
import dev.partlore.core.model.Glance
import dev.partlore.core.model.PartPage
import dev.partlore.core.model.PartRef
import dev.partlore.core.model.Pinout
import dev.partlore.core.model.SourceItem
import dev.partlore.core.model.VerificationLevel

/** Everything for one Part page. */
internal class PartQueries(private val db: SQLiteConnection) {
    fun part(id: String, packVersion: String): PartPage? {
        val args = listOf(id)
        val head =
            db.query("SELECT kind, name, manufacturer, summary, uses_id FROM part WHERE id = ?", args) {
                Head(it.text(), it.text(), it.text(), it.text(), it.textOrNull())
            }.firstOrNull() ?: return null
        val statuses = statuses(id)
        val sources = sources(id)
        return PartPage(
            id = id,
            name = head.name,
            aliases = db.query("SELECT alias FROM part_alias WHERE part_id = ?", args) { it.text() },
            kind = head.kind,
            manufacturer = head.manufacturer,
            summary = head.summary,
            uses = head.usesId?.let { refs("SELECT id, name FROM part WHERE id = ?", listOf(it)).firstOrNull() },
            usedBy = refs("SELECT id, name FROM part WHERE uses_id = ? ORDER BY name COLLATE NOCASE", args),
            related =
            refs(
                "SELECT p.id, p.name FROM part_related r JOIN part p ON p.id = r.related_id " +
                    "WHERE r.part_id = ? ORDER BY p.name COLLATE NOCASE",
                args,
            ),
            glance = glance(id),
            specs = db.specs(id, absolute = false),
            absoluteMax = db.specs(id, absolute = true),
            i2c = db.i2c(id),
            gotchas = db.gotchas(id),
            sources = sources,
            partStatus = statuses["part"] ?: FileStatus(VerificationLevel.Draft, emptyList(), null, emptyList(), null),
            pinsStatus = statuses["pins"],
            gotchasStatus = statuses["gotchas"],
            packVersion = packVersion,
            pinout = db.pinout(id, head.name, sources, statuses["pins"], packVersion),
        )
    }

    fun pinout(id: String, packVersion: String): Pinout? {
        val name = db.query("SELECT name FROM part WHERE id = ?", listOf(id)) { it.text() }.firstOrNull() ?: return null
        return db.pinout(id, name, sources(id), statuses(id)["pins"], packVersion)
    }

    private fun refs(sql: String, args: List<String>): List<PartRef> =
        db.query(sql, args) { PartRef(it.text(), it.text()) }

    private fun glance(id: String): Glance = db.query(
        "SELECT logic_level_v, five_v_tolerant, gpio_count, adc_channels, wifi, bluetooth " +
            "FROM glance WHERE part_id = ?",
        listOf(id),
    ) {
        Glance(
            it.doubleOrNull(),
            FiveVoltTolerance.fromPack(it.textOrNull()),
            it.intOrNull(),
            it.intOrNull(),
            it.textOrNull(),
            it.textOrNull(),
        )
    }.firstOrNull() ?: Glance(null, null, null, null, null, null)

    private fun statuses(id: String): Map<String, FileStatus> = db.query(
        "SELECT file, level, checked_by, checked_on, against, note FROM status WHERE part_id = ?",
        listOf(id),
    ) {
        it.text() to
            FileStatus(
                level = VerificationLevel.fromPack(it.text()),
                checkedBy = split(it.textOrNull()),
                checkedOn = it.textOrNull(),
                against = split(it.textOrNull()),
                note = it.textOrNull(),
            )
    }.toMap()

    /** Sources in their natural order: s1, s2 … s10. */
    private fun sources(id: String): List<SourceItem> = db.query(
        "SELECT id, type, title, publisher, url, version, retrieved, license FROM source WHERE part_id = ?",
        listOf(id),
    ) {
        SourceItem(it.text(), it.text(), it.text(), it.text(), it.text(), it.textOrNull(), it.text(), it.text())
    }.sortedWith(compareBy({ it.id.length }, { it.id }))

    // The packer stores lists as "a, b".
    private fun split(text: String?): List<String> = text?.split(", ")?.filter { it.isNotBlank() }.orEmpty()

    private data class Head(
        val kind: String,
        val name: String,
        val manufacturer: String,
        val summary: String,
        val usesId: String?,
    )
}
