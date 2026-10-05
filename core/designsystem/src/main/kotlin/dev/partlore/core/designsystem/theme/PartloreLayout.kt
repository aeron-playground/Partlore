package dev.partlore.core.designsystem.theme

import androidx.compose.ui.unit.dp

/** Sizes that aren't spacing: a breakpoint and fixed component sizes. */
object PartloreLayout {
    /** From this width up, screens use their expanded layout (for example 4-column grids). */
    val expandedWidth = 600.dp
    val artSmall = 48.dp
    val heroArtHeight = 112.dp

    // Wide enough for a one-line "Not checked yet" badge; two cards and a peek of the third fit a phone.
    val starterCardWidth = 176.dp
    val sourceDot = 8.dp
    val glyph = 14.dp
    val accentBar = 4.dp
    val touchTarget = 48.dp
}
