package dev.partlore.tools.packer

import dev.partlore.tools.content.cli.CliOptions
import dev.partlore.tools.content.diag.DiagnosticPrinter
import java.io.File
import java.io.PrintStream
import java.time.LocalDate

class PackerCli(
    private val out: PrintStream,
    private val github: Boolean,
    private val today: LocalDate = LocalDate.now(),
) {
    fun run(args: Array<String>): Int {
        val options = CliOptions.parse(args)
        val outDir = options?.out
        return if (options == null || options.command != "pack" || outDir == null) usage() else pack(options, outDir)
    }

    private fun pack(options: CliOptions, outDir: File): Int {
        val result = Packer(options.content, outDir, today).pack(options.mode)
        DiagnosticPrinter(out, github, options.prefix).print(result.diagnostics)
        return when (result) {
            is Packer.Result.Invalid -> 1.also { out.println("Not packed: fix the errors above.") }

            is Packer.Result.Packed -> {
                val parts = result.entry.parts
                out.println(
                    "Packed $parts part${if (parts == 1) "" else "s"} into ${result.file.path} " +
                        "(${result.entry.size} bytes, sha256 ${result.entry.sha256})",
                )
                0
            }
        }
    }

    private fun usage(): Int {
        out.println("usage: pack --content <dir> --out <dir> [--preview]")
        return 2
    }
}
