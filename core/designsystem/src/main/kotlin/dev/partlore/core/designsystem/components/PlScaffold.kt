package dev.partlore.core.designsystem.components

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import dev.partlore.core.designsystem.theme.PartloreTheme

/** Bottom bar on phones, navigation rail on wider screens (chosen from the window size). */
@Composable
fun PlScaffold(
    items: List<PlNavItem>,
    modifier: Modifier = Modifier,
    showNavigation: Boolean = true,
    content: @Composable () -> Unit,
) {
    // A full-screen page (the pinout viewer) hides the bar or rail; otherwise the scaffold's own choice.
    val layout = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfoV2())
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            items.forEach { item ->
                item(
                    selected = item.selected,
                    onClick = item.onClick,
                    icon = { Icon(painterResource(item.icon), contentDescription = null) },
                    label = { Text(item.label, style = PartloreTheme.typography.caption) },
                )
            }
        },
        modifier = modifier,
        layoutType = if (showNavigation) layout else NavigationSuiteType.None,
        containerColor = PartloreTheme.colors.bg,
        content = content,
    )
}
