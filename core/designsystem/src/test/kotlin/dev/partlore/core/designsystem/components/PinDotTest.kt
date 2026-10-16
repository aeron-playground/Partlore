package dev.partlore.core.designsystem.components

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.PinKind
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PinDotTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    // The pin's button carries the words; the dot's glyph must not be read a second time.
    @Test
    fun theDotIsDecorative() {
        compose.setContent { PartloreTheme { PlPinDot(PinKind.I2c) } }
        compose.onAllNodesWithText("I²C").assertCountEquals(0)
    }

    @Test
    fun theLegendNamesEveryKindAndMark() {
        compose.setContent { PartloreTheme { PlPinLegend() } }
        listOf(
            "Power", "Ground", "I²C", "SPI", "UART", "ADC", "PWM", "Touch", "Do not use", "GPIO or other",
            "Strapping pin", "Caution", "Input only",
        ).forEach { compose.onNodeWithText(it, substring = false).assertExists() }
    }
}
