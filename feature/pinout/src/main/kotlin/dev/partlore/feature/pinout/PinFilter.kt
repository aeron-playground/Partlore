package dev.partlore.feature.pinout

import androidx.annotation.StringRes
import dev.partlore.core.model.PinDirection
import dev.partlore.core.model.PinInfo
import dev.partlore.core.model.Pinout

enum class PinFilter(@StringRes val label: Int) {
    I2c(R.string.pinout_filter_i2c),
    Spi(R.string.pinout_filter_spi),
    Uart(R.string.pinout_filter_uart),
    Adc(R.string.pinout_filter_adc),
    Pwm(R.string.pinout_filter_pwm),
    Touch(R.string.pinout_filter_touch),
    Power(R.string.pinout_filter_power),
    Strapping(R.string.pinout_filter_strapping),
    InputOnly(R.string.pinout_filter_input_only),
    ;

    fun matches(pin: PinInfo): Boolean = when (this) {
        I2c -> pin.has("i2c")
        Spi -> pin.has("spi")
        Uart -> pin.has("uart")
        Adc -> pin.has("adc")
        Pwm -> pin.has("pwm")
        Touch -> pin.has("touch")
        Power -> pin.direction == PinDirection.Power || pin.direction == PinDirection.Ground
        Strapping -> pin.strapping != null
        InputOnly -> pin.direction == PinDirection.In
    }
}

private fun PinInfo.has(type: String) = functions.any { it.type == type }

/** The filters at least one pin matches; the others aren't shown. */
fun Pinout.filters(): List<PinFilter> = PinFilter.entries.filter { filter -> pins.any(filter::matches) }
