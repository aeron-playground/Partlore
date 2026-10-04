package dev.partlore.core.model

/** How far a file has been checked (`status.level` in the pack). */
enum class VerificationLevel(val packName: String) {
    Draft("draft"),
    Imported("imported"),
    Checked("checked"),
    Verified("verified"),
    NeedsReview("needs-review"),
    Disputed("disputed"),
    ;

    companion object {
        /** A level this app doesn't know (a newer pack) counts as Draft: never claim more checking than we know. */
        fun fromPack(name: String?): VerificationLevel = entries.firstOrNull { it.packName == name } ?: Draft
    }
}

/** How bad a gotcha is. Declared from worst to mildest: the Part page lists them in this order. */
enum class Severity(val packName: String) {
    Danger("danger"),
    Caution("caution"),
    Info("info"),
    ;

    companion object {
        /** A severity this app doesn't know shows as Caution, so a warning is never hidden. */
        fun fromPack(name: String?): Severity = entries.firstOrNull { it.packName == name } ?: Caution
    }
}

/** How many input pins take 5 V (`glance.five_v_tolerant`). */
enum class FiveVoltTolerance(val packName: String) {
    All("all"),
    Some("some"),
    None("none"),
    ;

    companion object {
        fun fromPack(name: String?): FiveVoltTolerance? = entries.firstOrNull { it.packName == name }
    }
}

data class Tag(val id: String, val label: String)

data class PartRef(val id: String, val name: String)

data class PartCard(
    val id: String,
    val name: String,
    val kind: String,
    val categoryId: String,
    val level: VerificationLevel,
    val tags: List<Tag>,
)

/** A category with the number of parts in it and in all its sub-categories. */
data class CategoryTile(val id: String, val name: String, val partCount: Int)

data class LibraryHome(
    val isPreview: Boolean,
    val partCount: Int,
    val starterBoards: List<PartCard>,
    val categories: List<CategoryTile>,
)

data class CategoryPage(
    val id: String,
    val name: String,
    val children: List<CategoryTile>,
    val tags: List<Tag>,
    val parts: List<PartCard>,
    /** The IDs of the parts under each child category, for the child chips. */
    val partsByChild: Map<String, Set<String>>,
)

/** Where in a source a value comes from: a page (PDF), a section (web page) or a ref (dataset). */
data class Cite(val sourceId: String, val page: Int?, val section: String?, val ref: String?)

/** One spec value. Ranges use [min]/[typ]/[max], single numbers [number], words [text]. */
data class SpecRow(
    val label: String,
    val unit: String?,
    val min: Double?,
    val typ: Double?,
    val max: Double?,
    val number: Double?,
    val text: String?,
    val condition: String?,
    val cite: Cite,
)

data class I2cRow(val address: Int, val isDefault: Boolean, val select: String?, val cite: Cite)

/** A gotcha; [pins] are display names of the pins it is about. */
data class GotchaItem(
    val id: String,
    val severity: Severity,
    val title: String,
    val body: String,
    val pins: List<String>,
    val cites: List<Cite>,
)

data class SourceItem(
    val id: String,
    val type: String,
    val title: String,
    val publisher: String,
    val url: String,
    val version: String?,
    val retrieved: String,
    val license: String,
)

data class FileStatus(
    val level: VerificationLevel,
    val checkedBy: List<String>,
    val checkedOn: String?,
    val against: List<String>,
    val note: String?,
)

data class Glance(
    val logicLevelV: Double?,
    val fiveVoltTolerant: FiveVoltTolerance?,
    val gpioCount: Int?,
    val adcChannels: Int?,
    val wifi: String?,
    val bluetooth: String?,
)

data class PartPage(
    val id: String,
    val name: String,
    val aliases: List<String>,
    val kind: String,
    val manufacturer: String,
    val summary: String,
    val uses: PartRef?,
    val usedBy: List<PartRef>,
    val related: List<PartRef>,
    val glance: Glance,
    val specs: List<SpecRow>,
    val absoluteMax: List<SpecRow>,
    val i2c: List<I2cRow>,
    val gotchas: List<GotchaItem>,
    val sources: List<SourceItem>,
    val partStatus: FileStatus,
    val pinsStatus: FileStatus?,
    val gotchasStatus: FileStatus?,
    val packVersion: String,
) {
    val dangerCount: Int get() = gotchas.count { it.severity == Severity.Danger }
}

/** The answer to a content question: the value, "not in this pack", or why the pack can't be read. */
sealed interface ContentResult<out T> {
    data class Ok<T>(val value: T) : ContentResult<T>

    data object NotFound : ContentResult<Nothing>

    data class Failed(val problem: ContentProblem, val detail: String) : ContentResult<Nothing>
}

enum class ContentProblem { Missing, Damaged, TooNew }
