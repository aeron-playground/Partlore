package dev.partlore.core.model

/** A pin's name in one label mode; [fallback] when the pin has no name of that kind and another is shown. */
data class PinLabel(val text: String, val fallback: Boolean)

private val FALLBACK_ORDER = listOf(PinLabelMode.Board, PinLabelMode.Module, PinLabelMode.Chip)
private val DEFAULT_ORDER = listOf(PinLabelMode.Board, PinLabelMode.Arduino, PinLabelMode.Module, PinLabelMode.Chip)

fun PinInfo.name(mode: PinLabelMode): String? = when (mode) {
    PinLabelMode.Chip -> chipName
    PinLabelMode.Module -> modulePad
    PinLabelMode.Board -> boardLabel
    PinLabelMode.Arduino -> arduino
}

/** The name in [mode], or the next one the pin has (board → module → chip → id), marked as a fallback. */
fun PinInfo.label(mode: PinLabelMode): PinLabel = name(mode)?.let { PinLabel(it, fallback = false) }
    ?: PinLabel(FALLBACK_ORDER.firstNotNullOfOrNull { name(it) } ?: id, fallback = true)

/** The modes at least one pin has a name in, in switch order (chip, module, board, Arduino). */
fun Pinout.labelModes(): List<PinLabelMode> = PinLabelMode.entries.filter { mode -> pins.any { it.name(mode) != null } }

/** [preferred] when this part has it; otherwise the first of board, Arduino, module, chip that it has. */
fun Pinout.labelMode(preferred: PinLabelMode?): PinLabelMode {
    val modes = labelModes()
    return preferred?.takeIf { it in modes } ?: DEFAULT_ORDER.firstOrNull { it in modes } ?: PinLabelMode.Chip
}
