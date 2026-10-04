package dev.partlore.tools.packer

import dev.partlore.core.packformat.PackFormat
import dev.partlore.tools.content.Validator
import dev.partlore.tools.content.diag.Diagnostic
import dev.partlore.tools.content.model.Content
import dev.partlore.tools.content.rules.latestToday
import dev.partlore.tools.content.ship.Mode
import java.io.File
import java.time.LocalDate

/** Validates content/, then writes the pack and manifest. Invalid content is never packed. */
class Packer(private val contentDir: File, private val outDir: File, private val today: LocalDate = latestToday()) {
    sealed interface Result {
        val diagnostics: List<Diagnostic>

        data class Packed(val file: File, val entry: Manifest.Entry, override val diagnostics: List<Diagnostic>) :
            Result

        data class Invalid(override val diagnostics: List<Diagnostic>) : Result
    }

    fun pack(mode: Mode): Result {
        val validation = Validator(contentDir, today).validate(mode)
        return if (validation.hasErrors) {
            Result.Invalid(validation.diagnostics)
        } else {
            write(validation.content, mode, validation.diagnostics)
        }
    }

    private fun write(content: Content, mode: Mode, diagnostics: List<Diagnostic>): Result {
        outDir.mkdirs()
        // A new version gets a new file name; old packs must not linger next to it.
        outDir.listFiles { f -> f.name.startsWith("partlore-content-") && f.name.endsWith(".db") }
            .orEmpty()
            .forEach { it.delete() }
        val file = File(outDir, PackFormat.packFileName(content.version))
        val parts = PackWriter().write(content, mode, ContentHash.of(contentDir), file)
        val entry =
            Manifest.Entry(
                id = "core",
                version = content.version,
                file = file.name,
                size = file.length(),
                sha256 = sha256(file),
                parts = parts,
                preview = mode == Mode.PREVIEW,
            )
        File(outDir, PackFormat.MANIFEST_FILE).writeText(Manifest.json(listOf(entry)))
        return Result.Packed(file, entry, diagnostics)
    }
}
