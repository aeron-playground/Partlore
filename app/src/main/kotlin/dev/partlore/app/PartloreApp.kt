package dev.partlore.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.partlore.core.designsystem.theme.PartloreTheme

/** Placeholder root until the app shell exists. */
@Composable
fun PartloreApp(modifier: Modifier = Modifier) {
    PartloreTheme {
        Box(
            modifier.fillMaxSize().background(
                PartloreTheme.colors.bg,
            ).safeDrawingPadding().padding(PartloreTheme.spacing.space16),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.app_name),
                    style = PartloreTheme.typography.displayM,
                    color = PartloreTheme.colors.textPrimary,
                )
                Text(
                    stringResource(R.string.placeholder_status),
                    style = PartloreTheme.typography.bodyM,
                    color = PartloreTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = PartloreTheme.spacing.space8),
                )
            }
        }
    }
}

@Preview
@Composable
private fun PartloreAppPreview() {
    PartloreApp()
}
