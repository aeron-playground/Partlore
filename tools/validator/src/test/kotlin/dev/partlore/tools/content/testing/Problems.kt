package dev.partlore.tools.content.testing

import dev.partlore.tools.content.Validator
import dev.partlore.tools.content.diag.Diagnostic
import dev.partlore.tools.content.ship.Mode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.time.LocalDate

internal val TODAY: LocalDate = LocalDate.of(2026, 10, 4)

internal fun ContentFixture.problems(mode: Mode = Mode.RELEASE): List<Diagnostic> =
    Validator(root, TODAY).validate(mode).diagnostics

internal fun assertProblem(problems: List<Diagnostic>, file: String, line: Int, messagePart: String) {
    assertTrue(
        "expected a problem at $file:$line containing \"$messagePart\", got:\n" +
            problems.joinToString("\n") { it.plain("") },
        problems.any { it.file == file && it.line == line && messagePart in it.message },
    )
}

internal fun assertNoProblems(problems: List<Diagnostic>) {
    assertEquals("", problems.joinToString("\n") { it.plain("") })
}
