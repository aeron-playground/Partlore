package dev.partlore.tools.content.checklist

import dev.partlore.tools.content.model.Citation
import dev.partlore.tools.content.model.Header
import dev.partlore.tools.content.model.I2cAddress
import dev.partlore.tools.content.model.Part
import dev.partlore.tools.content.model.Pin
import dev.partlore.tools.content.model.Scalar
import dev.partlore.tools.content.model.SpecValue
import java.math.BigDecimal

internal fun items(part: Part): List<Item> = buildList {
    part.specs.forEach { add(Item(it.cite, "part.yaml", it.pos.line, "spec ${describe(it)}")) }
    part.absoluteMax.forEach { add(Item(it.cite, "part.yaml", it.pos.line, "absolute max ${describe(it)}")) }
    part.i2c.forEach { add(Item(it.cite, "part.yaml", it.pos.line, "I²C address ${describe(it)}")) }
    part.pins?.headers?.forEach { add(Item(it.cite, "pins.yaml", it.pos.line, "header ${describe(it)}")) }
    part.pins?.pins?.forEach { pin ->
        pin.cites.forEach { add(Item(it, "pins.yaml", pin.pos.line, "pin ${describe(pin)}")) }
    }
    part.gotchas?.gotchas?.forEach { g ->
        g.cites.forEach { add(Item(it, "gotchas.yaml", g.pos.line, "gotcha ${g.id} (${g.severity}): ${g.title}")) }
    }
}

/** 3.0 → "3", 2.54 → "2.54". */
internal fun number(value: Double): String = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()

private fun describe(spec: SpecValue): String {
    val values =
        listOfNotNull(
            spec.min?.let { "min ${number(it)}" },
            spec.typ?.let { "typ ${number(it)}" },
            spec.max?.let { "max ${number(it)}" },
            spec.value?.let { "value ${scalar(it)}" },
        )
    val unit = spec.unit?.let { " $it" }.orEmpty()
    val condition = spec.condition?.let { " ($it)" }.orEmpty()
    return "${spec.key}: ${values.joinToString(", ")}$unit$condition"
}

private fun describe(address: I2cAddress): String =
    address.text + (if (address.isDefault) " (default)" else "") + address.select?.let { ", $it" }.orEmpty()

private fun describe(header: Header): String =
    "${header.id} \"${header.label}\": ${header.type}, ${header.rows} × ${header.pinsPerRow}, " +
        "pitch ${number(header.pitchMm)} mm"

private fun describe(pin: Pin): String = listOfNotNull(
    "${pin.id} at ${pin.header} row ${pin.row} index ${pin.index}",
    "names ${pin.names.joinToString(" / ")}",
    pin.direction + pin.voltage?.let { " ${number(it)} V" }.orEmpty(),
    pin.fiveVTolerant?.let { if (it) "5 V tolerant" else "not 5 V tolerant" },
    pin.functions.takeIf { it.isNotEmpty() }?.joinToString(", ") { f ->
        f.type + f.signal?.let { " $it" }.orEmpty() + if (f.isDefault) " (default)" else ""
    },
    pin.strapping?.let { "strapping ${it.role}: ${it.mustBe} at ${it.at}" },
    "safe ${pin.safe}",
).joinToString(" · ")

private fun scalar(value: Scalar): String = when (value) {
    is Scalar.Text -> value.value
    is Scalar.Num -> number(value.value)
    is Scalar.Bool -> value.value.toString()
}
