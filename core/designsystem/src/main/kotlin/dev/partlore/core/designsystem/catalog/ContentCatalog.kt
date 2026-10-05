package dev.partlore.core.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.partlore.core.designsystem.components.PlCategoryTile
import dev.partlore.core.designsystem.components.PlDangerBanner
import dev.partlore.core.designsystem.components.PlErrorState
import dev.partlore.core.designsystem.components.PlGlanceGrid
import dev.partlore.core.designsystem.components.PlGlanceItem
import dev.partlore.core.designsystem.components.PlGotchaCard
import dev.partlore.core.designsystem.components.PlPartCard
import dev.partlore.core.designsystem.components.PlSearchField
import dev.partlore.core.designsystem.components.PlSectionChips
import dev.partlore.core.designsystem.components.PlSpecRow
import dev.partlore.core.designsystem.components.PlSpecTable
import dev.partlore.core.designsystem.components.PlStatusBadge
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.ContentProblem
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.Severity
import dev.partlore.core.model.VerificationLevel

/** The components that show content, with made-up sample values. */
@Composable
fun ContentCatalog(modifier: Modifier = Modifier) {
    val spacing = PartloreTheme.spacing
    Column(
        modifier.fillMaxWidth().background(PartloreTheme.colors.bg).padding(spacing.space16),
        verticalArrangement = Arrangement.spacedBy(spacing.space12),
    ) {
        ListSamples()
        PartPageSamples()
        PlErrorState(ContentResult.Failed(ContentProblem.Damaged, "sample"))
    }
}

/** What Library and category lists show: search, status badges, part cards and a category tile. */
@Composable
private fun ListSamples(modifier: Modifier = Modifier) {
    val spacing = PartloreTheme.spacing
    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing.space12)) {
        PlSearchField("Search parts, pins, I²C addresses…", onClick = {})
        // Badges wrap to the next line instead of squeezing the last one.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.space8),
            verticalArrangement = Arrangement.spacedBy(spacing.space8),
        ) {
            VerificationLevel.entries.forEach { level ->
                PlStatusBadge(level, checkers = if (level == VerificationLevel.Verified) 2 else 1)
            }
        }
        PlPartCard(
            name = "Example DevBoard V1",
            line = "Wi-Fi · Bluetooth · 3.3 V logic",
            kind = "board",
            level = VerificationLevel.Draft,
            onClick = {},
        )
        PlPartCard(
            name = "Example Classic Board",
            line = "5 V logic · USB-B",
            kind = "board",
            level = VerificationLevel.Checked,
            onClick = {},
            compact = true,
        )
        // A category tile as it sits in the Library's two-column grid.
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space12)) {
            PlCategoryTile("Modules", "1 part", "module", onClick = {}, modifier = Modifier.weight(1f))
            Spacer(Modifier.weight(1f))
        }
    }
}

/** What a Part page shows: at a glance, a danger banner, section chips, spec tables and gotchas. */
@Composable
private fun PartPageSamples(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space12)) {
        PlGlanceGrid(
            listOf(
                PlGlanceItem("Logic", "3.3 V"),
                PlGlanceItem("5 V tolerant", "No"),
                PlGlanceItem("Radios", "Wi-Fi · BT"),
                PlGlanceItem("GPIO", "26"),
                PlGlanceItem("ADC", "16"),
                PlGlanceItem("Danger", "1", danger = true),
            ),
        )
        PlDangerBanner("Power the board from one source only", "USB, the 5V pin or the 3V3 pin: never two at once.")
        PlSectionChips(listOf("Specs", "Gotchas", "Related", "Sources"), selectedIndex = 0, onSelect = {})
        PlSpecTable(
            rows =
            listOf(
                PlSpecRow("Supply voltage", "3 – 3.6 V (typ 3.3)", null, "Source: datasheet, page 28"),
                PlSpecRow("Input voltage", "5 V", "USB or the 5V pin", "Source: guide, section Power options"),
            ),
            onShowSource = {},
            onCopy = {},
        )
        PlSpecTable(
            rows = listOf(PlSpecRow("Supply voltage", "-0.3 – 3.6 V", null, "Source: datasheet, page 28")),
            onShowSource = {},
            onCopy = {},
            title = "Absolute maximum",
            danger = true,
        )
        PlGotchaCard(
            "Keep IO0 high at reset",
            "Low at reset starts download mode.",
            Severity.Caution,
            pins = listOf("IO0"),
            sources = listOf("Datasheet · p. 13"),
        )
        PlGotchaCard("Boot plus Reset starts download mode", "Hold Boot and press Reset.", Severity.Info)
    }
}
