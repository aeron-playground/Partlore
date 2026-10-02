package dev.partlore.core.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import dev.partlore.core.designsystem.theme.PartloreStroke
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.namedColors
import dev.partlore.core.designsystem.theme.namedStyles
import dev.partlore.core.designsystem.theme.partloreElevation

private const val SAMPLE = "Every pin has a source."
private const val MONO_SAMPLE = "GPIO21 · 0x3C · 3.3 V · 0O"
private const val RGB_MASK = 0xFFFFFF

/** Every token in one scrolling page, for the debug catalog screen. */
@Composable
fun DesignCatalog(modifier: Modifier = Modifier) {
    Column(modifier.background(PartloreTheme.colors.bg).verticalScroll(rememberScrollState())) {
        ColorCatalog()
        TypographyCatalog()
        SpacingShapeCatalog()
        ElevationCatalog()
    }
}

@Composable
fun ColorCatalog(modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    CatalogSection("Colors", modifier) {
        colors.namedColors().forEach { (name, color) -> ColorRow(name, color) }
    }
}

@Composable
fun TypographyCatalog(modifier: Modifier = Modifier) {
    val typography = PartloreTheme.typography
    CatalogSection("Type", modifier) {
        typography.namedStyles().forEach { (name, style) ->
            Column(Modifier.padding(vertical = PartloreTheme.spacing.space4)) {
                Text(
                    "$name · ${style.fontSize}",
                    style = typography.caption,
                    color = PartloreTheme.colors.textSecondary,
                )
                Text(
                    if (name.startsWith("mono")) MONO_SAMPLE else SAMPLE,
                    style = style,
                    color = PartloreTheme.colors.textPrimary,
                )
            }
        }
    }
}

@Composable
fun SpacingShapeCatalog(modifier: Modifier = Modifier) {
    val spacing = PartloreTheme.spacing
    val shapes = PartloreTheme.shapes
    CatalogSection("Spacing and shape", modifier) {
        listOf(
            "space2" to spacing.space2, "space4" to spacing.space4, "space8" to spacing.space8,
            "space12" to spacing.space12, "space16" to spacing.space16, "space24" to spacing.space24,
            "space32" to spacing.space32, "space48" to spacing.space48, "space64" to spacing.space64,
        ).forEach { (name, size) -> SpacingRow(name, size) }
        Row(Modifier.padding(top = spacing.space16), horizontalArrangement = Arrangement.spacedBy(spacing.space12)) {
            listOf(shapes.xs, shapes.sm, shapes.md, shapes.lg, shapes.xl, shapes.full).forEach { ShapeSwatch(it) }
        }
    }
}

@Composable
fun ElevationCatalog(modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    val shape = PartloreTheme.shapes.md
    CatalogSection("Elevation", modifier) {
        PartloreTheme.elevation.all.forEach { elevation ->
            val surface = if (colors.isDark && elevation.level > 0) colors.surfaceRaised else colors.surface
            Box(
                Modifier
                    .padding(vertical = PartloreTheme.spacing.space12)
                    .fillMaxWidth()
                    .height(PartloreTheme.spacing.space64)
                    .partloreElevation(elevation, shape, colors)
                    .background(surface, shape),
                contentAlignment = Alignment.Center,
            ) {
                Text("e${elevation.level}", style = PartloreTheme.typography.label, color = colors.textPrimary)
            }
        }
    }
}

@Composable
private fun CatalogSection(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier.fillMaxWidth().background(PartloreTheme.colors.bg).padding(PartloreTheme.spacing.space16)) {
        Text(title, style = PartloreTheme.typography.headline, color = PartloreTheme.colors.textPrimary)
        Box(Modifier.height(PartloreTheme.spacing.space12))
        content()
    }
}

@Composable
private fun ColorRow(name: String, color: Color, modifier: Modifier = Modifier) {
    Row(modifier.padding(vertical = PartloreTheme.spacing.space4), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(PartloreTheme.spacing.space32)
                .background(color, PartloreTheme.shapes.sm)
                .border(PartloreStroke.hairline, PartloreTheme.colors.outline, PartloreTheme.shapes.sm),
        )
        Column(Modifier.padding(start = PartloreTheme.spacing.space12)) {
            Text(name, style = PartloreTheme.typography.bodyM, color = PartloreTheme.colors.textPrimary)
            Text(
                "#%06X".format(color.toArgb() and RGB_MASK),
                style = PartloreTheme.typography.monoS,
                color = PartloreTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun SpacingRow(name: String, size: Dp, modifier: Modifier = Modifier) {
    Row(modifier.padding(vertical = PartloreTheme.spacing.space2), verticalAlignment = Alignment.CenterVertically) {
        Text(
            name,
            style = PartloreTheme.typography.monoS,
            color = PartloreTheme.colors.textSecondary,
            maxLines = 1,
            modifier =
            Modifier
                .widthIn(min = PartloreTheme.spacing.space64 + PartloreTheme.spacing.space16)
                .padding(end = PartloreTheme.spacing.space8),
        )
        Box(Modifier.width(size).height(PartloreTheme.spacing.space8).background(PartloreTheme.colors.primary))
    }
}

@Composable
private fun ShapeSwatch(shape: Shape, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(PartloreTheme.spacing.space48)
            .background(PartloreTheme.colors.surfaceSunken, shape)
            .border(PartloreStroke.hairline, PartloreTheme.colors.outline, shape),
    )
}
