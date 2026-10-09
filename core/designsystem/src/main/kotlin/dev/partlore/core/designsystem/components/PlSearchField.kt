package dev.partlore.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreStroke
import dev.partlore.core.designsystem.theme.PartloreTheme

/** Looks like a search field; tapping it opens the Search tab. */
@Composable
fun PlSearchField(hint: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .heightIn(min = PartloreLayout.touchTarget)
            .clip(PartloreTheme.shapes.full)
            .background(colors.surfaceSunken)
            // In the Bench theme the sunken fill is almost the page colour; the outline keeps the field visible.
            .border(PartloreStroke.hairline, colors.outline, PartloreTheme.shapes.full)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = PartloreTheme.spacing.space16),
        horizontalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(PlIcons.Search), contentDescription = null, tint = colors.textSecondary)
        Text(
            hint,
            style = PartloreTheme.typography.bodyM,
            color = colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
