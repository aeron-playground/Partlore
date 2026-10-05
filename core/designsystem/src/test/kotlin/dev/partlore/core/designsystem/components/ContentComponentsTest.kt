package dev.partlore.core.designsystem.components

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.ContentProblem
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.VerificationLevel
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
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
}
