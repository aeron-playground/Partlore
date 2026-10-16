package dev.partlore.feature.pinout

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.components.PlChip
import dev.partlore.core.designsystem.components.PlPinLegend
import dev.partlore.core.designsystem.components.PlSegmented
import dev.partlore.core.designsystem.components.PlSheet
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.PinLabelMode

/** The label switch and the tools: rotate, table, keep screen on, red light, key. */
@Composable
internal fun Controls(state: PinoutState, onAction: (PinoutAction) -> Unit, modifier: Modifier = Modifier) {
    var key by remember { mutableStateOf(false) }
    val spacing = PartloreTheme.spacing
    Column(
        modifier.padding(start = spacing.space16, top = spacing.space12, end = spacing.space16),
        verticalArrangement = Arrangement.spacedBy(spacing.space8),
    ) {
        if (state.labelModes.size > 1) {
            PlSegmented(
                options = state.labelModes.map { stringResource(it.labelRes()) },
                selectedIndex = state.labelModes.indexOf(state.labelMode),
                onSelect = { onAction(PinoutAction.Labels(state.labelModes[it])) },
            )
        }
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spacing.space8),
        ) {
            Tool(R.string.pinout_tool_rotate, on = false) { onAction(PinoutAction.Rotate) }
            Tool(R.string.pinout_tool_table, state.table) { onAction(PinoutAction.ToggleTable) }
            Tool(R.string.pinout_tool_screen_on, state.keepScreenOn) { onAction(PinoutAction.ToggleKeepScreenOn) }
            Tool(R.string.pinout_tool_red_light, state.redLight) { onAction(PinoutAction.ToggleRedLight) }
            Tool(R.string.pinout_tool_key, key) { key = !key }
        }
    }
    if (key) {
        PlSheet(onDismiss = { key = false }) {
            Text(
                stringResource(R.string.pinout_key_title),
                style = PartloreTheme.typography.headline,
                color = PartloreTheme.colors.textPrimary,
            )
            PlPinLegend()
        }
    }
}

/** Filter chips at the bottom, in thumb reach. One at a time; tapping the chosen one clears it. */
@Composable
internal fun Filters(state: PinoutState, onAction: (PinoutAction) -> Unit, modifier: Modifier = Modifier) {
    if (state.filters.isEmpty()) return
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(PartloreTheme.spacing.space16),
        horizontalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space8),
    ) {
        state.filters.forEach { filter ->
            val on = filter == state.filter
            val name = stringResource(filter.label)
            val count = state.pinout.pins.count(filter::matches)
            // TalkBack hears how many pins a filter lights up: "I²C, 2 pins".
            val described = pluralStringResource(R.plurals.pinout_filter_count, count, name, count)
            PlChip(
                name,
                on,
                { onAction(PinoutAction.Filter(if (on) null else filter)) },
                Modifier.semantics { contentDescription = described },
            )
        }
    }
}

@Composable
internal fun UncheckedWarning(modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    Text(
        stringResource(R.string.pinout_unchecked),
        style = PartloreTheme.typography.bodyM,
        color = colors.textPrimary,
        modifier = modifier.fillMaxWidth().background(colors.warningContainer).padding(PartloreTheme.spacing.space12),
    )
}

@Composable
internal fun TableOnlyNote(modifier: Modifier = Modifier) {
    Text(
        stringResource(R.string.pinout_table_only),
        style = PartloreTheme.typography.caption,
        color = PartloreTheme.colors.textSecondary,
        modifier = modifier.padding(horizontal = PartloreTheme.spacing.space16),
    )
}

@Composable
private fun Tool(@StringRes name: Int, on: Boolean, onClick: () -> Unit) = PlChip(stringResource(name), on, onClick)

private fun PinLabelMode.labelRes(): Int = when (this) {
    PinLabelMode.Chip -> R.string.pinout_labels_chip
    PinLabelMode.Module -> R.string.pinout_labels_module
    PinLabelMode.Board -> R.string.pinout_labels_board
    PinLabelMode.Arduino -> R.string.pinout_labels_arduino
}
