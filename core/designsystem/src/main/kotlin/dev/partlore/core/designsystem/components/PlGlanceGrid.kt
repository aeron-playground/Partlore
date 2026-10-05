package dev.partlore.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreTheme

private const val COMPACT_COLUMNS = 3
private const val EXPANDED_COLUMNS = 6
private const val LARGE_TEXT_COLUMNS = 2

// From 200 % text (as the spec table) a value like "Bluetooth" doesn't fit a third of a phone.
private const val LARGE_FONT_SCALE = 2f

/** One fact in the at-a-glance strip. [danger] shows the value in the danger colour. */
data class PlGlanceItem(
    val label: String,
    val value: String,
    val danger: Boolean = false,
    val onClick: (() -> Unit)? = null,
)

/** The key facts of a part in a grid: 3 columns on phones, 6 on wide screens. */
@Composable
fun PlGlanceGrid(items: List<PlGlanceItem>, modifier: Modifier = Modifier) {
    val spacing = PartloreTheme.spacing
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val expanded = maxWidth >= PartloreLayout.expandedWidth
        val largeText = LocalDensity.current.fontScale >= LARGE_FONT_SCALE
        val columns =
            when {
                expanded && largeText -> COMPACT_COLUMNS
                expanded -> EXPANDED_COLUMNS
                largeText -> LARGE_TEXT_COLUMNS
                else -> COMPACT_COLUMNS
            }
        Column(verticalArrangement = Arrangement.spacedBy(spacing.space8)) {
            items.chunked(columns).forEach { row ->
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(spacing.space8)) {
                    row.forEach { GlanceCell(it, Modifier.weight(1f).fillMaxHeight()) }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun GlanceCell(item: PlGlanceItem, modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    val onClick = item.onClick
    Column(
        modifier
            .clip(PartloreTheme.shapes.sm)
            .background(if (colors.isDark) colors.surfaceRaised else colors.surface)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(PartloreTheme.spacing.space8)
            .semantics(mergeDescendants = true) { contentDescription = "${item.label}: ${item.value}" },
    ) {
        Text(item.label, style = PartloreTheme.typography.caption, color = colors.textSecondary, maxLines = 2)
        Text(
            item.value,
            style = PartloreTheme.typography.monoM,
            color = if (item.danger) colors.danger else colors.textPrimary,
        )
    }
}
