package dev.partlore.core.designsystem.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.R
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreTheme

private const val LABEL_WEIGHT = 0.42f
private const val VALUE_WEIGHT = 0.58f

// At 200 % text a two-column row has no room: each row becomes a small stacked card.
private const val STACKED_FONT_SCALE = 2f

/** One spec row. [sourceDescription] is read by TalkBack, for example "Source: …, page 28". */
data class PlSpecRow(val label: String, val value: String, val note: String?, val sourceDescription: String)

/** Spec values with a source dot on each row: tap shows the source, long-press copies the value. */
@Composable
fun PlSpecTable(
    rows: List<PlSpecRow>,
    onShowSource: (Int) -> Unit,
    onCopy: (Int) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    danger: Boolean = false,
) {
    val colors = PartloreTheme.colors
    val stacked = LocalDensity.current.fontScale >= STACKED_FONT_SCALE
    Column(
        modifier
            .fillMaxWidth()
            .clip(PartloreTheme.shapes.md)
            .background(if (colors.isDark) colors.surfaceRaised else colors.surface),
    ) {
        if (title != null) {
            Text(
                text = title,
                style = PartloreTheme.typography.title,
                color = if (danger) colors.danger else colors.textPrimary,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .background(if (danger) colors.dangerContainer else colors.surfaceSunken)
                    .padding(horizontal = PartloreTheme.spacing.space12, vertical = PartloreTheme.spacing.space8)
                    .semantics { heading() },
            )
        }
        rows.forEachIndexed { index, row ->
            SpecRowItem(row, stacked, zebra = index % 2 == 1, onClick = {
                onShowSource(index)
            }, onLongClick = { onCopy(index) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpecRowItem(
    row: PlSpecRow,
    stacked: Boolean,
    zebra: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PartloreTheme.colors
    val spacing = PartloreTheme.spacing
    val rowModifier =
        modifier
            .fillMaxWidth()
            .heightIn(min = PartloreLayout.touchTarget)
            .background(
                if (!zebra) {
                    colors.surface.copy(alpha = 0f)
                } else if (colors.isDark) {
                    colors.surface
                } else {
                    colors.bg
                },
            )
            .combinedClickable(
                onClickLabel = stringResource(R.string.pl_show_source),
                onLongClickLabel = stringResource(R.string.pl_copy_value),
                onLongClick = onLongClick,
                onClick = onClick,
            ).padding(horizontal = spacing.space12, vertical = spacing.space8)
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "${row.label}: ${row.value}. ${row.sourceDescription}"
            }
    if (stacked) {
        Column(rowModifier) {
            Text(row.label, style = PartloreTheme.typography.caption, color = colors.textSecondary)
            ValueWithDot(row)
        }
    } else {
        Row(rowModifier, horizontalArrangement = Arrangement.spacedBy(spacing.space12)) {
            Text(
                row.label,
                style = PartloreTheme.typography.bodyM,
                color = colors.textSecondary,
                modifier = Modifier.weight(LABEL_WEIGHT),
            )
            Box(Modifier.weight(VALUE_WEIGHT)) { ValueWithDot(row) }
        }
    }
}

@Composable
private fun ValueWithDot(row: PlSpecRow, modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space4),
        ) {
            Text(
                row.value,
                style = PartloreTheme.typography.monoM,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f, fill = false),
            )
            Box(Modifier.size(PartloreLayout.sourceDot).clip(PartloreTheme.shapes.full).background(colors.info))
        }
        row.note?.let { Text(it, style = PartloreTheme.typography.caption, color = colors.textSecondary) }
    }
}
