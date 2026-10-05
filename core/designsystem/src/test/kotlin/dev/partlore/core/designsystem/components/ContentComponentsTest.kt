package dev.partlore.core.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.ContentProblem
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.Severity
import dev.partlore.core.model.VerificationLevel
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ContentComponentsTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    private fun hasRole(role: Role) = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

    @Test
    fun statusBadgesSayHowFarDataIsChecked() {
        compose.setContent {
            PartloreTheme {
                androidx.compose.foundation.layout.Column {
                    PlStatusBadge(VerificationLevel.Draft)
                    PlStatusBadge(VerificationLevel.Checked, checkers = 2)
                    PlStatusBadge(VerificationLevel.Disputed)
                }
            }
        }
        compose.onNodeWithText("Not checked yet").assertExists()
        compose.onNodeWithText("Checked by 2 people").assertExists()
        compose.onNodeWithText("Sources disagree").assertExists()
    }

    @Test
    fun aPartCardIsAButtonThatOpensThePart() {
        var opened = 0
        compose.setContent {
            PartloreTheme {
                PlPartCard("Example Board", "Wi-Fi", "board", VerificationLevel.Draft, onClick = { opened++ })
            }
        }
        compose.onNodeWithText("Example Board").assert(hasRole(Role.Button)).performClick()
        assertEquals(1, opened)
    }

    @Test
    fun theSearchFieldOpensSearch() {
        var opened = 0
        compose.setContent { PartloreTheme { PlSearchField("Search parts", onClick = { opened++ }) } }
        compose.onNodeWithText("Search parts").performClick()
        assertEquals(1, opened)
    }

    @Test
    fun theErrorStateOffersCopyDetails() {
        compose.setContent { PartloreTheme { PlErrorState(ContentResult.Failed(ContentProblem.Damaged, "bad")) } }
        compose.onNodeWithText("Content couldn't be loaded.").assertExists()
        compose.onNodeWithText("Copy details").assert(hasRole(Role.Button))
    }

    @Test
    fun aNewerPackSaysTheAppNeedsUpdating() {
        compose.setContent { PartloreTheme { PlErrorState(ContentResult.Failed(ContentProblem.TooNew, "format 2")) } }
        compose.onNodeWithText("This content needs a newer version of the app.").assertExists()
    }

    @Test
    fun specRowsOpenTheirSourceAndCopyOnLongPress() {
        val shown = mutableListOf<Int>()
        val copied = mutableListOf<Int>()
        compose.setContent {
            PartloreTheme {
                PlSpecTable(
                    rows = listOf(PlSpecRow("Logic level", "3.3 V", null, "Source: Example Datasheet, page 28")),
                    onShowSource = { shown += it },
                    onCopy = { copied += it },
                )
            }
        }
        val row = compose.onNodeWithContentDescription("Logic level: 3.3 V. Source: Example Datasheet, page 28")
        row.assertHeightIsAtLeast(48.dp)
        row.performClick()
        row.performTouchInput { longClick() }
        assertEquals(listOf(0), shown)
        assertEquals(listOf(0), copied)
    }

    @Test
    fun glanceCellsReadAsLabelAndValue() {
        compose.setContent { PartloreTheme { PlGlanceGrid(listOf(PlGlanceItem("Logic", "3.3 V"))) } }
        compose.onNodeWithContentDescription("Logic: 3.3 V").assertExists()
    }

    @Test
    fun sectionChipsAreTabs() {
        var picked = -1
        compose.setContent {
            PartloreTheme {
                PlSectionChips(listOf("Specs", "Gotchas"), selectedIndex = 0, onSelect = {
                    picked =
                        it
                })
            }
        }
        compose.onNodeWithText("Specs").assertIsSelected().assert(hasRole(Role.Tab))
        compose.onNodeWithText("Gotchas").performClick()
        assertEquals(1, picked)
    }

    @Test
    fun gotchaCardsNameTheirSeverity() {
        compose.setContent {
            PartloreTheme {
                PlGotchaCard(
                    "Keep IO0 high at reset",
                    "Low starts download mode.",
                    Severity.Caution,
                    pins = listOf("IO0"),
                )
            }
        }
        compose.onNodeWithText("Caution").assertExists()
        compose.onNodeWithText("IO0").assertExists()
    }

    // A phone-wide screen: Robolectric's default is 320 dp, narrower than the card at 200 %.
    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi")
    fun aCompactCardGrowsWithTheTextSize() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                PartloreTheme {
                    PlPartCard("Example Board", "Wi-Fi", "board", VerificationLevel.Checked, onClick = {
                    }, compact = true)
                }
            }
        }
        // 176 dp at 100 % text (one-line "Not checked yet"), twice that at 200 %, so nothing breaks mid-word.
        compose.onNode(hasClickAction() and hasText("Example Board")).assertWidthIsEqualTo(352.dp)
    }

    // Three columns are too narrow for big text: "Bluetooth" broke mid-word at 200 %.
    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi")
    fun theGlanceGridUsesTwoColumnsWithLargeText() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                PartloreTheme { PlGlanceGrid(listOf(PlGlanceItem("Logic", "3.3 V"), PlGlanceItem("GPIO", "26"))) }
            }
        }
        // 411 dp wide, 8 dp between two cells: 201.5 dp each; the label sits inside 8 dp of padding on both sides.
        compose.onNodeWithContentDescription("Logic: 3.3 V").assertWidthIsEqualTo(185.5.dp)
    }

    // At 200 % text or in landscape a sheet is taller than the screen; its end must stay reachable.
    @Test
    fun sheetContentScrolls() {
        compose.setContent { PartloreTheme { PlSheet(onDismiss = {}) { Text("Inside the sheet") } } }
        compose.onNode(hasScrollAction() and hasAnyDescendant(hasText("Inside the sheet"))).assertExists()
    }
}
