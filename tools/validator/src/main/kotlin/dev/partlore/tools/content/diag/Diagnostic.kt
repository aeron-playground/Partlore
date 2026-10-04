package dev.partlore.tools.content.diag

/** A place in a content file: path relative to the content folder, line and column counted from 1. */
data class Pos(val file: String, val line: Int, val col: Int)

enum class Severity { ERROR, WARNING }

data class Diagnostic(val file: String, val line: Int, val col: Int, val severity: Severity, val message: String) :
    Comparable<Diagnostic> {
    override fun compareTo(other: Diagnostic): Int =
        compareValuesBy(this, other, Diagnostic::file, Diagnostic::line, Diagnostic::col, Diagnostic::message)

    /** `content/parts/x/pins.yaml:12:5: error: …`, the format terminals and editors turn into links. */
    fun plain(prefix: String): String = "$prefix$file:$line:$col: ${severity.name.lowercase()}: $message"

    /** A GitHub Actions command: the message shows on this line of the pull request. */
    fun github(prefix: String): String = "::${severity.name.lowercase()} file=$prefix$file,line=$line,col=$col::" +
        message.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
}

/** Collects problems; read them sorted by file, line and column. */
class Diagnostics {
    private val items = mutableListOf<Diagnostic>()

    fun error(pos: Pos, message: String) {
        items += Diagnostic(pos.file, pos.line, pos.col, Severity.ERROR, message)
    }

    val all: List<Diagnostic> get() = items.sorted()

    val hasErrors: Boolean get() = items.any { it.severity == Severity.ERROR }
}
