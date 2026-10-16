package dev.partlore.feature.pinout

import dev.partlore.core.model.PinInfo
import dev.partlore.core.model.PinLabelMode
import dev.partlore.core.model.Pinout
import dev.partlore.core.model.PlacedBoard
import dev.partlore.core.model.VerificationLevel

data class PinoutState(
    val pinout: Pinout,
    val board: PlacedBoard,
    val labelMode: PinLabelMode,
    val labelModes: List<PinLabelMode>,
    val filters: List<PinFilter>,
    val filter: PinFilter?,
    val selected: PinInfo?,
    val table: Boolean,
    val keepScreenOn: Boolean,
    val redLight: Boolean,
    val bench: Boolean,
    val haptics: Boolean,
) {
    /** Pins outside the chosen filter; empty when no filter is on. */
    val dimmed: Set<String>
        get() = filter?.let { f -> pinout.pins.filterNot(f::matches).mapTo(mutableSetOf()) { it.id } }.orEmpty()

    /** Only checked or verified pins skip the warning bar. */
    val pinsChecked: Boolean
        get() = pinout.status?.level == VerificationLevel.Checked || pinout.status?.level == VerificationLevel.Verified
}

sealed interface PinoutAction {
    data class Select(val pinId: String?) : PinoutAction

    data class Filter(val filter: PinFilter?) : PinoutAction

    data class Labels(val mode: PinLabelMode) : PinoutAction

    data object Rotate : PinoutAction

    data object ToggleTable : PinoutAction

    data object ToggleKeepScreenOn : PinoutAction

    data object ToggleRedLight : PinoutAction
}
