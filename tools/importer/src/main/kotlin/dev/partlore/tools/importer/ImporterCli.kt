package dev.partlore.tools.importer

import tools.jackson.core.JacksonException
import tools.jackson.dataformat.yaml.YAMLMapper
import java.io.File
import java.io.PrintStream
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * `--checkout <esp-dev-kits> --hints <board.yaml> --out <content/parts> [--on <yyyy-mm-dd>]`.
 * Exit 0: the files are written. 1: the import stopped, and the reason is printed. 2: wrong options.
 */
class ImporterCli(private val out: PrintStream, private val err: PrintStream) {
    fun run(args: Array<String>): Int {
        val options = options(args)
        if (options == null) {
            err.println(USAGE)
            return 2
        }
        return try {
            importBoard(options)
            0
        } catch (e: ImportException) {
            err.println("import stopped: ${e.message}")
            1
        }
    }

    private class Options(val checkout: File, val hints: File, val out: File, val on: LocalDate)

    private fun options(args: Array<String>): Options? {
        val given = args.toList().chunked(2).filter { it.size == 2 }.associate { (key, value) -> key to value }
        val on = given["--on"]?.let(::date) ?: LocalDate.now()
        val valid = args.size % 2 == 0 && given.keys.all { it in KNOWN } && REQUIRED.all { it in given } &&
            (given["--on"] == null || date(given.getValue("--on")) != null)
        return if (valid) {
            Options(
                checkout = File(given.getValue("--checkout")),
                hints = File(given.getValue("--hints")),
                out = File(given.getValue("--out")),
                on = on,
            )
        } else {
            null
        }
    }

    private fun date(text: String): LocalDate? = try {
        LocalDate.parse(text)
    } catch (_: DateTimeParseException) {
        null
    }

    private fun importBoard(options: Options) {
        val hints = Hints.read(options.hints.readText())
        val guide = File(options.checkout, hints.guide.ref)
        if (!guide.isFile) throw ImportException("there is no guide at ${guide.path}")
        // The commit written to sources.yaml must be exactly what was read.
        if (git(options.checkout, "status", "--porcelain", "--", hints.guide.ref).isNotBlank()) {
            throw ImportException("${hints.guide.ref} has local changes; import committed files only")
        }
        val commit = git(options.checkout, "rev-parse", "HEAD").trim()
        val result = Importer.import(guide.readText(), hints, commit, options.on)
        val dir = File(options.out, hints.part.get("id").asString())
        keepPeoplesFiles(dir, result.files.keys)
        dir.mkdirs()
        result.files.forEach { (name, text) -> File(dir, name).writeText(text) }
        out.println("Wrote ${result.files.keys.sorted().joinToString()} to ${dir.path}")
        out.println("Look at these when you check the board:")
        result.flagged.forEach { out.println("  - $it") }
    }

    // Only the importer's own files at level imported may be replaced; anything else may hold a person's check.
    private fun keepPeoplesFiles(dir: File, names: Set<String>) {
        val kept = names.map { File(dir, it) }.filter { it.isFile && !ours(it) }
        if (kept.isNotEmpty()) {
            throw ImportException(
                "won't overwrite ${kept.joinToString { it.name }} in ${dir.path}: " +
                    "not written by the importer at level imported",
            )
        }
    }

    // sources.yaml has no status. part.yaml and pins.yaml must still be at level imported: a person may
    // have checked a file without removing the importer's marker. A file that can't be read stays too.
    private fun ours(file: File): Boolean {
        val text = file.readText()
        val marked =
            text.startsWith("# yaml-language-server") && text.lines().getOrNull(1).orEmpty().startsWith(Importer.MARKER)
        val level =
            try {
                YAMLMapper().readTree(text)?.get("status")?.get("level")?.asString()
            } catch (_: JacksonException) {
                return false
            }
        return marked && if (file.name == "sources.yaml") level == null else level == "imported"
    }

    private fun git(dir: File, vararg args: String): String {
        val process = ProcessBuilder("git", "-C", dir.path, *args).start()
        val output = process.inputStream.bufferedReader().readText()
        val problem = process.errorStream.bufferedReader().readText()
        if (process.waitFor() != 0) {
            throw ImportException("git ${args.joinToString(" ")} failed in ${dir.path}: $problem")
        }
        return output
    }

    private companion object {
        val REQUIRED = setOf("--checkout", "--hints", "--out")
        val KNOWN = REQUIRED + "--on"
        const val USAGE =
            "usage: importer --checkout <esp-dev-kits> --hints <board.yaml> --out <content/parts> " +
                "[--on <yyyy-mm-dd>]"
    }
}
