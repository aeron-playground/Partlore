package dev.partlore.tools.content.cli

import dev.partlore.tools.content.Validator
import dev.partlore.tools.content.checklist.Checklist
import dev.partlore.tools.content.diag.DiagnosticPrinter
import dev.partlore.tools.content.ship.Mode
import java.io.File
import java.io.PrintStream
import java.time.LocalDate

class ValidatorCli(
    private val out: PrintStream,
    private val github: Boolean,
    private val today: LocalDate = LocalDate.now(),
) {
    fun run(args: Array<String>): Int {
        val options = CliOptions.parse(args) ?: return usage()
        return when (options.command) {
            "validate" -> validate(options)
            "checklist" -> checklist(options)
            else -> usage()
        }
    }

    private fun validate(options: CliOptions): Int {
        val result = Validator(options.content, today).validate(options.mode)
        val printer = DiagnosticPrinter(out, github, options.prefix)
        printer.print(result.diagnostics)
        printer.summary(result.content.parts.size, result.diagnostics)
        return if (result.hasErrors) 1 else 0
    }

    // Checklists are for drafts too, so they use preview rules.
    private fun checklist(options: CliOptions): Int {
        val outDir = options.out ?: return usage()
        val result = Validator(options.content, today).validate(Mode.PREVIEW)
        DiagnosticPrinter(out, github, options.prefix).print(result.diagnostics)
        if (!result.hasErrors) {
            result.content.parts.filter { options.part == null || it.id == options.part }.forEach { part ->
                val file = File(outDir, "${part.id}.md").apply { parentFile.mkdirs() }
                file.writeText(Checklist.render(part))
                out.println("Wrote ${file.path}")
            }
        }
        return if (result.hasErrors) 1 else 0
    }

    private fun usage(): Int {
        out.println("usage: validate|checklist [--content <dir>] [--out <dir>] [--part <maker/part>] [--preview]")
        return 2
    }
}
