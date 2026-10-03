package dev.partlore.app.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.partlore.app.R
import dev.partlore.core.designsystem.components.PlEmptyState
import dev.partlore.core.designsystem.components.PlIcons
import dev.partlore.core.designsystem.components.PlTopBar
import dev.partlore.core.designsystem.theme.PartloreTheme

// Start pages for each tab. Library, Search and Tools fill up in later build steps.

@Composable
fun LibraryHomeScreen(modifier: Modifier = Modifier) =
    TabHome(stringResource(R.string.tab_library), stringResource(R.string.library_empty), PlIcons.Library, modifier)

@Composable
fun SearchHomeScreen(modifier: Modifier = Modifier) =
    TabHome(stringResource(R.string.tab_search), stringResource(R.string.search_empty), PlIcons.Search, modifier)

@Composable
fun ToolsHomeScreen(modifier: Modifier = Modifier) =
    TabHome(stringResource(R.string.tab_tools), stringResource(R.string.tools_empty), PlIcons.Tools, modifier)

@Composable
fun BenchHomeScreen(onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(PartloreTheme.colors.bg)) {
        PlTopBar(title = stringResource(R.string.bench_title), large = true) {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    painter = painterResource(PlIcons.Settings),
                    contentDescription = stringResource(R.string.open_settings),
                    tint = PartloreTheme.colors.textPrimary,
                )
            }
        }
        PlEmptyState(message = stringResource(R.string.bench_empty), icon = PlIcons.Bench)
    }
}

@Composable
private fun TabHome(title: String, message: String, icon: Int, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(PartloreTheme.colors.bg)) {
        PlTopBar(title = title, large = true)
        PlEmptyState(message = message, icon = icon)
    }
}
