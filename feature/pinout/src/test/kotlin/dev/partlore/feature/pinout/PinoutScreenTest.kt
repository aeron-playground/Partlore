package dev.partlore.feature.pinout

import android.view.View
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.FileStatus
import dev.partlore.core.model.PinLabelMode
import dev.partlore.core.model.PinoutLayout
import dev.partlore.core.model.Rotation
import dev.partlore.core.model.VerificationLevel
import dev.partlore.core.testing.SampleContent
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val UNCHECKED = "Pins not checked yet. Check the datasheet before you wire anything."

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp-xhdpi")
class PinoutScreenTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    private val actions = mutableListOf<PinoutAction>()

    private fun state(
        selected: String? = null,
        keepScreenOn: Boolean = false,
        level: VerificationLevel = VerificationLevel.Draft,
    ): PinoutState {
        val pinout = SampleContent.devBoardPinout.copy(status = FileStatus(level, emptyList(), null, emptyList(), null))
        return PinoutState(
            pinout = pinout,
            board = PinoutLayout.place(pinout, Rotation.R0),
            labelMode = PinLabelMode.Board,
            labelModes = listOf(PinLabelMode.Chip, PinLabelMode.Module, PinLabelMode.Board),
            filters = pinout.filters(),
            filter = null,
            selected = pinout.pins.firstOrNull { it.id == selected },
            table = false,
            keepScreenOn = keepScreenOn,
            redLight = false,
            bench = false,
            haptics = false,
        )
    }

    private fun show(state: PinoutState) = compose.setContent {
        PartloreTheme { PinoutScreen(ContentResult.Ok(state), onBack = {}, onAction = { actions += it }) }
    }

    // Review Focus 5: unchecked pins must never look trustworthy.
    @Test
    fun uncheckedPinsShowAWarningThatStays() {
        show(state())
        compose.onNodeWithText(UNCHECKED).assertExists()
    }

    @Test
    fun checkedPinsShowNoWarning() {
        show(state(level = VerificationLevel.Checked))
        compose.onNodeWithText(UNCHECKED).assertDoesNotExist()
    }

    @Test
    fun tappingAPinSelectsIt() {
        show(state())
        compose.onNodeWithContentDescription("D4", substring = true).performClick()
        assertEquals(listOf<PinoutAction>(PinoutAction.Select("io2")), actions)
    }

    @Test
    fun anOpenPinShowsItsDetails() {
        show(state(selected = "io2"))
        compose.onNodeWithText("Strapping pin").assertExists()
        val note = "Its level at reset decides how the chip starts (boot-mode). It must be low for a normal start."
        compose.onNodeWithText(note).assertExists()
    }

    @Test
    fun filterChipsSayHowManyPinsTheyMatch() {
        show(state())
        compose.onNodeWithContentDescription("I²C, 2 pins").assertExists()
        compose.onNodeWithContentDescription("Power, 3 pins").assertExists()
    }

    @Test
    fun filtersLabelsAndToolsSendActions() {
        show(state())
        compose.onNodeWithContentDescription("I²C, 2 pins").performClick()
        compose.onNodeWithText("Chip").performClick()
        compose.onNodeWithText("Rotate").performClick()
        compose.onNodeWithText("Table").performClick()
        compose.onNodeWithText("Keep screen on").performClick()
        compose.onNodeWithText("Red light").performClick()
        assertEquals(
            listOf(
                PinoutAction.Filter(PinFilter.I2c),
                PinoutAction.Labels(PinLabelMode.Chip),
                PinoutAction.Rotate,
                PinoutAction.ToggleTable,
                PinoutAction.ToggleKeepScreenOn,
                PinoutAction.ToggleRedLight,
            ),
            actions,
        )
    }

    // Review Focus 4: the screen may sleep again as soon as the viewer is gone.
    @Test
    fun leavingTheViewerLetsTheScreenSleep() {
        var viewer by mutableStateOf(true)
        lateinit var view: View
        compose.setContent {
            view = LocalView.current
            PartloreTheme {
                if (viewer) PinoutScreen(ContentResult.Ok(state(keepScreenOn = true)), onBack = {}, onAction = {})
            }
        }
        assertEquals(true, view.keepScreenOn)
        viewer = false
        compose.waitForIdle()
        assertEquals(false, view.keepScreenOn)
    }

    // At 200 % text the rows double and the board is wider than a phone: it opens fitted, not cut on both sides.
    // Native graphics measures text as a phone does; the default test mode counts about 1 px per letter.
    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun anOversizedBoardOpensFittedToTheScreen() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                PartloreTheme { PinoutScreen(ContentResult.Ok(state()), onBack = {}, onAction = {}) }
            }
        }
        val width = compose.onRoot().fetchSemanticsNode().boundsInRoot.width
        val left = compose.onNodeWithContentDescription("3V3", substring = true).fetchSemanticsNode().boundsInRoot
        val right = compose.onNodeWithContentDescription("D34", substring = true).fetchSemanticsNode().boundsInRoot
        val fullCell = with(compose.density) { (PartloreLayout.pinRow * 2).toPx() }
        assertTrue("scaled down: ${left.width} < $fullCell", left.width < fullCell)
        assertTrue("left edge on screen: ${left.left}", left.left >= 0f)
        assertTrue("right edge on screen: ${right.right} <= $width", right.right <= width)
    }

    @Test
    fun aPartWithoutPinsSaysSo() {
        compose.setContent { PartloreTheme { PinoutScreen(ContentResult.NotFound, onBack = {}, onAction = {}) } }
        compose.onNodeWithText("This part has no pinout yet.").assertExists()
    }
}
