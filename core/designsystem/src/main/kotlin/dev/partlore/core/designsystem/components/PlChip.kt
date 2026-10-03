package dev.partlore.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import dev.partlore.core.designsystem.theme.PartloreStroke
import dev.partlore.core.designsystem.theme.PartloreTheme

/** A choice that can be on or off. A checkbox for TalkBack; the touch target is at least 48 dp. */
@Composable
fun PlChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    val shape = PartloreTheme.shapes.xs
    Box(
        modifier =
        modifier
            .minimumInteractiveComponentSize()
            .clip(shape)
            .background(if (selected) colors.primary else colors.surface)
            .border(PartloreStroke.hairline, if (selected) colors.primary else colors.outline, shape)
            .selectable(selected = selected, onClick = onClick, role = Role.Checkbox)
            .padding(horizontal = PartloreTheme.spacing.space12, vertical = PartloreTheme.spacing.space4),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = PartloreTheme.typography.label,
            color = if (selected) colors.onPrimary else colors.textPrimary,
        )
    }
}
