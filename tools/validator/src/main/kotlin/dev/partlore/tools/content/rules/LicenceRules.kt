package dev.partlore.tools.content.rules

import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.model.Content

/** Sources must have a licence we can work with, and PDFs must say exactly which file was checked. */
object LicenceRules {
    val ALLOWED: Set<String> =
        setOf(
            "link-only", "CC0-1.0", "CC-BY-4.0", "CC-BY-SA-3.0", "CC-BY-SA-4.0", "Apache-2.0", "MIT", "BSD-2-Clause",
            "BSD-3-Clause",
        )

    private val NON_COMMERCIAL_OR_NO_CHANGES = Regex("(^|-)(NC|ND)(-|$)")

    fun check(content: Content, d: Diagnostics) = content.parts.flatMap { it.sources }.forEach { source ->
        licenceProblem(source.license)?.let { d.error(source.pos, "source ${source.id}: $it") }
        if (source.type in PDF_TYPES && (source.version == null || source.sha256 == null)) {
            d.error(
                source.pos,
                "source ${source.id} is a ${source.type}: add version and the sha256 of the exact PDF",
            )
        }
    }

    private fun licenceProblem(licence: String): String? = when {
        licence in ALLOWED -> null

        NON_COMMERCIAL_OR_NO_CHANGES.containsMatchIn(licence) ->
            "licence $licence doesn't allow commercial use or changes. " +
                "Use license: link-only and enter the facts by hand."

        else -> "licence $licence is not on the allow-list (${ALLOWED.joinToString()})"
    }
}
