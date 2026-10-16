package dev.partlore.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import dev.partlore.core.designsystem.R
import dev.partlore.core.designsystem.theme.PinKind
import dev.partlore.core.model.PinDirection
import dev.partlore.core.model.PinInfo
import dev.partlore.core.model.PinLabelMode
import dev.partlore.core.model.PinSafety
import dev.partlore.core.model.Pinout
import dev.partlore.core.model.label
import dev.partlore.core.model.name

/** What the board needs to draw one pin; [description] is what TalkBack reads. */
data class PlBoardPin(
    val id: String,
    val label: String,
    val labelMuted: Boolean,
    val description: String,
    val kind: PinKind,
    val strapping: Boolean,
    val caution: Boolean,
    val inputOnly: Boolean,
)

private val FUNCTION_KINDS =
    listOf(
        "i2c" to PinKind.I2c,
        "spi" to PinKind.Spi,
        "uart" to PinKind.Uart,
        "adc" to PinKind.Adc,
        "pwm" to PinKind.Pwm,
        "touch" to PinKind.Touch,
    )

/** Power and ground by direction, "avoid" pins as do-not-use, else the first of I²C, SPI, UART, ADC, PWM, Touch. */
fun PinInfo.pinKind(): PinKind = when {
    direction == PinDirection.Power -> PinKind.Power
    direction == PinDirection.Ground -> PinKind.Ground
    safe == PinSafety.Avoid -> PinKind.DoNotUse
    else -> FUNCTION_KINDS.firstOrNull { (type, _) -> functions.any { it.type == type } }?.second ?: PinKind.Gpio
}

/** Every pin of [pinout] ready to draw, named in [mode]. */
@Composable
fun rememberBoardPins(pinout: Pinout, mode: PinLabelMode): Map<String, PlBoardPin> {
    val marks =
        PinMarks(
            strapping = stringResource(R.string.pl_pin_strapping),
            caution = stringResource(R.string.pl_pin_caution),
            avoid = stringResource(R.string.pl_pin_do_not_use),
            inputOnly = stringResource(R.string.pl_pin_input_only),
        )
    return remember(pinout, mode, marks) { pinout.pins.associate { it.id to it.boardPin(mode, marks) } }
}

private data class PinMarks(val strapping: String, val caution: String, val avoid: String, val inputOnly: String)

// Read like "D4. GPIO2. ADC2_CH2. Strapping pin. Caution.": the shown name, the other names, functions, marks.
private fun PinInfo.boardPin(mode: PinLabelMode, marks: PinMarks): PlBoardPin {
    val label = label(mode)
    val names = PinLabelMode.entries.mapNotNull { name(it) }.distinct().filter { it != label.text }
    val signals = functions.filter { it.type != "gpio" }.joinToString(", ") { it.signal ?: it.type.uppercase() }
    val flags =
        listOfNotNull(
            marks.strapping.takeIf { strapping != null },
            marks.inputOnly.takeIf { direction == PinDirection.In },
            marks.caution.takeIf { safe == PinSafety.Caution },
            marks.avoid.takeIf { safe == PinSafety.Avoid },
        )
    val description = (
        listOf(label.text) + names + listOf(signals).filter {
            it.isNotEmpty()
        } + flags
        ).joinToString(". ")
    return PlBoardPin(
        id = id,
        label = label.text,
        labelMuted = label.fallback,
        description = description,
        kind = pinKind(),
        strapping = strapping != null,
        caution = safe == PinSafety.Caution,
        inputOnly = direction == PinDirection.In,
    )
}
