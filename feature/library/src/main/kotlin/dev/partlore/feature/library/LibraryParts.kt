package dev.partlore.feature.library

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.PartCard

internal const val HOW_TO_ADD_URL = "https://github.com/aeron-playground/Partlore/blob/main/content/README.md"

/** The short line under a part's name: its tags, for example "Bluetooth · 3.3 V logic · Wi-Fi". */
internal fun PartCard.line(): String = tags.joinToString(" · ") { it.label }

/** Which placeholder drawing a category gets until categories have their own art. */
internal fun artKindFor(categoryId: String): String = if ("board" in categoryId) "board" else "module"

@Composable
internal fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = PartloreTheme.typography.title,
        color = PartloreTheme.colors.textPrimary,
        modifier = modifier.padding(top = PartloreTheme.spacing.space8).semantics { heading() },
    )
}
