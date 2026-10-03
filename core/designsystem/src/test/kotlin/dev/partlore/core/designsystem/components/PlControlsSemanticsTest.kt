package dev.partlore.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// Native graphics measure real text; the default mode counts every letter as one pixel wide.
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PlControlsSemanticsTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    private fun hasRole(role: Role) = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

    private fun lineCount(text: String): Int {
        val layouts = mutableListOf<TextLayoutResult>()
        val node = compose.onNodeWithText(text, useUnmergedTree = true).fetchSemanticsNode()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
        return layouts.single().lineCount
    }

    @Test
    fun segmentedOptionsAreRadioButtonsAndReportSelection() {
        var picked = -1
        compose.setContent {
            PartloreTheme { PlSegmented(listOf("Light", "Dark"), selectedIndex = 0, onSelect = { picked = it }) }
        }
        compose.onNodeWithText("Light").assertIsSelected().assert(hasRole(Role.RadioButton))
        compose.onNodeWithText("Dark").assertIsNotSelected().performClick()
        assertEquals(1, picked)
    }

    @Test
    // A common small phone, with the same side padding as the Settings screen.
    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    fun segmentedLabelsNeverBreakAWordAtLargeText() {
        val labels = listOf("System", "Light", "Dark", "Bench")
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                PartloreTheme {
                    Box(Modifier.padding(horizontal = PartloreTheme.spacing.space16)) {
                        PlSegmented(labels, selectedIndex = 0, onSelect = {})
                    }
                }
            }
        }
        labels.forEach { assertEquals("lines in '$it'", 1, lineCount(it)) }
    }

    @Test
    fun tappingAnywhereOnASwitchRowToggles() {
        var value = false
        compose.setContent {
            PartloreTheme { PlSwitchRow(title = "Haptics", checked = value, onCheckedChange = { value = it }) }
        }
        compose.onNodeWithText("Haptics").performClick()
        assertEquals(true, value)
    }

    @Test
    fun chipsAreCheckboxes() {
        var clicks = 0
        compose.setContent { PartloreTheme { PlChip("ESP32", selected = true, onClick = { clicks++ }) } }
        compose.onNodeWithText("ESP32").assertIsSelected().assert(hasRole(Role.Checkbox)).performClick()
        assertEquals(1, clicks)
    }
}
