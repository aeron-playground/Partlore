package dev.partlore.tools.content.model

import dev.partlore.tools.content.diag.Pos

enum class Level(val yaml: String) {
    DRAFT("draft"),
    IMPORTED("imported"),
    CHECKED("checked"),
    VERIFIED("verified"),
    NEEDS_REVIEW("needs-review"),
    DISPUTED("disputed"),
    ;

    companion object {
        fun of(yaml: String): Level = entries.first { it.yaml == yaml }
    }
}

data class ImportedFrom(val importer: String, val source: String, val ref: String, val on: String)

data class Status(
    val level: Level,
    val checkedBy: List<String>,
    val checkedOn: String?,
    val against: List<String>,
    val importedFrom: ImportedFrom?,
    val note: String?,
    val pos: Pos,
)

data class Citation(val source: String, val page: Int?, val section: String?, val ref: String?, val pos: Pos)

data class Source(
    val id: String,
    val type: String,
    val title: String,
    val publisher: String,
    val url: String,
    val version: String?,
    val retrieved: String,
    val sha256: String?,
    val license: String,
    val pos: Pos,
)

sealed interface Scalar {
    data class Text(val value: String) : Scalar

    data class Num(val value: Double) : Scalar

    data class Bool(val value: Boolean) : Scalar
}

data class SpecValue(
    val key: String,
    val unit: String?,
    val min: Double?,
    val typ: Double?,
    val max: Double?,
    val value: Scalar?,
    val condition: String?,
    val cite: Citation,
    val pos: Pos,
)

data class I2cAddress(
    val address: Int,
    val text: String,
    val isDefault: Boolean,
    val select: String?,
    val cite: Citation,
    val pos: Pos,
)

data class Header(
    val id: String,
    val label: String,
    val type: String,
    val rows: Int,
    val pinsPerRow: Int,
    val pitchMm: Double,
    val cite: Citation,
    val pos: Pos,
)

data class PinFunction(val type: String, val signal: String?, val isDefault: Boolean)

data class Strapping(val role: String, val mustBe: String, val at: String)

data class Pin(
    val id: String,
    val header: String,
    val row: Int,
    val index: Int,
    val chipName: String?,
    val modulePad: String?,
    val boardLabel: String?,
    val labelAliases: List<String>,
    val arduino: String?,
    val direction: String,
    val voltage: Double?,
    val fiveVTolerant: Boolean?,
    val functions: List<PinFunction>,
    val strapping: Strapping?,
    val safe: String,
    val note: String?,
    val cites: List<Citation>,
    val pos: Pos,
) {
    /** Every name the pin goes by, without repeats: chip, pad, label, aliases, Arduino number. */
    val names: List<String>
        get() = (listOfNotNull(chipName, modulePad, boardLabel) + labelAliases + listOfNotNull(arduino)).distinct()
}

data class PinFile(val file: String, val status: Status, val headers: List<Header>, val pins: List<Pin>)

data class Gotcha(
    val id: String,
    val severity: String,
    val title: String,
    val body: String,
    val pins: List<String>,
    val cites: List<Citation>,
    val pos: Pos,
)

data class GotchaFile(val file: String, val status: Status, val gotchas: List<Gotcha>)

data class Part(
    val id: String,
    val folder: String,
    val kind: String,
    val name: String,
    val aliases: List<String>,
    val manufacturer: String,
    val category: String,
    val tags: List<String>,
    val uses: String?,
    val related: List<String>,
    val summary: String,
    val specs: List<SpecValue>,
    val absoluteMax: List<SpecValue>,
    val i2c: List<I2cAddress>,
    val status: Status,
    val sources: List<Source>,
    val pins: PinFile?,
    val gotchas: GotchaFile?,
    val article: String?,
    val pos: Pos,
    /** Where each top-level field of part.yaml is, for precise messages. */
    val fieldPos: Map<String, Pos>,
) {
    fun at(field: String): Pos = fieldPos[field] ?: pos

    val sourcesById: Map<String, Source> get() = sources.associateBy { it.id }

    /** Every citation in the part's files. */
    val citations: List<Citation>
        get() =
            (specs + absoluteMax).map { it.cite } + i2c.map { it.cite } +
                pins?.headers?.map { it.cite }.orEmpty() + pins?.pins?.flatMap { it.cites }.orEmpty() +
                gotchas?.gotchas?.flatMap { it.cites }.orEmpty()
}

data class Category(val id: String, val name: String, val parent: String?, val sort: Int?, val pos: Pos)

data class Tag(val id: String, val label: String, val pos: Pos)

data class SpecKey(
    val key: String,
    val label: String,
    val type: String,
    val unit: String?,
    val description: String,
    val pos: Pos,
)

data class Content(
    val version: String,
    val categories: List<Category>,
    val tags: List<Tag>,
    val specKeys: List<SpecKey>,
    val parts: List<Part>,
    /** Folders (maker/part) whose files could not be read; others may still point at them. */
    val unreadable: Set<String>,
)
