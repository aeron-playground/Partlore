package dev.partlore.tools.content

import dev.partlore.tools.content.diag.Diagnostic
import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.diag.Severity
import dev.partlore.tools.content.load.ContentLoader
import dev.partlore.tools.content.model.Content
import dev.partlore.tools.content.rules.CitationRules
import dev.partlore.tools.content.rules.PartLinkRules
import dev.partlore.tools.content.rules.PinLayoutRules
import dev.partlore.tools.content.rules.SharedFileRules
import java.io.File

data class ValidationResult(val content: Content, val diagnostics: List<Diagnostic>) {
    val hasErrors: Boolean get() = diagnostics.any { it.severity == Severity.ERROR }
}

/** Loads content/ and runs every rule. */
class Validator(private val contentDir: File) {
    fun validate(): ValidationResult {
        val diagnostics = Diagnostics()
        val content = ContentLoader(contentDir).load(diagnostics)
        SharedFileRules.check(content, diagnostics)
        CitationRules.check(content, diagnostics)
        PartLinkRules.check(content, diagnostics)
        PinLayoutRules.check(content, diagnostics)
        return ValidationResult(content, diagnostics.all)
    }
}
