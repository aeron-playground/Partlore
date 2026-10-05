package dev.partlore.core.designsystem.components

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dev.partlore.core.designsystem.R
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.ContentProblem
import dev.partlore.core.model.ContentResult
import kotlinx.coroutines.launch

/** Content can't be shown: plain words, what to do next, and the details to copy. Never a stack trace. */
@Composable
fun PlErrorState(failure: ContentResult.Failed, modifier: Modifier = Modifier, appVersion: String? = null) {
    val colors = PartloreTheme.colors
    val spacing = PartloreTheme.spacing
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val details =
        "Content problem: ${failure.problem}\n${failure.detail}" + appVersion?.let { "\nApp version: $it" }.orEmpty()
    Column(
        modifier.fillMaxWidth().padding(spacing.space32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space16),
    ) {
        PlGlyphIcon(PlGlyph.Alert, colors.danger, Modifier.size(spacing.space48), cutout = colors.bg)
        Text(
            text = stringResource(
                if (failure.problem ==
                    ContentProblem.TooNew
                ) {
                    R.string.pl_content_too_new
                } else {
                    R.string.pl_content_error
                },
            ),
            style = PartloreTheme.typography.bodyL,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.pl_content_error_help),
            style = PartloreTheme.typography.bodyM,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        PlButton(stringResource(R.string.pl_copy_details), onClick = {
            scope.launch { clipboard.setClipEntry(ClipData.newPlainText("Partlore", details).toClipEntry()) }
        })
    }
}
