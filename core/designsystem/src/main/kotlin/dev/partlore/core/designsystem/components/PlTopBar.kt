package dev.partlore.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.R
import dev.partlore.core.designsystem.theme.PartloreTheme

/** Screen title row. `large` is for tab start pages; others get a Back button. */
@Composable
fun PlTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    large: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .heightIn(min = PartloreTheme.spacing.space64)
            .padding(horizontal = PartloreTheme.spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(PlIcons.Back),
                    contentDescription = stringResource(R.string.pl_back),
                    tint = PartloreTheme.colors.textPrimary,
                )
            }
        } else {
            Spacer(Modifier.width(PartloreTheme.spacing.space12))
        }
        Text(
            text = title,
            style = if (large) PartloreTheme.typography.displayM else PartloreTheme.typography.title,
            color = PartloreTheme.colors.textPrimary,
            maxLines = 2,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}
