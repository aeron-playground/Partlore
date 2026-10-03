package dev.partlore.core.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import dev.partlore.core.designsystem.components.PlButton
import dev.partlore.core.designsystem.components.PlCard
import dev.partlore.core.designsystem.components.PlChip
import dev.partlore.core.designsystem.components.PlEmptyState
import dev.partlore.core.designsystem.components.PlIcons
import dev.partlore.core.designsystem.components.PlSegmented
import dev.partlore.core.designsystem.components.PlSwitchRow
import dev.partlore.core.designsystem.components.PlTextButton
import dev.partlore.core.designsystem.components.PlTopBar
import dev.partlore.core.designsystem.theme.PartloreTheme

@Composable
fun ComponentsCatalog(modifier: Modifier = Modifier) {
    val spacing = PartloreTheme.spacing
    Column(modifier.fillMaxWidth().background(PartloreTheme.colors.bg)) {
        PlTopBar(title = "Library", large = true)
        PlTopBar(title = "Settings", onBack = {})
        Column(Modifier.padding(spacing.space16), verticalArrangement = Arrangement.spacedBy(spacing.space12)) {
            PlCard(Modifier.fillMaxWidth()) {
                Text("ESP32 DevKitC", style = PartloreTheme.typography.title, color = PartloreTheme.colors.textPrimary)
                Text(
                    "Card with e1 elevation",
                    style = PartloreTheme.typography.caption,
                    color = PartloreTheme.colors.textSecondary,
                )
            }
            PlSegmented(listOf("System", "Light", "Dark", "Bench"), selectedIndex = 1, onSelect = {})
            PlSwitchRow(title = "Haptics", checked = true, onCheckedChange = {})
            PlSwitchRow(title = "Sounds", subtitle = "Off by default", checked = false, onCheckedChange = {})
            FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.space8)) {
                PlChip("ESP32", selected = true, onClick = {})
                PlChip("Arduino", selected = false, onClick = {})
                PlChip("Pico", selected = true, onClick = {})
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.space8)) {
                PlButton("Continue", onClick = {})
                PlTextButton("Skip", onClick = {})
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.space16)) {
                val icons =
                    listOf(
                        PlIcons.Library,
                        PlIcons.Search,
                        PlIcons.Tools,
                        PlIcons.Bench,
                        PlIcons.Settings,
                        PlIcons.Back,
                        PlIcons.Close,
                    )
                icons.forEach {
                    Icon(painterResource(it), contentDescription = null, tint = PartloreTheme.colors.textPrimary)
                }
            }
            PlEmptyState(
                message = "Nothing saved yet. Tap ★ on any part.",
                icon = PlIcons.Bench,
                actionLabel = "Browse the library",
                onAction = {},
            )
        }
    }
}
