package dev.partlore.feature.pinout

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.content.ContentLinks
import dev.partlore.core.designsystem.components.PlButton
import dev.partlore.core.designsystem.components.PlTextButton
import dev.partlore.core.designsystem.components.rememberLinkOpener
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.PinDirection
import dev.partlore.core.model.PinInfo
import dev.partlore.core.model.PinSafety
import dev.partlore.core.model.Strapping
import dev.partlore.core.model.label

/**
 * Everything the data has about the open pin: in a sheet on phones, in a side panel on wide screens. It doesn't
 * scroll itself: the sheet already scrolls (a scroll inside a scroll can't be measured), the panel adds one.
 */
@Composable
internal fun PinDetails(state: PinoutState, modifier: Modifier = Modifier) {
    val pin = state.selected ?: return
    val spacing = PartloreTheme.spacing
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.space12),
    ) {
        Text(
            pin.label(state.labelMode).text,
            style = PartloreTheme.typography.headline,
            color = PartloreTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        Names(pin)
        Facts(pin)
        Functions(pin)
        pin.strapping?.let { StrappingNote(it) }
        Safety(pin)
        Gotchas(pin)
        SourcesAndReport(state, pin)
    }
}

@Composable
private fun Names(pin: PinInfo) {
    Section(R.string.pinout_names) {
        pin.chipName?.let { Fact(R.string.pinout_name_chip, it) }
        pin.modulePad?.let { Fact(R.string.pinout_name_module, it) }
        pin.boardLabel?.let { Fact(R.string.pinout_name_board, it) }
        pin.arduino?.let { Fact(R.string.pinout_name_arduino, it) }
        if (pin.aliases.isNotEmpty()) Fact(R.string.pinout_name_aliases, pin.aliases.joinToString(", "))
    }
}

@Composable
private fun Facts(pin: PinInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space4)) {
        Fact(R.string.pinout_direction, stringResource(pin.direction.wordRes()))
        pin.voltage?.let { Fact(R.string.pinout_voltage, stringResource(R.string.pinout_voltage_value, it.toString())) }
        pin.fiveVoltTolerant?.let {
            Fact(R.string.pinout_five_volt, stringResource(if (it) R.string.pinout_yes else R.string.pinout_no))
        }
    }
}

@Composable
private fun Functions(pin: PinInfo) {
    if (pin.functions.isEmpty()) return
    val default = stringResource(R.string.pinout_default)
    Section(R.string.pinout_functions) {
        pin.functions.forEach { f ->
            val text = listOfNotNull(f.type.uppercase(), f.signal, default.takeIf { f.isDefault }).joinToString(" · ")
            Text(text, style = PartloreTheme.typography.monoM, color = PartloreTheme.colors.textPrimary)
        }
    }
}

@Composable
private fun StrappingNote(strapping: Strapping) {
    // Only the two levels this app knows get a "must be"; any other value says the level matters, never a guess.
    val level =
        when (strapping.mustBe) {
            "high" -> R.string.pinout_level_high
            "low" -> R.string.pinout_level_low
            else -> null
        }
    val text =
        level?.let {
            stringResource(R.string.pinout_strapping_must_be, stringResource(it), strapping.at, strapping.role)
        }
            ?: stringResource(R.string.pinout_strapping_any, strapping.at, strapping.role)
    Section(R.string.pinout_strapping_title) {
        Text(text, style = PartloreTheme.typography.bodyM, color = PartloreTheme.colors.textPrimary)
    }
}

@Composable
private fun Safety(pin: PinInfo) {
    val word =
        when (pin.safe) {
            PinSafety.Caution -> R.string.pinout_safety_caution
            PinSafety.Avoid -> R.string.pinout_safety_avoid
            PinSafety.Ok -> null
        }
    if (word == null && pin.note == null) return
    Column {
        word?.let {
            Text(stringResource(it), style = PartloreTheme.typography.title, color = PartloreTheme.colors.warning)
        }
        pin.note?.let { Text(it, style = PartloreTheme.typography.bodyM, color = PartloreTheme.colors.textPrimary) }
    }
}

@Composable
private fun Gotchas(pin: PinInfo) {
    if (pin.gotchas.isEmpty()) return
    Section(R.string.pinout_gotchas) {
        pin.gotchas.forEach {
            Text("• ${it.title}", style = PartloreTheme.typography.bodyM, color = PartloreTheme.colors.textPrimary)
        }
    }
}

@Composable
private fun SourcesAndReport(state: PinoutState, pin: PinInfo) {
    val openLink = rememberLinkOpener()
    val sources = state.pinout.sources.associateBy { it.id }
    Section(R.string.pinout_sources) {
        pin.cites.forEach { cite ->
            val source = sources[cite.sourceId]
            val place = cite.page?.let { "p. $it" } ?: cite.section ?: cite.ref.orEmpty()
            PlTextButton("${source?.title ?: cite.sourceId} · $place", onClick = { source?.let { openLink(it.url) } })
        }
    }
    PlButton(
        stringResource(R.string.pinout_report),
        onClick = {
            openLink(ContentLinks.reportProblem(state.pinout.partId, state.pinout.packVersion, "pins.${pin.id}"))
        },
    )
}
