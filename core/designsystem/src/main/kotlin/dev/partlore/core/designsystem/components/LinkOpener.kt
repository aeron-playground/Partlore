package dev.partlore.core.designsystem.components

import android.content.ClipData
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import dev.partlore.core.designsystem.R
import kotlinx.coroutines.launch

/**
 * Opens a link in the browser. Android throws IllegalArgumentException when no app on the phone can
 * open links; then the link is copied and a short message says so, instead of the app crashing.
 * Use this for every link instead of calling LocalUriHandler directly.
 */
@Composable
fun rememberLinkOpener(): (String) -> Unit {
    val uri = LocalUriHandler.current
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val noBrowser = stringResource(R.string.pl_no_browser)
    return remember(uri, context, clipboard, scope, noBrowser) {
        { url ->
            try {
                uri.openUri(url)
            } catch (_: IllegalArgumentException) {
                scope.launch { clipboard.setClipEntry(ClipData.newPlainText(url, url).toClipEntry()) }
                Toast.makeText(context, noBrowser, Toast.LENGTH_LONG).show()
            }
        }
    }
}
