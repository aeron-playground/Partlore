package dev.partlore.feature.part

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.components.PlButton
import dev.partlore.core.designsystem.components.PlSheet
import dev.partlore.core.designsystem.components.PlStatusBadge
import dev.partlore.core.designsystem.components.rememberLinkOpener
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.Cite
import dev.partlore.core.model.FileStatus
import dev.partlore.core.model.PartPage
import dev.partlore.core.model.SourceItem

internal sealed interface PartSheet {
    data object Status : PartSheet

    data class Source(val cite: Cite) : PartSheet
}

@Composable
internal fun PartSheetHost(sheet: PartSheet?, page: PartPage, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    if (sheet != null) {
        PlSheet(onDismiss = onDismiss, modifier = modifier) {
            when (sheet) {
                PartSheet.Status -> StatusSheetContent(page)
                is PartSheet.Source -> SourceSheetContent(sheet.cite, page.sources)
            }
        }
    }
}

/** What each file's level means for this part, who checked it, when and against what. */
@Composable
internal fun StatusSheetContent(page: PartPage, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space16)) {
        SheetTitle(stringResource(R.string.part_status_title))
        StatusExplained(stringResource(R.string.part_file_part), page.partStatus, page.sources)
        page.pinsStatus?.let { StatusExplained(stringResource(R.string.part_file_pins), it, page.sources) }
        page.gotchasStatus?.let { StatusExplained(stringResource(R.string.part_file_gotchas), it, page.sources) }
    }
}

@Composable
private fun StatusExplained(
    file: String,
    status: FileStatus,
    sources: List<SourceItem>,
    modifier: Modifier = Modifier,
) {
    val colors = PartloreTheme.colors
    val typography = PartloreTheme.typography
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space4)) {
        Text(file, style = typography.title, color = colors.textPrimary)
        PlStatusBadge(status.level)
        Text(stringResource(status.level.help()), style = typography.bodyM, color = colors.textPrimary)
        CheckedLine(status, sources)
        status.note?.let { Text(it, style = typography.caption, color = colors.textSecondary) }
    }
}

/** "Checked by alice on 2026-10-04, against Example Datasheet." Nothing while nobody has checked the file. */
@Composable
internal fun CheckedLine(status: FileStatus, sources: List<SourceItem>, modifier: Modifier = Modifier) {
    if (status.checkedBy.isNotEmpty()) {
        val against = status.against.joinToString(", ") { id -> sources.firstOrNull { it.id == id }?.title ?: id }
        Text(
            stringResource(
                R.string.part_checked_line,
                status.checkedBy.joinToString(", "),
                status.checkedOn.orEmpty(),
                against,
            ),
            modifier = modifier,
            style = PartloreTheme.typography.caption,
            color = PartloreTheme.colors.textSecondary,
        )
    }
}

/** Where a value comes from: the document, the place in it, and a link to read it. */
@Composable
internal fun SourceSheetContent(cite: Cite, sources: List<SourceItem>, modifier: Modifier = Modifier) {
    val source = sources.firstOrNull { it.id == cite.sourceId }
    val openLink = rememberLinkOpener()
    val colors = PartloreTheme.colors
    val typography = PartloreTheme.typography
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space8)) {
        SheetTitle(sourceTitle(cite, sources))
        Text(citePlace(cite).replaceFirstChar { it.uppercase() }, style = typography.monoM, color = colors.textPrimary)
        if (source != null) {
            Text(
                listOfNotNull(source.publisher, source.version).joinToString(" · "),
                style = typography.bodyM,
                color = colors.textSecondary,
            )
            Text(
                stringResource(R.string.part_retrieved, source.retrieved),
                style = typography.caption,
                color = colors.textSecondary,
            )
            Text(licenceText(source.license), style = typography.caption, color = colors.textSecondary)
            PlButton(stringResource(R.string.part_open_source), onClick = { openLink(source.url) })
        }
    }
}

@Composable
private fun SheetTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = PartloreTheme.typography.headline,
        color = PartloreTheme.colors.textPrimary,
        modifier = modifier.semantics { heading() },
    )
}
