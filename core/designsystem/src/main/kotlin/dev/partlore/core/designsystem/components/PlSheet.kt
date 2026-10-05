package dev.partlore.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.partlore.core.designsystem.theme.PartloreTheme

/** A bottom sheet over the current page. Back or a swipe down closes it; predictive back is built in. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlSheet(onDismiss: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val spacing = PartloreTheme.spacing
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = PartloreTheme.colors.surfaceRaised,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(start = spacing.space16, end = spacing.space16, bottom = spacing.space32),
            verticalArrangement = Arrangement.spacedBy(spacing.space12),
            content = content,
        )
    }
}
