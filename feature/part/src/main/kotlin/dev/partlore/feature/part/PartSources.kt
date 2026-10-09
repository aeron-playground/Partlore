package dev.partlore.feature.part

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.partlore.core.designsystem.components.PlButton
import dev.partlore.core.designsystem.components.PlCard
import dev.partlore.core.designsystem.components.PlStatusBadge
import dev.partlore.core.designsystem.components.PlTextButton
import dev.partlore.core.designsystem.components.rememberLinkOpener
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.FileStatus
import dev.partlore.core.model.PartPage
import dev.partlore.core.model.SourceItem

/** Every source with its licence, how far each file is checked, and the ways to fix a mistake. */
@Composable
internal fun SourcesBlock(page: PartPage, actions: PartActions, modifier: Modifier = Modifier) {
    val openLink = rememberLinkOpener()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space12)) {
        page.sources.forEach { source -> SourceCard(source, onOpen = { openLink(source.url) }) }
        FileStatusRow(stringResource(R.string.part_file_part), page.partStatus, page.sources, actions.showStatus)
        page.pinsStatus?.let {
            FileStatusRow(stringResource(R.string.part_file_pins), it, page.sources, actions.showStatus)
        }
        page.gotchasStatus?.let {
            FileStatusRow(stringResource(R.string.part_file_gotchas), it, page.sources, actions.showStatus)
        }
        PlButton(
            stringResource(R.string.part_report_problem),
            onClick = { openLink(GithubLinks.reportProblem(page.id, page.packVersion)) },
        )
        PlTextButton(stringResource(R.string.part_edit_on_github), onClick = {
            openLink(GithubLinks.editOnGithub(page.id))
        })
        Text(
            stringResource(R.string.part_pack_version, page.packVersion),
            style = PartloreTheme.typography.caption,
            color = PartloreTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun SourceCard(source: SourceItem, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    val typography = PartloreTheme.typography
    PlCard(modifier.fillMaxWidth(), onClick = onOpen) {
        Text(source.title, style = typography.title, color = colors.textPrimary)
        Text(
            listOfNotNull(source.publisher, source.version).joinToString(" · "),
            style = typography.caption,
            color = colors.textSecondary,
        )
        Text(licenceText(source.license), style = typography.caption, color = colors.textSecondary)
    }
}

/** One file's level, and who checked it, when and against what. */
@Composable
private fun FileStatusRow(
    label: String,
    status: FileStatus,
    sources: List<SourceItem>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space4)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = PartloreTheme.typography.bodyM,
                color = PartloreTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            PlStatusBadge(status.level, onClick = onClick)
        }
        CheckedLine(status, sources)
    }
}
