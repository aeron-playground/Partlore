package dev.partlore.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreTheme

private const val DIMMED_ALPHA = 0.35f

/** A pin in the table view: dot, name and its functions. Reads like the board's pin. */
@Composable
fun PlPinRow(
    pin: PlBoardPin,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
) {
    val colors = PartloreTheme.colors
    val alpha by animateFloatAsState(if (dimmed) DIMMED_ALPHA else 1f, PartloreTheme.motion.fade(), label = "dim")
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = PartloreLayout.pinRow)
            .graphicsLayer { this.alpha = alpha }
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = pin.description }
            .padding(horizontal = PartloreTheme.spacing.space16, vertical = PartloreTheme.spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space12),
    ) {
        PlPinDot(pin.kind, strapping = pin.strapping, caution = pin.caution, inputOnly = pin.inputOnly)
        Column(Modifier.weight(1f)) {
            // The row's description already says these words; TalkBack reads it once.
            Text(
                pin.label,
                style = PartloreTheme.typography.monoM,
                color = if (pin.labelMuted) colors.textSecondary else colors.textPrimary,
                modifier = Modifier.clearAndSetSemantics {},
            )
            if (detail.isNotEmpty()) {
                Text(
                    detail,
                    style = PartloreTheme.typography.caption,
                    color = colors.textSecondary,
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }
        }
    }
}
