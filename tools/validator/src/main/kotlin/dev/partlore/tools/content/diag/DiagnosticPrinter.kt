package dev.partlore.tools.content.diag

import java.io.PrintStream

/** Prints problems for people and, on GitHub Actions, as annotations on the pull request. */
class DiagnosticPrinter(private val out: PrintStream, private val github: Boolean, private val prefix: String) {
    fun print(diagnostics: List<Diagnostic>) = diagnostics.forEach {
        out.println(it.plain(prefix))
        if (github) out.println(it.github(prefix))
    }

    fun summary(parts: Int, diagnostics: List<Diagnostic>) {
        val errors = diagnostics.count { it.severity == Severity.ERROR }
        out.println("${count(parts, "part")}, ${count(errors, "error")}")
    }

    private fun count(n: Int, word: String): String = "$n $word" + if (n == 1) "" else "s"
}
