package dev.partlore.app.shell

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.partlore.core.designsystem.components.PlButton
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PartloreShellTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    @Test
    fun benchGearOpensSettingsAndReselectingBenchGoesBack() {
        compose.setContent { PartloreTheme { PartloreShell(screens = fakeScreens()) } }
        compose.onNodeWithText("Bench").performClick()
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithText("Settings page").assertExists()
        compose.onNodeWithText("Bench").performClick()
        compose.onNodeWithText("Nothing saved yet. Tap ★ on any part.").assertExists()
    }

    @Test
    fun aHiddenTabKeepsItsPageAndItsState() {
        val counterPage: @androidx.compose.runtime.Composable (ShellNav) -> Unit = {
            var taps by rememberSaveable { mutableIntStateOf(0) }
            PlButton("Tapped $taps", onClick = { taps++ })
        }
        compose.setContent { PartloreTheme { PartloreShell(screens = fakeScreens(settings = counterPage)) } }
        compose.onNodeWithText("Bench").performClick()
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithText("Tapped 0").performClick()
        compose.onNodeWithText("Library").performClick()
        compose.onNodeWithText("Bench").performClick()
        compose.onNodeWithText("Tapped 1").assertExists()
    }

    @Test
    fun theOpenPageSurvivesRecreation() {
        val restore = StateRestorationTester(compose)
        restore.setContent { PartloreTheme { PartloreShell(screens = fakeScreens()) } }
        compose.onNodeWithText("Bench").performClick()
        compose.onNodeWithContentDescription("Settings").performClick()
        restore.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Settings page").assertExists()
    }

    @Test
    fun libraryPagesStackUpAndBackClosesTheTopOne() {
        compose.setContent { PartloreTheme { PartloreShell(screens = fakeScreens()) } }
        compose.onNodeWithText("Open boards").performClick()
        compose.onNodeWithText("Open the dev board").performClick()
        compose.onNodeWithText("Part example/devboard-v1").assertExists()
        compose.onNodeWithText("Go back").performClick()
        compose.onNodeWithText("Category boards").assertExists()
    }
}
