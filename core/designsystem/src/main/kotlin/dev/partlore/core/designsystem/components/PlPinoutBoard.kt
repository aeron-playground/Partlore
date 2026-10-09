package dev.partlore.core.designsystem.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreSprings
import dev.partlore.core.designsystem.theme.PartloreStroke
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.PlacedBoard
import dev.partlore.core.model.PlacedPin
import dev.partlore.core.model.Side
import kotlinx.coroutines.launch

private const val DIMMED_ALPHA = 0.35f
private const val PICKED_SCALE = 1.15f
private const val READ_UPWARD = -90f

/**
 * The board: an outline with each pin on its cell and its label outside. One composable per pin, so every
 * pin is a real button for TalkBack. [onPinClick] null draws a preview that can't be tapped.
 */
@Composable
fun PlPinoutBoard(
    board: PlacedBoard,
    pins: Map<String, PlBoardPin>,
    modifier: Modifier = Modifier,
    dimmed: Set<String> = emptySet(),
    selected: String? = null,
    large: Boolean = false,
    zoom: PlZoomState? = null,
    onPinClick: ((String) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val motion = PartloreTheme.motion
    val placed = board.pins.filter { it.pinId in pins }
    val click: ((String) -> Unit)? =
        onPinClick?.let { pick ->
            { id: String ->
                if (zoom == null ||
                    zoom.tapsPick
                ) {
                    pick(id)
                } else {
                    scope.launch { zoom.zoomTo(1f, motion.spring(PartloreSprings.glide)) }
                }
            }
        }
    val cell = cellSize(large)
    Layout(
        contents =
        listOf(
            { Outline() },
            {
                placed.forEach { p ->
                    key(p.pinId) {
                        PinCell(
                            pins.getValue(p.pinId),
                            p.pinId in dimmed,
                            p.pinId == selected,
                            large,
                            click,
                            Modifier.glide(p, cell),
                        )
                    }
                }
            },
            {
                placed.forEach { p ->
                    key(p.pinId) {
                        PinLabel(pins.getValue(p.pinId), p.labelSide, p.pinId in dimmed, Modifier.glide(p, cell))
                    }
                }
            },
        ),
        modifier = if (zoom != null) modifier.zoomable(zoom) else modifier,
    ) { (outline, cells, labels), _ ->
        val cellPlaceables = cells.map { it.measure(Constraints.fixed(cell, cell)) }
        val labelPlaceables = labels.map { it.measure(Constraints()) }
        val m = Margins.of(placed, labelPlaceables)
        val box = outline.single().measure(Constraints.fixed((board.columns - 1) * cell, (board.rows - 1) * cell))
        layout(m.left + board.columns * cell + m.right, m.top + board.rows * cell + m.bottom) {
            box.place(m.left + cell / 2, m.top + cell / 2)
            placed.forEachIndexed { i, p ->
                val x = m.left + p.x * cell
                val y = m.top + p.y * cell
                cellPlaceables[i].place(x, y)
                labelPlaceables[i].place(labelAt(p.labelSide, x, y, cell, labelPlaceables[i]))
            }
        }
    }
}

/**
 * When a pin's cell changes (rotate), it starts where it was and springs to its new place; reduced motion jumps.
 * Like a list item moving: the layout places it at the new spot at once, and a draw offset makes it glide there.
 */
@Composable
private fun Modifier.glide(pin: PlacedPin, cell: Int): Modifier {
    val target = IntOffset(pin.x * cell, pin.y * cell)
    val motion = PartloreTheme.motion
    val shift = remember { Animatable(IntOffset.Zero, IntOffset.VectorConverter) }
    var last by remember { mutableStateOf(target) }
    LaunchedEffect(target) {
        if (target != last) {
            if (!motion.reduced) shift.snapTo(shift.value + last - target)
            last = target
            shift.animateTo(IntOffset.Zero, motion.spring(PartloreSprings.glide))
        }
    }
    return offset { shift.value }
}

@Composable
private fun cellSize(large: Boolean): Int {
    val density = LocalDensity.current
    val row = if (large) PartloreLayout.pinRowLarge else PartloreLayout.pinRow
    return with(density) { (row * density.fontScale.coerceAtLeast(1f)).roundToPx() }
}

private data class Margins(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    companion object {
        fun of(placed: List<PlacedPin>, labels: List<Placeable>): Margins {
            fun max(side: Side, size: (Placeable) -> Int) =
                placed.indices.filter { placed[it].labelSide == side }.maxOfOrNull { size(labels[it]) } ?: 0
            return Margins(
                max(Side.Left) {
                    it.width
                },
                max(Side.Top) { it.height },
                max(Side.Right) { it.width },
                max(Side.Bottom) { it.height },
            )
        }
    }
}

private fun labelAt(side: Side, x: Int, y: Int, cell: Int, label: Placeable): IntOffset = when (side) {
    Side.Left -> IntOffset(x - label.width, y + (cell - label.height) / 2)
    Side.Right -> IntOffset(x + cell, y + (cell - label.height) / 2)
    Side.Top -> IntOffset(x + (cell - label.width) / 2, y - label.height)
    Side.Bottom -> IntOffset(x + (cell - label.width) / 2, y + cell)
}

@Composable
private fun Outline(modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    Box(
        modifier
            .clip(PartloreTheme.shapes.md)
            .background(colors.surface)
            .border(PartloreStroke.hairline, colors.outline, PartloreTheme.shapes.md),
    )
}

@Composable
private fun PinCell(
    pin: PlBoardPin,
    dimmed: Boolean,
    selected: Boolean,
    large: Boolean,
    onClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val motion = PartloreTheme.motion
    val alpha by animateFloatAsState(
        if (dimmed) DIMMED_ALPHA else 1f,
        motion.spring(PartloreSprings.snap),
        label = "dim",
    )
    val scale by animateFloatAsState(
        if (selected && !motion.reduced) PICKED_SCALE else 1f,
        motion.spring(PartloreSprings.snap),
        label = "pick",
    )
    val ring by animateFloatAsState(if (selected) 1f else 0f, motion.fade(), label = "ring")
    val colors = PartloreTheme.colors
    Box(
        modifier
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }.then(if (onClick != null) Modifier.clickable(role = Role.Button) { onClick(pin.id) } else Modifier)
            .semantics {
                contentDescription = pin.description
                this.selected = selected
            },
        contentAlignment = Alignment.Center,
    ) {
        PlPinDot(
            pin.kind,
            Modifier.border(PartloreLayout.pinRing, colors.primary.copy(alpha = ring), PartloreTheme.shapes.full),
            strapping = pin.strapping,
            caution = pin.caution,
            inputOnly = pin.inputOnly,
            large = large,
        )
    }
}

// Switching label modes cross-fades each label; reduced motion swaps it at once.
@Composable
private fun PinLabel(pin: PlBoardPin, side: Side, dimmed: Boolean, modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    val motion = PartloreTheme.motion
    val upward = side == Side.Top || side == Side.Bottom
    val outer =
        modifier
            .clearAndSetSemantics {}
            .graphicsLayer { alpha = if (dimmed) DIMMED_ALPHA else 1f }
            .then(if (upward) Modifier.readUpward() else Modifier)
    val text: @Composable (String) -> Unit = { label ->
        Text(
            text = label,
            style = PartloreTheme.typography.monoS,
            color = if (pin.labelMuted) colors.textSecondary else colors.textPrimary,
            maxLines = 1,
        )
    }
    if (motion.reduced) {
        Box(outer) { text(pin.label) }
    } else {
        Crossfade(pin.label, outer, motion.fade(), label = "label") { text(it) }
    }
}

// Turns a label to read upward: swaps its width and height, then rotates the drawing.
private fun Modifier.readUpward(): Modifier = this
    .layout { measurable, constraints ->
        val p = measurable.measure(constraints)
        layout(p.height, p.width) { p.place(-(p.width - p.height) / 2, (p.width - p.height) / 2) }
    }.rotate(READ_UPWARD)

/** The whole board scaled down to fit, for the part page. Not interactive. */
@Composable
fun PlPinoutPreview(board: PlacedBoard, pins: Map<String, PlBoardPin>, modifier: Modifier = Modifier) {
    Layout(content = { PlPinoutBoard(board, pins) }, modifier = modifier) { measurables, constraints ->
        val p = measurables.single().measure(Constraints())
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else p.width
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else p.height
        val fit = minOf(1f, width / p.width.toFloat(), height / p.height.toFloat())
        layout(width, height) {
            p.placeWithLayer((width - p.width) / 2, (height - p.height) / 2) {
                scaleX = fit
                scaleY = fit
            }
        }
    }
}
