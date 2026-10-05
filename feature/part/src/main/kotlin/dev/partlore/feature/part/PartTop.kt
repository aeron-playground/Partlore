package dev.partlore.feature.part

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.components.PlDangerBanner
import dev.partlore.core.designsystem.components.PlGlanceGrid
import dev.partlore.core.designsystem.components.PlGlanceItem
import dev.partlore.core.designsystem.components.PlPartArt
import dev.partlore.core.designsystem.components.PlStatusBadge
import dev.partlore.core.designsystem.components.PlTextButton
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.PartPage
import dev.partlore.core.model.Severity

/** Hero, at a glance and the danger banners. */
@Composable
internal fun PartTop(page: PartPage, actions: PartActions, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space16)) {
        Hero(page, actions)
        val glance = glanceItems(page, onDanger = { actions.goTo(PartSection.Gotchas) })
        if (glance.isNotEmpty()) PlGlanceGrid(glance)
        page.gotchas.filter { it.severity == Severity.Danger }.forEach { PlDangerBanner(it.title, it.body) }
    }
}

@Composable
private fun Hero(page: PartPage, actions: PartActions, modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    val typography = PartloreTheme.typography
    val weakest = page.weakestStatus()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space8)) {
        PlPartArt(page.kind, Modifier.fillMaxWidth().height(PartloreLayout.heroArtHeight))
        Text(
            page.name,
            style = typography.displayM,
            color = colors.textPrimary,
            modifier = Modifier.semantics {
                heading()
            },
        )
        if (page.aliases.isNotEmpty()) {
            Text(
                stringResource(R.string.part_also_called, page.aliases.joinToString(", ")),
                style = typography.bodyM,
                color = colors.textSecondary,
            )
        }
        Text(
            "${page.manufacturer} · ${stringResource(kindLabel(page.kind))}",
            style = typography.bodyM,
            color = colors.textSecondary,
        )
        page.uses?.let { base ->
            PlTextButton(stringResource(R.string.part_built_on, base.name), onClick = { actions.openPart(base.id) })
        }
        PlStatusBadge(weakest.level, checkers = weakest.checkedBy.size, onClick = actions.showStatus)
        Text(page.summary, style = typography.bodyL, color = colors.textPrimary)
    }
}

/** Only the facts the part has; the danger count jumps to the gotchas. */
@Composable
private fun glanceItems(page: PartPage, onDanger: () -> Unit): List<PlGlanceItem> {
    val glance = page.glance
    val radios =
        listOfNotNull(
            glance.wifi?.let { stringResource(R.string.part_wifi) },
            glance.bluetooth?.let { stringResource(R.string.part_bluetooth) },
        )
    return listOfNotNull(
        glance.logicLevelV?.let { PlGlanceItem(stringResource(R.string.part_glance_logic), "${plainNumber(it)} V") },
        glance.fiveVoltTolerant?.let {
            PlGlanceItem(stringResource(R.string.part_glance_five_volt), stringResource(it.label()))
        },
        radios.takeIf {
            it.isNotEmpty()
        }?.let { PlGlanceItem(stringResource(R.string.part_glance_radios), it.joinToString(" · ")) },
        glance.gpioCount?.let { PlGlanceItem(stringResource(R.string.part_glance_gpio), it.toString()) },
        glance.adcChannels?.let { PlGlanceItem(stringResource(R.string.part_glance_adc), it.toString()) },
        page.dangerCount.takeIf { it > 0 }?.let {
            PlGlanceItem(stringResource(R.string.part_glance_danger), it.toString(), danger = true, onClick = onDanger)
        },
    )
}
