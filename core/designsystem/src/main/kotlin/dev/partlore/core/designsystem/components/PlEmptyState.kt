package dev.partlore.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import dev.partlore.core.designsystem.theme.PartloreTheme

/** One sentence and at most one action. Never an empty screen. */
@Composable
fun PlEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(PartloreTheme.spacing.space32),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = PartloreTheme.colors.textSecondary,
                modifier = Modifier.size(PartloreTheme.spacing.space48),
            )
        }
        Text(
            text = message,
            style = PartloreTheme.typography.bodyL,
            color = PartloreTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = PartloreTheme.spacing.space16),
        )
        if (actionLabel != null && onAction != null) {
            PlButton(actionLabel, onAction, Modifier.padding(top = PartloreTheme.spacing.space24))
        }
    }
}
