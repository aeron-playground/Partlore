package dev.partlore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.partlore.core.designsystem.catalog.DesignCatalog
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.PartloreThemeMode

/** Debug builds only: every design token, with a light / dark / bench switch. */
class DesignCatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CatalogScreen() }
    }
}

@Composable
private fun CatalogScreen(modifier: Modifier = Modifier) {
    var mode by rememberSaveable { mutableStateOf(PartloreThemeMode.Light) }
    PartloreTheme(mode = mode) {
        Column(modifier.fillMaxSize().background(PartloreTheme.colors.bg).safeDrawingPadding()) {
            Row(
                Modifier.padding(PartloreTheme.spacing.space8),
                horizontalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space8),
            ) {
                PartloreThemeMode.entries.forEach { option ->
                    FilterChip(selected = option == mode, onClick = { mode = option }, label = { Text(option.name) })
                }
            }
            DesignCatalog()
        }
    }
}
