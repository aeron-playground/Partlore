package dev.partlore.tools.content

import dev.partlore.tools.content.diag.Diagnostic
import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.diag.Severity
import dev.partlore.tools.content.load.ContentLoader
import dev.partlore.tools.content.model.Content
import dev.partlore.tools.content.rules.CitationRules
import dev.partlore.tools.content.rules.HeaderRunRules
import dev.partlore.tools.content.rules.LicenceRules
import dev.partlore.tools.content.rules.PartLinkRules
import dev.partlore.tools.content.rules.PinLayoutRules
import dev.partlore.tools.content.rules.SafetyRules
import dev.partlore.tools.content.rules.SharedFileRules
import dev.partlore.tools.content.rules.StatusRules
import dev.partlore.tools.content.rules.latestToday
import dev.partlore.tools.content.ship.Mode
import java.io.File
import java.time.LocalDate

data class ValidationResult(val content: Content, val diagnostics: List<Diagnostic>) {
    val hasErrors: Boolean get() = diagnostics.any { it.severity == Severity.ERROR }
}

/** Loads content/ and runs every rule. */
class Validator(private val contentDir: File, private val today: LocalDate = latestToday()) {
    fun validate(mode: Mode = Mode.RELEASE): ValidationResult {
        val diagnostics = Diagnostics()
        val content = ContentLoader(contentDir).load(diagnostics)
        SharedFileRules.check(content, diagnostics)
        CitationRules.check(content, diagnostics)
        PartLinkRules.check(content, diagnostics)
        PinLayoutRules.check(content, diagnostics)
        HeaderRunRules.check(content, diagnostics)
        SafetyRules.check(content, diagnostics)
        LicenceRules.check(content, diagnostics)
        StatusRules(today).check(content, mode, diagnostics)
        return ValidationResult(content, diagnostics.all)
    }
}
