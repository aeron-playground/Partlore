package dev.partlore.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.R
import dev.partlore.core.designsystem.theme.PartloreColors
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.VerificationLevel

/** How far a part's data has been checked: a glyph and words, never colour alone. */
@Composable
fun PlStatusBadge(
    level: VerificationLevel,
    modifier: Modifier = Modifier,
    checkers: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val spacing = PartloreTheme.spacing
    val look = statusLook(level, PartloreTheme.colors)
    Row(
        modifier =
        modifier
            .then(if (onClick != null) Modifier.minimumInteractiveComponentSize() else Modifier)
            .clip(PartloreTheme.shapes.sm)
            .background(look.background)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = spacing.space8, vertical = spacing.space4)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlGlyphIcon(look.glyph, look.content, cutout = look.background)
        Text(statusLabel(level, checkers), style = PartloreTheme.typography.label, color = look.content)
    }
}

private data class StatusLook(val glyph: PlGlyph, val content: Color, val background: Color)

private fun statusLook(level: VerificationLevel, colors: PartloreColors): StatusLook = when (level) {
    VerificationLevel.Checked, VerificationLevel.Verified -> StatusLook(
        PlGlyph.Check,
        colors.success,
        colors.surfaceSunken,
    )

    VerificationLevel.Disputed -> StatusLook(PlGlyph.Alert, colors.danger, colors.dangerContainer)

    else -> StatusLook(PlGlyph.Half, colors.warning, colors.warningContainer)
}

@Composable
private fun statusLabel(level: VerificationLevel, checkers: Int?): String = when (level) {
    VerificationLevel.Draft -> stringResource(R.string.pl_status_draft)

    VerificationLevel.Imported -> stringResource(R.string.pl_status_imported)

    VerificationLevel.Checked ->
        if (checkers != null && checkers > 0) {
            pluralStringResource(R.plurals.pl_status_checked_by, checkers, checkers)
        } else {
            stringResource(R.string.pl_status_checked)
        }

    VerificationLevel.Verified ->
        if (checkers != null && checkers > 0) {
            pluralStringResource(R.plurals.pl_status_verified_by, checkers, checkers)
        } else {
            stringResource(R.string.pl_status_verified)
        }

    VerificationLevel.NeedsReview -> stringResource(R.string.pl_status_needs_review)

    VerificationLevel.Disputed -> stringResource(R.string.pl_status_disputed)
}
