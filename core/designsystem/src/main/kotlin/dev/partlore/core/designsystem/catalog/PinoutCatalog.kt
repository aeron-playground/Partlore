package dev.partlore.core.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import dev.partlore.core.designsystem.components.PlPinLegend
import dev.partlore.core.designsystem.components.PlPinRow
import dev.partlore.core.designsystem.components.PlPinoutBoard
import dev.partlore.core.designsystem.components.PlPinoutPreview
import dev.partlore.core.designsystem.components.rememberBoardPins
import dev.partlore.core.designsystem.theme.ColourBlindness
import dev.partlore.core.designsystem.theme.LocalPartloreColors
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.BoardEdge
import dev.partlore.core.model.Cite
import dev.partlore.core.model.HeaderRun
import dev.partlore.core.model.PinDirection
import dev.partlore.core.model.PinFunction
import dev.partlore.core.model.PinHeader
import dev.partlore.core.model.PinInfo
import dev.partlore.core.model.PinLabelMode
import dev.partlore.core.model.PinSafety
import dev.partlore.core.model.Pinout
import dev.partlore.core.model.PinoutLayout
import dev.partlore.core.model.Rotation
import dev.partlore.core.model.RunAxis
import dev.partlore.core.model.Side
import dev.partlore.core.model.Strapping

private val cite = Cite(sourceId = "s1", page = null, section = null, ref = null)

private fun header(id: String, edge: BoardEdge) = PinHeader(
    id = id,
    label = id.uppercase(),
    type = "header",
    rows = 1,
    pinsPerRow = 3,
    pitchMm = 2.54,
    runs = listOf(
        HeaderRun(edge, RunAxis.Vertical, pins = 3, first = Side.Top, row1 = null, order = null, cite = cite),
    ),
    cite = cite,
)

private fun pin(
    id: String,
    header: String,
    index: Int,
    label: String,
    direction: PinDirection = PinDirection.Io,
    functions: List<PinFunction> = emptyList(),
    safe: PinSafety = PinSafety.Ok,
    strapping: Strapping? = null,
) = PinInfo(
    id = id,
    headerId = header,
    row = 1,
    index = index,
    chipName = null,
    modulePad = null,
    boardLabel = label,
    arduino = null,
    aliases = emptyList(),
    direction = direction,
    voltage = null,
    fiveVoltTolerant = null,
    functions = functions,
    safe = safe,
    strapping = strapping,
    note = null,
    cites = listOf(cite),
    gotchas = emptyList(),
)

// A made-up board, never real content: one pin of each look the viewer draws.
private val samplePinout =
    Pinout(
        partId = "example/pins",
        partName = "Example pins",
        headers = listOf(header("j1", BoardEdge.Left), header("j2", BoardEdge.Right)),
        pins =
        listOf(
            pin("3v3", "j1", index = 1, label = "3V3", direction = PinDirection.Power),
            pin("sda", "j1", index = 2, label = "D21", functions = listOf(PinFunction("i2c", "SDA", true))),
            pin(
                "boot",
                "j1",
                index = 3,
                label = "D4",
                functions = listOf(PinFunction("adc", "ADC2_CH2", false)),
                safe = PinSafety.Caution,
                strapping = Strapping("boot-mode", "low", "reset"),
            ),
            pin("gnd", "j2", index = 1, label = "GND", direction = PinDirection.Ground),
            pin("clk", "j2", index = 2, label = "CLK", safe = PinSafety.Avoid),
            pin("in34", "j2", index = 3, label = "D34", direction = PinDirection.In),
        ),
        sources = emptyList(),
        status = null,
        packVersion = "0.0.0",
    )

/** The pinout parts: the board (one pin picked, one dimmed), its preview, table rows and the legend. */
@Composable
fun PinoutCatalog(modifier: Modifier = Modifier) {
    val pins = rememberBoardPins(samplePinout, PinLabelMode.Board)
    val board = PinoutLayout.place(samplePinout, Rotation.R0)
    CatalogSection("Pinout", modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space16)) {
            PlPinoutBoard(board, pins, selected = "boot", dimmed = setOf("clk"), onPinClick = {})
            PlPinoutPreview(board, pins, Modifier.fillMaxWidth().height(PartloreLayout.pinoutPreviewHeight))
            Column {
                PlPinRow(pins.getValue("sda"), detail = "I²C SDA", onClick = {})
                PlPinRow(pins.getValue("boot"), detail = "ADC2_CH2 · strapping pin", onClick = {})
                PlPinRow(pins.getValue("clk"), detail = "Flash clock: do not use", onClick = {}, dimmed = true)
            }
            PlPinLegend()
        }
    }
}

/** The legend as people with red-green colour blindness see it: kinds must stay apart by glyph and shade. */
@Composable
fun PinoutColourBlindCatalog(modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    CatalogSection("Pin colours, colour blind", modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space16)) {
            ColourBlindness.entries.forEach { type ->
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space8)) {
                    Text(type.name, style = PartloreTheme.typography.title, color = colors.textPrimary)
                    CompositionLocalProvider(
                        LocalPartloreColors provides colors.copy(pins = type.simulate(colors.pins)),
                    ) {
                        PlPinLegend()
                    }
                }
            }
        }
    }
}
