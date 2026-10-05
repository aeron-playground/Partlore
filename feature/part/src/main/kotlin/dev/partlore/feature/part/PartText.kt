package dev.partlore.feature.part

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.partlore.core.model.Cite
import dev.partlore.core.model.FiveVoltTolerance
import dev.partlore.core.model.SourceItem
import dev.partlore.core.model.VerificationLevel

private const val LINK_ONLY = "link-only"

/** Where in its source a value is: "page 28", "section Power options" or "entry pins.p12". */
@Composable
internal fun citePlace(cite: Cite): String = cite.page?.let { stringResource(R.string.part_place_page, it) }
    ?: cite.section?.let { stringResource(R.string.part_place_section, it) }
    ?: cite.ref?.let { stringResource(R.string.part_place_ref, it) }
    ?: ""

internal fun sourceTitle(cite: Cite, sources: List<SourceItem>): String =
    sources.firstOrNull { it.id == cite.sourceId }?.title ?: cite.sourceId

/** What TalkBack reads for a source dot: "Source: Example DevBoard Datasheet, page 28". */
@Composable
internal fun sourceDescription(cite: Cite, sources: List<SourceItem>): String =
    stringResource(R.string.part_source_for, sourceTitle(cite, sources), citePlace(cite))

/** A gotcha's source chip: "Datasheet · page 13". */
@Composable
internal fun citeChip(cite: Cite, sources: List<SourceItem>): String {
    val type = sources.firstOrNull { it.id == cite.sourceId }?.type ?: cite.sourceId
    return "${type.replace('-', ' ').replaceFirstChar { it.uppercase() }} · ${citePlace(cite)}"
}

@Composable
internal fun licenceText(license: String): String = if (license ==
    LINK_ONLY
) {
    stringResource(R.string.part_link_only)
} else {
    stringResource(R.string.part_licence, license)
}

/** A kind this app doesn't know (a newer pack) is called "Part". */
@StringRes
internal fun kindLabel(kind: String): Int = when (kind) {
    "chip" -> R.string.part_kind_chip
    "module" -> R.string.part_kind_module
    "board" -> R.string.part_kind_board
    "sensor" -> R.string.part_kind_sensor
    "display" -> R.string.part_kind_display
    "power" -> R.string.part_kind_power
    else -> R.string.part_kind_other
}

@StringRes
internal fun FiveVoltTolerance.label(): Int = when (this) {
    FiveVoltTolerance.All -> R.string.part_five_volt_all
    FiveVoltTolerance.Some -> R.string.part_five_volt_some
    FiveVoltTolerance.None -> R.string.part_five_volt_none
}

@StringRes
internal fun VerificationLevel.help(): Int = when (this) {
    VerificationLevel.Draft -> R.string.part_help_draft
    VerificationLevel.Imported -> R.string.part_help_imported
    VerificationLevel.Checked -> R.string.part_help_checked
    VerificationLevel.Verified -> R.string.part_help_verified
    VerificationLevel.NeedsReview -> R.string.part_help_needs_review
    VerificationLevel.Disputed -> R.string.part_help_disputed
}
