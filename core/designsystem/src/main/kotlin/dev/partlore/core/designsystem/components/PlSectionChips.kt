package dev.partlore.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import dev.partlore.core.designsystem.theme.PartloreSprings
import dev.partlore.core.designsystem.theme.PartloreStroke
import dev.partlore.core.designsystem.theme.PartloreTheme

/** "On this page": one tab per section; the current section is highlighted. */
@Composable
fun PlSectionChips(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PartloreTheme.spacing
    Row(
        modifier
            .fillMaxWidth()
            .background(PartloreTheme.colors.bg)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = spacing.space16, vertical = spacing.space4)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(spacing.space8),
    ) {
        labels.forEachIndexed { index, label ->
            SectionChip(label, index == selectedIndex, onClick = { onSelect(index) })
        }
    }
}

@Composable
private fun SectionChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    val shape = PartloreTheme.shapes.xs
    val background by animateColorAsState(
        if (selected) colors.primaryContainer else colors.surface,
        PartloreTheme.motion.spring(PartloreSprings.snap),
        label = "section",
    )
    Box(
        modifier
            .minimumInteractiveComponentSize()
            .clip(shape)
            .background(background)
            .border(PartloreStroke.hairline, if (selected) colors.primaryContainer else colors.outline, shape)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .padding(horizontal = PartloreTheme.spacing.space12, vertical = PartloreTheme.spacing.space4),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = PartloreTheme.typography.label,
            color = if (selected) colors.primary else colors.textPrimary,
        )
    }
}
