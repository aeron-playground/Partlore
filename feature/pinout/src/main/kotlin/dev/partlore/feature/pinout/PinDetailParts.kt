package dev.partlore.feature.pinout

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.PinDirection

/** One name and value row of the pin details. */
@Composable
internal fun Fact(@StringRes name: Int, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space12)) {
        Text(
            stringResource(name),
            style = PartloreTheme.typography.bodyM,
            color = PartloreTheme.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = PartloreTheme.typography.monoM,
            color = PartloreTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

/** A titled part of the pin details; the title is a heading for TalkBack. */
@Composable
internal fun Section(@StringRes title: Int, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space4)) {
        Text(
            stringResource(title),
            style = PartloreTheme.typography.title,
            color = PartloreTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        content()
    }
}

internal fun PinDirection.wordRes(): Int = when (this) {
    PinDirection.Io -> R.string.pinout_direction_io
    PinDirection.In -> R.string.pinout_direction_in
    PinDirection.Out -> R.string.pinout_direction_out
    PinDirection.Power -> R.string.pinout_direction_power
    PinDirection.Ground -> R.string.pinout_direction_ground
    PinDirection.NotConnected -> R.string.pinout_direction_nc
    PinDirection.Unknown -> R.string.pinout_direction_unknown
}
