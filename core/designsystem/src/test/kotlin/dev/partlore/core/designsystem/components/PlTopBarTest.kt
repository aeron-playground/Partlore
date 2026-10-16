package dev.partlore.core.designsystem.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.VerificationLevel
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val TITLE = "Example DevBoard V1"
private const val BADGE = "Not checked yet"

// Native graphics measures text as a phone does, which this test is about.
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xhdpi")
class PlTopBarTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    private fun show(fontScale: Float) = compose.setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
            PartloreTheme {
                PlTopBar(TITLE, onBack = {}, badge = { PlStatusBadge(VerificationLevel.Draft) })
            }
        }
    }

    @Test
    fun theBadgeSitsNextToTheTitleWhenBothFit() {
        show(fontScale = 1f)
        val title = compose.onNodeWithText(TITLE).fetchSemanticsNode().boundsInRoot
        val badge = compose.onNodeWithText(BADGE, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue("same line: badge top ${badge.top} < title bottom ${title.bottom}", badge.top < title.bottom)
    }

    // At 200 % text the badge would leave the title a few letters; it moves under the title instead.
    @Test
    fun theBadgeMovesUnderTheTitleWhenTheyDoNotFit() {
        show(fontScale = 2f)
        val title = compose.onNodeWithText(TITLE).fetchSemanticsNode().boundsInRoot
        val badge = compose.onNodeWithText(BADGE, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue("under: badge top ${badge.top} >= title bottom ${title.bottom}", badge.top >= title.bottom)
    }
}
