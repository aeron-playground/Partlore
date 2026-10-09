package dev.partlore.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.R
import dev.partlore.core.designsystem.theme.PartloreTheme

/** A danger gotcha, pinned above the specs. */
@Composable
fun PlDangerBanner(title: String, body: String, modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    val spacing = PartloreTheme.spacing
    Column(
        modifier
            .fillMaxWidth()
            .clip(PartloreTheme.shapes.md)
            .background(colors.dangerContainer)
            .padding(spacing.space12)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(spacing.space4),
    ) {
        // The same header as a danger gotcha card: an icon and the word, so colour is never the only signal.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.space4),
        ) {
            PlGlyphIcon(PlGlyph.Alert, colors.danger, cutout = colors.dangerContainer)
            Text(
                stringResource(R.string.pl_severity_danger),
                style = PartloreTheme.typography.label,
                color = colors.danger,
            )
        }
        Text(title, style = PartloreTheme.typography.title, color = colors.danger)
        Text(body, style = PartloreTheme.typography.bodyM, color = colors.textPrimary)
    }
}
