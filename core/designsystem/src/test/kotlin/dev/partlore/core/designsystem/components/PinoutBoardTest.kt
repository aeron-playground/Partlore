package dev.partlore.core.designsystem.components

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.PinKind
import dev.partlore.core.model.BoardEdge
import dev.partlore.core.model.Cite
import dev.partlore.core.model.HeaderRun
import dev.partlore.core.model.PinDirection
import dev.partlore.core.model.PinFunction
import dev.partlore.core.model.PinHeader
import dev.partlore.core.model.PinInfo
import dev.partlore.core.model.PinSafety
import dev.partlore.core.model.Pinout
import dev.partlore.core.model.PinoutLayout
import dev.partlore.core.model.Rotation
import dev.partlore.core.model.RunAxis
import dev.partlore.core.model.Side
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp-xhdpi")
class PinoutBoardTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    private val cite = Cite("s1", 1, null, null)
    private val run = HeaderRun(BoardEdge.Left, RunAxis.Vertical, 2, Side.Top, null, null, cite)
    private val sda =
        PinInfo(
            "io21", "j1", 1, 1, "GPIO21", "IO21", "D21", null, emptyList(), PinDirection.Io, 3.3, false,
            listOf(PinFunction("i2c", "SDA", true)), PinSafety.Ok, null, null, emptyList(), emptyList(),
        )
    private val gnd =
        sda.copy(
            id = "gnd",
            index = 2,
            chipName = null,
            modulePad = "GND",
            boardLabel = "GND",
            direction = PinDirection.Ground,
            functions = emptyList(),
        )
    private val pinout =
        Pinout(
            "t/p",
            "P",
            listOf(PinHeader("j1", "J1", "header", 1, 2, 2.54, listOf(run), cite)),
            listOf(sda, gnd),
            emptyList(),
            null,
            "0.1.0",
        )
    private val board = PinoutLayout.place(pinout, Rotation.R0)
    private val pins =
        mapOf(
            "io21" to PlBoardPin("io21", "D21", false, "D21. GPIO21. I²C SDA", PinKind.I2c, false, false, false),
            "gnd" to PlBoardPin("gnd", "GND", false, "GND. Ground", PinKind.Ground, false, false, false),
        )

    @Test
    fun pinsAreButtonsThatSayWhatTheyAre() {
        compose.setContent { PartloreTheme { PlPinoutBoard(board, pins, onPinClick = {}) } }
        compose.onNodeWithContentDescription("D21. GPIO21. I²C SDA")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }

    @Test
    fun tappingAPinPicksIt() {
        val picked = mutableListOf<String>()
        compose.setContent { PartloreTheme { PlPinoutBoard(board, pins, onPinClick = { picked += it }) } }
        compose.onNodeWithContentDescription("GND. Ground").performClick()
        assertEquals(listOf("gnd"), picked)
    }

    // Review Focus 3: pins are smaller than a finger below 1×, so a tap zooms instead of picking a neighbour.
    @Test
    fun aTapBelowNormalZoomZoomsInsteadOfSelecting() {
        val picked = mutableListOf<String>()
        val zoom = PlZoomState(initialScale = 0.5f)
        compose.setContent { PartloreTheme { PlPinoutBoard(board, pins, zoom = zoom, onPinClick = { picked += it }) } }
        compose.onNodeWithContentDescription("GND. Ground").performClick()
        compose.waitForIdle()
        assertEquals(emptyList<String>(), picked)
        assertEquals(1f, zoom.scale)
    }

    @Test
    fun aPreviewHasNoButtons() {
        compose.setContent { PartloreTheme { PlPinoutPreview(board, pins) } }
        compose.onAllNodes(hasClickAction()).assertCountEquals(0)
    }

    @Test
    fun pinKindsFollowDirectionSafetyThenFunction() {
        assertEquals(PinKind.I2c, sda.pinKind())
        assertEquals(PinKind.Ground, gnd.pinKind())
        assertEquals(PinKind.DoNotUse, sda.copy(safe = PinSafety.Avoid).pinKind())
        assertEquals(PinKind.Gpio, sda.copy(functions = listOf(PinFunction("dac", "DAC1", false))).pinKind())
    }
}
