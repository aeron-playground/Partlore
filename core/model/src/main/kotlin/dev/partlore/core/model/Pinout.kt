package dev.partlore.core.model

enum class BoardEdge(val packName: String) {
    Left("left"),
    Right("right"),
    Top("top"),
    Bottom("bottom"),
    Inside("inside"),
    ;

    companion object {
        /** Null for a value this app doesn't know: the header is then not drawn. */
        fun fromPack(name: String?): BoardEdge? = entries.firstOrNull { it.packName == name }
    }
}

enum class Side(val packName: String) {
    Top("top"),
    Bottom("bottom"),
    Left("left"),
    Right("right"),
    ;

    companion object {
        fun fromPack(name: String?): Side? = entries.firstOrNull { it.packName == name }
    }
}

enum class RunAxis { Vertical, Horizontal }

/** Part of a header along one board edge, in pin order; [first] is the end its first position is at. */
data class HeaderRun(
    val edge: BoardEdge,
    val axis: RunAxis,
    val pins: Int,
    val first: Side,
    val row1: Side?,
    val order: Int?,
    val cite: Cite,
)

data class PinHeader(
    val id: String,
    val label: String,
    val type: String,
    val rows: Int,
    val pinsPerRow: Int,
    val pitchMm: Double,
    val runs: List<HeaderRun>,
    val cite: Cite,
)

enum class PinDirection(val packName: String) {
    Io("io"),
    In("in"),
    Out("out"),
    Power("power"),
    Ground("ground"),
    NotConnected("nc"),
    Unknown(""),
    ;

    companion object {
        fun fromPack(name: String?): PinDirection =
            entries.firstOrNull { it.packName == name && it != Unknown } ?: Unknown
    }
}

/** How safe a pin is to use. A value this app doesn't know counts as caution. */
enum class PinSafety(val packName: String) {
    Ok("ok"),
    Caution("caution"),
    Avoid("avoid"),
    ;

    companion object {
        fun fromPack(name: String?): PinSafety = entries.firstOrNull { it.packName == name } ?: Caution
    }
}

data class PinFunction(val type: String, val signal: String?, val isDefault: Boolean)

/** [mustBe] is the level a normal start needs; null when the source gives none. */
data class Strapping(val role: String, val mustBe: String?, val at: String)

data class PinGotcha(val id: String, val title: String, val severity: Severity)

data class PinInfo(
    val id: String,
    val headerId: String,
    val row: Int,
    val index: Int,
    val chipName: String?,
    val modulePad: String?,
    val boardLabel: String?,
    val arduino: String?,
    val aliases: List<String>,
    val direction: PinDirection,
    val voltage: Double?,
    val fiveVoltTolerant: Boolean?,
    val functions: List<PinFunction>,
    val safe: PinSafety,
    val strapping: Strapping?,
    val note: String?,
    val cites: List<Cite>,
    val gotchas: List<PinGotcha>,
)

enum class PinLabelMode { Chip, Module, Board, Arduino }

data class Pinout(
    val partId: String,
    val partName: String,
    val headers: List<PinHeader>,
    val pins: List<PinInfo>,
    val sources: List<SourceItem>,
    val status: FileStatus?,
    val packVersion: String,
)
