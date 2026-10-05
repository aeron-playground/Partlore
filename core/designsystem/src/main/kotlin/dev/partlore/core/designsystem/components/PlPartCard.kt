package dev.partlore.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreSprings
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.partloreElevation
import dev.partlore.core.model.VerificationLevel

private const val PRESSED_SCALE = 0.97f

// The carousel card grows with the text size (up to twice as wide), so names and badges don't break mid-word.
private const val MAX_TEXT_GROWTH = 2f

/** A part in a list (wide) or a carousel (compact). Presses down slightly when touched. */
@Composable
fun PlPartCard(
    name: String,
    line: String,
    kind: String,
    level: VerificationLevel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colors = PartloreTheme.colors
    val motion = PartloreTheme.motion
    val compactWidth = PartloreLayout.starterCardWidth * LocalDensity.current.fontScale.coerceIn(1f, MAX_TEXT_GROWTH)
    val spacing = PartloreTheme.spacing
    val shape = PartloreTheme.shapes.md
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    // Reduced motion: no scaling, only the ripple.
    val scale by animateFloatAsState(
        targetValue = if (pressed && !motion.reduced) PRESSED_SCALE else 1f,
        animationSpec = motion.spring(PartloreSprings.snap),
        label = "press",
    )
    val card =
        Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }.partloreElevation(PartloreTheme.elevation.e1, shape, colors)
            .clip(shape)
            .background(if (colors.isDark) colors.surfaceRaised else colors.surface)
            .clickable(interactionSource = source, indication = ripple(), role = Role.Button, onClick = onClick)
            .padding(spacing.space12)
    if (compact) {
        Column(
            modifier.width(compactWidth).then(card),
            verticalArrangement = Arrangement.spacedBy(spacing.space8),
        ) {
            PlPartArt(kind, Modifier.size(PartloreLayout.artSmall))
            CardText(name, line)
            PlStatusBadge(level)
        }
    } else {
        Row(
            modifier.fillMaxWidth().then(card),
            horizontalArrangement = Arrangement.spacedBy(spacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlPartArt(kind, Modifier.size(PartloreLayout.artSmall))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.space4)) {
                CardText(name, line)
                PlStatusBadge(level)
            }
        }
    }
}

@Composable
private fun CardText(name: String, line: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            name,
            style = PartloreTheme.typography.title,
            color = PartloreTheme.colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (line.isNotBlank()) {
            Text(
                line,
                style = PartloreTheme.typography.caption,
                color = PartloreTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
