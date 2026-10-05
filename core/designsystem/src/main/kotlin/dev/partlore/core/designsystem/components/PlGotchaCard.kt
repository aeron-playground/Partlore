package dev.partlore.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import dev.partlore.core.designsystem.R
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.Severity

/** A trap the datasheet doesn't make obvious, with the pins it's about and where it's written down. */
@Composable
fun PlGotchaCard(
    title: String,
    body: String,
    severity: Severity,
    modifier: Modifier = Modifier,
    pins: List<String> = emptyList(),
    sources: List<String> = emptyList(),
    onSource: (Int) -> Unit = {},
) {
    val colors = PartloreTheme.colors
    val spacing = PartloreTheme.spacing
    val background = if (colors.isDark) colors.surfaceRaised else colors.surface
    val (accent, glyph, word) =
        when (severity) {
            Severity.Danger -> Triple(colors.danger, PlGlyph.Alert, R.string.pl_severity_danger)
            Severity.Caution -> Triple(colors.warning, PlGlyph.Half, R.string.pl_severity_caution)
            Severity.Info -> Triple(colors.info, PlGlyph.Dot, R.string.pl_severity_info)
        }
    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(PartloreTheme.shapes.md).background(background)) {
        Box(Modifier.width(PartloreLayout.accentBar).fillMaxHeight().background(accent))
        Column(
            Modifier.weight(1f).padding(spacing.space12),
            verticalArrangement = Arrangement.spacedBy(spacing.space8),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space4),
            ) {
                PlGlyphIcon(glyph, accent, cutout = background)
                Text(stringResource(word), style = PartloreTheme.typography.label, color = accent)
            }
            Text(title, style = PartloreTheme.typography.title, color = colors.textPrimary)
            Text(body, style = PartloreTheme.typography.bodyM, color = colors.textPrimary)
            if (pins.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.space4),
                    verticalArrangement = Arrangement.spacedBy(spacing.space4),
                ) {
                    pins.forEach { pin ->
                        Text(
                            pin,
                            style = PartloreTheme.typography.monoS,
                            color = colors.textPrimary,
                            modifier =
                            Modifier
                                .clip(PartloreTheme.shapes.xs)
                                .background(colors.surfaceSunken)
                                .padding(horizontal = spacing.space8, vertical = spacing.space2),
                        )
                    }
                }
            }
            if (sources.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.space4)) {
                    sources.forEachIndexed { index, label -> PlTextButton(label, onClick = { onSource(index) }) }
                }
            }
        }
    }
}
