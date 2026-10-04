package dev.partlore.tools.content.rules

import dev.partlore.tools.content.diag.Diagnostics
import dev.partlore.tools.content.model.Content
import dev.partlore.tools.content.model.Pin
import dev.partlore.tools.content.model.SpecValue

/** Rules whose violation could mislead someone into wiring something wrong. */
object SafetyRules {
    private val NO_FUNCTIONS = setOf("power", "ground", "nc")

    // Signal pins need a voltage; a supply pin like VIN may accept a range, which part.yaml gives.
    private val SIGNALS = setOf("io", "in", "out")
    private val INPUTS = setOf("io", "in")

    fun check(content: Content, d: Diagnostics) = content.parts.forEach { part ->
        part.pins?.pins?.forEach { pin -> pinProblems(pin).forEach { d.error(pin.pos, it) } }
        (part.specs + part.absoluteMax).forEach { value -> rangeProblem(value)?.let { d.error(value.pos, it) } }
    }

    private fun pinProblems(pin: Pin): List<String> = buildList {
        if (pin.names.isEmpty()) {
            add("pin ${pin.id} needs at least one name (chip_name, module_pad, board_label or label_aliases)")
        }
        if (pin.strapping != null && pin.safe == "ok") {
            add("pin ${pin.id} is a strapping pin, so it can't be \"safe: ok\"")
        }
        if (pin.direction in NO_FUNCTIONS && pin.functions.isNotEmpty()) {
            add("pin ${pin.id} is ${pin.direction}: it can't have functions")
        }
        if (pin.direction in SIGNALS && pin.voltage == null) add("pin ${pin.id} needs a voltage")
        if (pin.direction in INPUTS && pin.fiveVTolerant == null) add("pin ${pin.id} needs five_v_tolerant")
    }

    private fun rangeProblem(value: SpecValue): String? {
        val (min, typ, max) = Triple(value.min, value.typ, value.max)
        return when {
            min != null && max != null && min > max -> "${value.key}: min $min is above max $max"
            min != null && typ != null && min > typ -> "${value.key}: min $min is above typ $typ"
            typ != null && max != null && typ > max -> "${value.key}: typ $typ is above max $max"
            else -> null
        }
    }
}
