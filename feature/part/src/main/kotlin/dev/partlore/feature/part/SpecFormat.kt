package dev.partlore.feature.part

import dev.partlore.core.model.SpecRow
import java.math.BigDecimal
import java.util.Locale

/** The words for a spec value: "3 – 3.6 V (typ 3.3)", "≤ 240 mA", "3.3 V", or the text as written. */
internal fun formatSpec(row: SpecRow): String = row.text ?: formatNumbers(row)

private fun formatNumbers(row: SpecRow): String {
    val unit = row.unit?.let { " $it" }.orEmpty()
    val number = row.number
    val min = row.min
    val max = row.max
    val typ = row.typ?.let(::plainNumber)
    val range =
        when {
            number != null -> plainNumber(number)
            min != null && max != null -> "${plainNumber(min)} – ${plainNumber(max)}"
            min != null -> "≥ ${plainNumber(min)}"
            max != null -> "≤ ${plainNumber(max)}"
            else -> null
        }
    return when {
        range == null && typ == null -> ""
        range == null -> "$typ$unit"
        typ == null || number != null -> "$range$unit"
        else -> "$range$unit (typ $typ)"
    }
}

/** 3.0 → "3", 3.30 → "3.3", 2.4E8 → "240000000": no trailing zeros and no exponent. */
internal fun plainNumber(value: Double): String = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()

/** I²C addresses are written in hex: 60 → "0x3C". */
internal fun hexAddress(address: Int): String = String.format(Locale.ROOT, "0x%02X", address)
