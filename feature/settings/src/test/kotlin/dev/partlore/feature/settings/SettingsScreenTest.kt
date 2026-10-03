package dev.partlore.feature.settings

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.MotionSetting
import dev.partlore.core.model.ThemeSetting
import dev.partlore.core.model.UserSettings
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsScreenTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    @Test
    fun everyControlReportsTheUsersChoice() {
        val changes = mutableListOf<SettingsChange>()
        var backs = 0
        compose.setContent {
            PartloreTheme {
                SettingsScreen(
                    settings = UserSettings(),
                    versionName = "0.0.2",
                    onBack = { backs++ },
                    onChange = { changes += it },
                )
            }
        }
        compose.onNodeWithText("Dark").performClick()
        compose.onNodeWithText("Reduced").performClick()
        compose.onNodeWithText("Haptics").performScrollTo().performClick()
        compose.onNodeWithText("Sounds").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Back").performClick()

        assertEquals(
            listOf(
                SettingsChange.Theme(ThemeSetting.Dark),
                SettingsChange.Motion(MotionSetting.Reduced),
                SettingsChange.Haptics(false),
                SettingsChange.Sounds(true),
            ),
            changes,
        )
        assertEquals(1, backs)
    }

    @Test
    fun showsTheVersion() {
        compose.setContent {
            PartloreTheme {
                SettingsScreen(UserSettings(), versionName = "0.0.2", onBack = {}, onChange = {})
            }
        }
        compose.onNodeWithText("Version 0.0.2").performScrollTo().assertExists()
    }
}
