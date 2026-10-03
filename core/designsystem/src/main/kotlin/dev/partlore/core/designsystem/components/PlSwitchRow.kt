package dev.partlore.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import dev.partlore.core.designsystem.theme.PartloreTheme

/** A setting with a switch. The whole row is the touch target. */
@Composable
fun PlSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .heightIn(min = PartloreTheme.spacing.space48)
            .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Switch)
            .padding(vertical = PartloreTheme.spacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = PartloreTheme.spacing.space12)) {
            Text(title, style = PartloreTheme.typography.bodyM, color = PartloreTheme.colors.textPrimary)
            if (subtitle != null) {
                Text(subtitle, style = PartloreTheme.typography.caption, color = PartloreTheme.colors.textSecondary)
            }
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}
