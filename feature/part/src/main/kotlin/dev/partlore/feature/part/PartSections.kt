package dev.partlore.feature.part

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.components.PlCard
import dev.partlore.core.designsystem.components.PlGotchaCard
import dev.partlore.core.designsystem.components.PlSpecRow
import dev.partlore.core.designsystem.components.PlSpecTable
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.PartPage
import dev.partlore.core.model.PartRef
import dev.partlore.core.model.SourceItem
import dev.partlore.core.model.SpecRow

@Composable
internal fun PartSectionBlock(
    section: PartSection,
    page: PartPage,
    actions: PartActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space12)) {
        Text(
            stringResource(section.title),
            style = PartloreTheme.typography.headline,
            color = PartloreTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        when (section) {
            PartSection.Specs -> SpecsBlock(page, actions)
            PartSection.I2c -> I2cBlock(page, actions)
            PartSection.Gotchas -> GotchasBlock(page, actions)
            PartSection.Related -> RelatedBlock(page, actions)
            PartSection.Sources -> SourcesBlock(page, actions)
        }
    }
}

@Composable
private fun SpecsBlock(page: PartPage, actions: PartActions, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space12)) {
        if (page.specs.isNotEmpty()) {
            PlSpecTable(
                rows = page.specs.map { specRow(it, page.sources) },
                onShowSource = { actions.showSource(page.specs[it].cite) },
                onCopy = { actions.copy(formatSpec(page.specs[it])) },
            )
        }
        if (page.absoluteMax.isNotEmpty()) {
            PlSpecTable(
                rows = page.absoluteMax.map { specRow(it, page.sources) },
                onShowSource = { actions.showSource(page.absoluteMax[it].cite) },
                onCopy = { actions.copy(formatSpec(page.absoluteMax[it])) },
                title = stringResource(R.string.part_absolute_max),
                danger = true,
            )
            Text(
                stringResource(R.string.part_absolute_max_note),
                style = PartloreTheme.typography.caption,
                color = PartloreTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun specRow(row: SpecRow, sources: List<SourceItem>): PlSpecRow =
    PlSpecRow(row.label, formatSpec(row), row.condition, sourceDescription(row.cite, sources))

@Composable
private fun I2cBlock(page: PartPage, actions: PartActions, modifier: Modifier = Modifier) {
    val rows =
        page.i2c.map { row ->
            PlSpecRow(
                label = hexAddress(row.address),
                value = stringResource(if (row.isDefault) R.string.part_i2c_default else R.string.part_i2c_other),
                note = row.select,
                sourceDescription = sourceDescription(row.cite, page.sources),
            )
        }
    PlSpecTable(
        rows = rows,
        onShowSource = { actions.showSource(page.i2c[it].cite) },
        onCopy = { actions.copy(hexAddress(page.i2c[it].address)) },
        modifier = modifier,
    )
}

@Composable
private fun GotchasBlock(page: PartPage, actions: PartActions, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space12)) {
        page.gotchas.forEach { gotcha ->
            PlGotchaCard(
                title = gotcha.title,
                body = gotcha.body,
                severity = gotcha.severity,
                pins = gotcha.pins,
                sources = gotcha.cites.map { citeChip(it, page.sources) },
                onSource = { actions.showSource(gotcha.cites[it]) },
            )
        }
    }
}

@Composable
private fun RelatedBlock(page: PartPage, actions: PartActions, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space16)) {
        page.uses?.let { RelatedGroup(stringResource(R.string.part_uses), listOf(it), actions) }
        if (page.related.isNotEmpty()) RelatedGroup(stringResource(R.string.part_similar), page.related, actions)
        if (page.usedBy.isNotEmpty()) RelatedGroup(stringResource(R.string.part_used_by), page.usedBy, actions)
    }
}

@Composable
private fun RelatedGroup(title: String, parts: List<PartRef>, actions: PartActions, modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space8)) {
        Text(title, style = PartloreTheme.typography.label, color = colors.textSecondary)
        parts.forEach { ref ->
            PlCard(Modifier.fillMaxWidth().heightIn(min = PartloreLayout.touchTarget), onClick = {
                actions.openPart(ref.id)
            }) {
                Text(ref.name, style = PartloreTheme.typography.title, color = colors.textPrimary)
            }
        }
    }
}
