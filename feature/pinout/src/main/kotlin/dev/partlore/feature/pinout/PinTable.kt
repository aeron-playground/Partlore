package dev.partlore.feature.pinout

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.components.PlBoardPin
import dev.partlore.core.designsystem.components.PlPinRow
import dev.partlore.core.designsystem.theme.PartloreTheme

/** The accessible view: every pin as a row, header by header, in pin order. */
@Composable
internal fun PinTable(
    state: PinoutState,
    pins: Map<String, PlBoardPin>,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val byHeader =
        state.pinout.headers.associateWith { h ->
            state.pinout.pins.filter { it.headerId == h.id }.sortedWith(compareBy({ it.row }, { it.index }))
        }
    LazyColumn(modifier) {
        byHeader.forEach { (header, headerPins) ->
            item(key = "header:${header.id}") {
                Text(
                    header.label,
                    style = PartloreTheme.typography.title,
                    color = PartloreTheme.colors.textPrimary,
                    modifier = Modifier.padding(PartloreTheme.spacing.space16).semantics { heading() },
                )
            }
            items(headerPins, key = { it.id }) { pin ->
                PlPinRow(
                    pin = pins.getValue(pin.id),
                    detail = pin.functions.filter { it.type != "gpio" }.joinToString(" · ") {
                        it.signal
                            ?: it.type.uppercase()
                    },
                    onClick = { onPick(pin.id) },
                    dimmed = pin.id in state.dimmed,
                )
            }
        }
    }
}
