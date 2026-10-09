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

    /** A pin's row in the pinout: a full touch target. */
    val pinRow = 48.dp

    /** Bench mode: pins one size bigger. */
    val pinRowLarge = 56.dp
    val pinDot = 30.dp
    val pinDotLarge = 36.dp
    val pinRing = 2.dp
    val pinDash = 4.dp
    val pinBadge = 14.dp
    val pinDetailsWidth = 360.dp
    val pinoutPreviewHeight = 240.dp
    const val PIN_MIN_ZOOM = 0.5f
    const val PIN_MAX_ZOOM = 3f
    const val PIN_DOUBLE_TAP_ZOOM = 2f
}
