package dev.partlore.core.designsystem.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val PINCH_START = 20f
private const val PINCH_END = 80f

// Touch slop takes a few pixels before a pinch starts; the point between the fingers may drift by that much.
private const val PINCH_TOLERANCE = 3f

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

    // The design: below 1× a tap zooms to 1× around that spot, so the tapped pin stays under the finger.
    @Test
    fun aTapBelowNormalZoomKeepsThePinUnderTheFinger() {
        val zoom = PlZoomState(initialScale = 0.5f)
        compose.setContent { PartloreTheme { PlPinoutBoard(board, pins, zoom = zoom, onPinClick = {}) } }
        val gnd = compose.onNodeWithContentDescription("GND. Ground")
        val before = gnd.fetchSemanticsNode().boundsInRoot.center
        gnd.performClick()
        compose.waitForIdle()
        assertNear(before, gnd.fetchSemanticsNode().boundsInRoot.center)
    }

    @Test
    fun aPinchZoomsAroundTheFingers() {
        val zoom = PlZoomState()
        compose.setContent { PartloreTheme { PlPinoutBoard(board, pins, zoom = zoom, onPinClick = {}) } }
        val gnd = compose.onNodeWithContentDescription("GND. Ground")
        val c = gnd.fetchSemanticsNode().boundsInRoot.center
        val near = Offset(PINCH_START, 0f)
        val far = Offset(PINCH_END, 0f)
        compose.onRoot().performTouchInput { pinch(c - near, c - far, c + near, c + far) }
        compose.waitForIdle()
        assertTrue("zoomed in: ${zoom.scale}", zoom.scale > 1f)
        assertNear(c, gnd.fetchSemanticsNode().boundsInRoot.center, PINCH_TOLERANCE)
    }

    // TalkBack sends a click without a finger position; a TalkBack user picks the pin at any zoom.
    @Test
    fun aTalkBackClickBelowNormalZoomPicks() {
        val picked = mutableListOf<String>()
        val zoom = PlZoomState(initialScale = 0.5f)
        compose.setContent { PartloreTheme { PlPinoutBoard(board, pins, zoom = zoom, onPinClick = { picked += it }) } }
        compose.onNodeWithContentDescription("GND. Ground").performSemanticsAction(SemanticsActions.OnClick)
        assertEquals(listOf("gnd"), picked)
        assertEquals(0.5f, zoom.scale)
    }

    private fun assertNear(expected: Offset, actual: Offset, tolerance: Float = 1f) {
        assertEquals("x", expected.x, actual.x, tolerance)
        assertEquals("y", expected.y, actual.y, tolerance)
    }

    // Focus moves along each header, not row by row across the board.
    @Test
    fun talkBackGoesHeaderByHeader() {
        val right = HeaderRun(BoardEdge.Right, RunAxis.Vertical, 2, Side.Top, null, null, cite)
        val two =
            pinout.copy(
                headers = pinout.headers + PinHeader("j2", "J2", "header", 1, 2, 2.54, listOf(right), cite),
                pins = pinout.pins + listOf(sda.copy(id = "r1", headerId = "j2"), gnd.copy(id = "r2", headerId = "j2")),
            )
        val all = pins + mapOf(
            "r1" to PlBoardPin("r1", "R1", false, "R1", PinKind.Gpio, false, false, false),
            "r2" to PlBoardPin("r2", "R2", false, "R2", PinKind.Gpio, false, false, false),
        )
        compose.setContent {
            PartloreTheme { PlPinoutBoard(PinoutLayout.place(two, Rotation.R0), all, onPinClick = {}) }
        }
        compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.IsTraversalGroup, true)).assertExists()
        val order =
            listOf("D21. GPIO21. I²C SDA", "GND. Ground", "R1", "R2").map { name ->
                compose.onNodeWithContentDescription(
                    name,
                ).fetchSemanticsNode().config[SemanticsProperties.TraversalIndex]
            }
        assertEquals(order.sorted(), order)
        assertEquals(order.distinct(), order)
    }

    // On the part page the preview is a picture; its caption and the Open pinout button say what it shows.
    @Test
    fun aPreviewIsNotReadPinByPin() {
        compose.setContent { PartloreTheme { PlPinoutPreview(board, pins) } }
        compose.onAllNodesWithContentDescription("GND", substring = true).assertCountEquals(0)
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
