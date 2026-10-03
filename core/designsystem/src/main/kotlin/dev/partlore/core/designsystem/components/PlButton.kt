package dev.partlore.core.designsystem.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.partlore.core.designsystem.theme.PartloreTheme

@Composable
fun PlButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = PartloreTheme.spacing.space48),
        shape = PartloreTheme.shapes.xl,
    ) {
        Text(text, style = PartloreTheme.typography.label)
    }
}

@Composable
fun PlTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.heightIn(min = PartloreTheme.spacing.space48)) {
        Text(text, style = PartloreTheme.typography.label, color = PartloreTheme.colors.primary)
    }
}
