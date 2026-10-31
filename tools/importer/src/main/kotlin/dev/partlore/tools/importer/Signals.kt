package dev.partlore.tools.importer

/** One function of a pin as pins.yaml writes it: a type and, for most types, the signal name. */
data class Fn(val type: String, val signal: String?)

/** What a guide's Type and Function cells say about a pin. [flags] are words a person must look at. */
data class PinFacts(val chipName: String?, val direction: String, val functions: List<Fn>, val flags: List<String>)

/**
 * Turns the Type and Function cells of a guide's pin row into pins.yaml facts, written the way the
 * checked files write them. A signal it doesn't know stops the import: it never guesses a type.
 */
object Signals {
    private val DIRECTIONS =
        mapOf("P" to "power", "G" to "ground", "I" to "in", "O" to "out", "I/O" to "io", "I/O/T" to "io")

    // First match wins.
    private val TYPES =
        listOf(
            Regex("""GPIO\d+""") to "gpio",
            Regex("""RTC_GPIO\d+""") to "rtc",
            Regex("""ADC\d_CH\d+""") to "adc",
            Regex("""DAC_\d""") to "dac",
            Regex("""TOUCH(_CH)?\d+""") to "touch",
            Regex("""U\d(TXD|RXD|RTS|CTS)""") to "uart",
            Regex("""(SUB|F|H|V)?SPI\w+""") to "spi",
            Regex("""MT(MS|DI|DO|CK)""") to "jtag",
            Regex("""CLK_OUT\d|XTAL_32K_[PN]""") to "clock",
            Regex("""USB_D[+-]""") to "usb",
            Regex("""CHIP_PU|EN|Reset""") to "reset",
            Regex("""Boot""") to "boot",
        )

    // The checked files write these without a signal name. GPIOn is the pin's chip name instead.
    private val WITHOUT_SIGNAL = setOf("Boot", "Reset")

    // Words that only describe a power or ground pin.
    private val SUPPLY_WORDS = setOf("3.3 V power supply", "5 V power supply", "Ground")

    // Known, and left out on purpose like the checked files do: module pad names, the ESP32's VDET inputs.
    private val NOT_MODELLED = setOf("S_VP", "S_VN", "VDET_1", "VDET_2")

    // Not a function: something on the board (an LED) or a flash-chip line. A person decides what to write.
    private val FLAGGED = setOf("RGB LED", "CLK", "CMD", "D0", "D1", "D2", "D3")

    fun read(type: String, function: String): PinFacts {
        val direction = DIRECTIONS[type] ?: throw ImportException("unknown pin type '$type'")
        val chipNames = mutableListOf<String>()
        val functions = mutableListOf<Fn>()
        val flags = mutableListOf<String>()
        for (token in function.split(",").map(String::trim).filter(String::isNotEmpty)) {
            when (token) {
                in SUPPLY_WORDS ->
                    if (direction != "power" && direction != "ground") {
                        throw ImportException("'$token' describes a supply pin, but the pin type is '$type'")
                    }

                in NOT_MODELLED -> Unit

                in FLAGGED -> flags += token

                else -> {
                    val fn = function(token, function)
                    if (fn.type == "gpio") chipNames += token
                    functions += fn
                }
            }
        }
        if (chipNames.size > 1) throw ImportException("two GPIO names in '$function'")
        // A bare "Reset" next to CHIP_PU or EN only describes it.
        val reset = functions.any { it.type == "reset" && it.signal != null }
        val kept = functions.filterNot { reset && it.type == "reset" && it.signal == null }
        return PinFacts(chipNames.singleOrNull(), direction, kept.sortedBy { if (it.type == "gpio") 0 else 1 }, flags)
    }

    private fun function(token: String, cell: String): Fn {
        val type =
            TYPES.firstOrNull { it.first.matches(token) }?.second
                ?: throw ImportException("unknown signal '$token' in '$cell'")
        return Fn(type, token.takeUnless { type == "gpio" || it in WITHOUT_SIGNAL })
    }
}
