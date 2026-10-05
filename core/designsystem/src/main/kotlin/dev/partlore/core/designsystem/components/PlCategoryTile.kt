package dev.partlore.core.designsystem.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreTheme

/** A category in the Library grid: art, name and how many parts it holds. */
@Composable
fun PlCategoryTile(
    name: String,
    countLabel: String,
    artKind: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlCard(modifier, onClick = onClick) {
        PlPartArt(artKind, Modifier.size(PartloreLayout.artSmall))
        Spacer(Modifier.height(PartloreTheme.spacing.space8))
        Text(
            name,
            style = PartloreTheme.typography.title,
            color = PartloreTheme.colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(countLabel, style = PartloreTheme.typography.caption, color = PartloreTheme.colors.textSecondary)
    }
}
