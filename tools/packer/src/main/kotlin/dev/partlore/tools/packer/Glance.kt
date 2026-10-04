package dev.partlore.tools.packer

import dev.partlore.tools.content.model.Part
import dev.partlore.tools.content.model.Pin
import dev.partlore.tools.content.model.Scalar

/** The at-a-glance strip, worked out once so it can never disagree with the pin table. */
internal object Glance {
    data class Values(
        val logicLevelV: Double?,
        val fiveVTolerant: String?,
        val gpioCount: Int?,
        val adcChannels: Int?,
        val wifi: String?,
        val bluetooth: String?,
    )

    private val INPUTS = setOf("io", "in")

    fun of(part: Part, pinsShip: Boolean): Values {
        val pins = part.pins?.pins?.takeIf { pinsShip }
        return Values(
            logicLevelV = (spec(part, "io.logic_level") as? Scalar.Num)?.value,
            fiveVTolerant = pins?.let { tolerance(it) },
            gpioCount = pins?.count { pin -> pin.functions.any { it.type == "gpio" } },
            adcChannels = pins?.count { pin -> pin.functions.any { it.type == "adc" } },
            wifi = (spec(part, "radio.wifi") as? Scalar.Text)?.value,
            bluetooth = (spec(part, "radio.bluetooth") as? Scalar.Text)?.value,
        )
    }

    private fun spec(part: Part, key: String): Scalar? = part.specs.firstOrNull { it.key == key }?.value

    private fun tolerance(pins: List<Pin>): String? {
        val inputs = pins.filter { it.direction in INPUTS }
        val tolerant = inputs.count { it.fiveVTolerant == true }
        return when {
            inputs.isEmpty() -> null
            tolerant == inputs.size -> "all"
            tolerant == 0 -> "none"
            else -> "some"
        }
    }
}
