package dev.partlore.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** WCAG 2 contrast ratio between two opaque colors: 1.0 (same) to 21.0 (black on white). */
fun contrastRatio(foreground: Color, background: Color): Float {
    val a = foreground.luminance() + 0.05f
    val b = background.luminance() + 0.05f
    return maxOf(a, b) / minOf(a, b)
}
