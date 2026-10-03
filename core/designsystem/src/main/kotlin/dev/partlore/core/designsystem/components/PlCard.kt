package dev.partlore.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.partloreElevation

@Composable
fun PlCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val colors = PartloreTheme.colors
    val shape = PartloreTheme.shapes.md
    Column(
        modifier =
        modifier
            .partloreElevation(PartloreTheme.elevation.e1, shape, colors)
            .clip(shape)
            .background(if (colors.isDark) colors.surfaceRaised else colors.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(PartloreTheme.spacing.space12),
        content = content,
    )
}
