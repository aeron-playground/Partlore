package dev.partlore.tools.content.rules

import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.model.Content
import dev.partlore.tools.content.model.GotchaFile
import dev.partlore.tools.content.model.Header
import dev.partlore.tools.content.model.Pin
import dev.partlore.tools.content.model.PinFile

/** Every pin sits in exactly one existing place, and every place has a pin. */
object PinLayoutRules {
    fun check(content: Content, d: Diagnostics) = content.parts.forEach { part ->
        part.pins?.let { layout(it, d) }
        part.gotchas?.let { gotchaPins(it, part.pins, d) }
    }

    private fun layout(file: PinFile, d: Diagnostics) {
        reportDuplicates(file.headers, { it.id }, { it.pos }, "header", d)
        reportDuplicates(file.pins, { it.id }, { it.pos }, "pin", d)
        val headers = file.headers.associateBy { it.id }
        val taken = mutableMapOf<Triple<String, Int, Int>, Pin>()
        file.pins.forEach { pin -> place(pin, headers[pin.header], taken, d) }
        file.headers.forEach { header -> missing(header, taken.keys, d) }
    }

    private fun place(pin: Pin, header: Header?, taken: MutableMap<Triple<String, Int, Int>, Pin>, d: Diagnostics) {
        when {
            header == null -> d.error(pin.pos, "pin ${pin.id}: header \"${pin.header}\" is not in headers")

            pin.row > header.rows || pin.index > header.pinsPerRow ->
                d.error(
                    pin.pos,
                    "pin ${pin.id}: row ${pin.row}, index ${pin.index} is outside header ${header.id} " +
                        "(${header.rows} × ${header.pinsPerRow})",
                )

            else ->
                taken.putIfAbsent(Triple(pin.header, pin.row, pin.index), pin)?.let { first ->
                    d.error(pin.pos, "pin ${pin.id} is in the same place as ${first.id}")
                }
        }
    }

    private fun missing(header: Header, taken: Set<Triple<String, Int, Int>>, d: Diagnostics) {
        for (row in 1..header.rows) {
            for (index in 1..header.pinsPerRow) {
                if (Triple(header.id, row, index) !in taken) {
                    d.error(header.pos, "header ${header.id} has no pin at row $row, index $index")
                }
            }
        }
    }

    private fun gotchaPins(file: GotchaFile, pins: PinFile?, d: Diagnostics) {
        reportDuplicates(file.gotchas, { it.id }, { it.pos }, "gotcha", d)
        val ids = pins?.pins?.map { it.id }?.toSet().orEmpty()
        file.gotchas.forEach { gotcha ->
            gotcha.pins.filter { it !in ids }.forEach {
                d.error(gotcha.pos, "gotcha ${gotcha.id}: pin \"$it\" is not in pins.yaml")
            }
        }
    }
}
