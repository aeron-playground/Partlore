package dev.partlore.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.partlore.core.designsystem.R
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.PinKind

/** What each colour, glyph and mark means. */
@Composable
fun PlPinLegend(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space8)) {
        PinKind.entries.forEach { kind -> LegendRow(stringResource(kind.nameRes())) { PlPinDot(kind) } }
        LegendRow(stringResource(R.string.pl_pin_strapping)) { PlPinDot(PinKind.Gpio, strapping = true) }
        LegendRow(stringResource(R.string.pl_pin_caution)) { PlPinDot(PinKind.Gpio, caution = true) }
        LegendRow(stringResource(R.string.pl_pin_input_only)) { PlPinDot(PinKind.Gpio, inputOnly = true) }
    }
}

@Composable
private fun LegendRow(name: String, modifier: Modifier = Modifier, dot: @Composable () -> Unit) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space12),
    ) {
        dot()
        Text(name, style = PartloreTheme.typography.bodyM, color = PartloreTheme.colors.textPrimary)
    }
}

internal fun PinKind.nameRes(): Int = when (this) {
    PinKind.Power -> R.string.pl_pin_power
    PinKind.Ground -> R.string.pl_pin_ground
    PinKind.I2c -> R.string.pl_pin_i2c
    PinKind.Spi -> R.string.pl_pin_spi
    PinKind.Uart -> R.string.pl_pin_uart
    PinKind.Adc -> R.string.pl_pin_adc
    PinKind.Pwm -> R.string.pl_pin_pwm
    PinKind.Touch -> R.string.pl_pin_touch
    PinKind.DoNotUse -> R.string.pl_pin_do_not_use
    PinKind.Gpio -> R.string.pl_pin_gpio
}
