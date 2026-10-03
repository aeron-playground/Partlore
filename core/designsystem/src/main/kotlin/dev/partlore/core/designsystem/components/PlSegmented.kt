package dev.partlore.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import dev.partlore.core.designsystem.theme.PartloreTheme

/**
 * One choice out of a few, side by side. Each option is a radio button for TalkBack.
 *
 * When a label can't fit its share of the row without breaking a word (large text, narrow
 * phone), the options stack, one per line, instead.
 */
@Composable
fun PlSegmented(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    val shape = PartloreTheme.shapes.sm
    Layout(
        content = {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Box(
                    modifier =
                    Modifier
                        .heightIn(min = PartloreTheme.spacing.space48)
                        .clip(shape)
                        .background(if (selected) colors.surface else Color.Transparent)
                        .selectable(selected = selected, onClick = { onSelect(index) }, role = Role.RadioButton)
                        .padding(horizontal = PartloreTheme.spacing.space4),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = PartloreTheme.typography.label,
                        color = if (selected) colors.textPrimary else colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
        modifier =
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceSunken)
            .padding(PartloreTheme.spacing.space2)
            .selectableGroup(),
        measurePolicy = { measurables, constraints -> segmentedLayout(measurables, constraints) },
    )
}

private fun MeasureScope.segmentedLayout(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
    val width = constraints.maxWidth
    val share = width / measurables.size.coerceAtLeast(1)
    val fitsInRow = measurables.all { it.maxIntrinsicWidth(Constraints.Infinity) <= share }
    return if (fitsInRow) {
        val height = measurables.maxOf { it.minIntrinsicHeight(share) }
        val placeables =
            measurables.mapIndexed { index, measurable ->
                // The last option takes the pixels left over by the division.
                val optionWidth = if (index == measurables.lastIndex) width - share * index else share
                measurable.measure(Constraints.fixed(optionWidth, height))
            }
        layout(width, height) {
            placeables.forEachIndexed { index, placeable -> placeable.placeRelative(share * index, 0) }
        }
    } else {
        val placeables = measurables.map { it.measure(Constraints.fixedWidth(width)) }
        layout(width, placeables.sumOf { it.height }) {
            var y = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(0, y)
                y += placeable.height
            }
        }
    }
}
