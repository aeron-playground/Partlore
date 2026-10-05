package dev.partlore.feature.part

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.Cite
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.FileStatus
import dev.partlore.core.model.I2cRow
import dev.partlore.core.model.PartPage
import dev.partlore.core.model.VerificationLevel
import dev.partlore.core.testing.SampleContent
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private const val SUPPLY = "Supply voltage: 3 – 3.6 V (typ 3.3). Source: Example DevBoard Datasheet, page 28"

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h2400dp-xhdpi")
class PartScreenTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    private val opened = mutableListOf<String>()
    private val links = mutableListOf<String>()

    private fun show(state: ContentResult<PartPage> = ContentResult.Ok(SampleContent.devBoard)) {
        val uri = object : UriHandler {
            override fun openUri(uri: String) {
                links += uri
            }
        }
        compose.setContent {
            CompositionLocalProvider(LocalUriHandler provides uri) {
                PartloreTheme { PartScreen(state, onBack = {}, onOpenPart = { opened += it }) }
            }
        }
    }

    private fun scrollTo(matcher: SemanticsMatcher) =
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(matcher)

    @Test
    fun theHeroLeadsWithTheLeastCheckedStatus() {
        show()
        compose.onNodeWithText("Also called EDB1").assertExists()
        compose.onNodeWithText("Example Labs · Board").assertExists()
        compose.onAllNodesWithText("Not checked yet").onFirst().assertExists()
    }

    @Test
    fun dangerGotchasAreBannersAndCounted() {
        show()
        compose.onNodeWithContentDescription("Danger: 1").assertExists()
        compose.onAllNodesWithText("Power the board from one source only").onFirst().assertExists()
    }

    @Test
    fun emptySectionsAreLeftOut() {
        show()
        scrollTo(hasText("Key specs"))
        compose.onNodeWithText("I²C addresses").assertDoesNotExist()
    }

    @Test
    fun i2cAddressesShowWhenThePartHasThem() {
        show(
            ContentResult.Ok(
                SampleContent.devBoard.copy(i2c = listOf(I2cRow(0x3C, true, null, Cite("s1", 28, null, null)))),
            ),
        )
        scrollTo(hasText("0x3C"))
    }

    @Test
    fun tappingASpecShowsItsSource() {
        show()
        scrollTo(hasContentDescription(SUPPLY))
        compose.onNodeWithContentDescription(SUPPLY).performClick()
        compose.onNodeWithText("Retrieved 2026-10-04").assertExists()
        compose.onNodeWithText("Open the source").performClick()
        // The sheet is its own window with Android's own link handler, so it starts the browser directly.
        val started = shadowOf(ApplicationProvider.getApplicationContext<Application>()).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, started?.action)
        assertEquals("https://example.com/devboard.pdf", started?.data?.toString())
    }

    @Test
    fun longPressCopiesTheValue() {
        show()
        scrollTo(hasContentDescription(SUPPLY))
        compose.onNodeWithContentDescription(SUPPLY).performTouchInput { longClick() }
        compose.waitForIdle()
        val clipboard = ApplicationProvider.getApplicationContext<Context>().getSystemService(
            ClipboardManager::class.java,
        )
        assertEquals("3 – 3.6 V (typ 3.3)", clipboard.primaryClip?.getItemAt(0)?.text.toString())
    }

    @Test
    fun reportAProblemOpensAPrefilledIssue() {
        show()
        scrollTo(hasText("Report a problem"))
        compose.onNodeWithText("Report a problem").performClick()
        assertEquals(listOf(GithubLinks.reportProblem("example/devboard-v1", "0.1.0")), links)
    }

    @Test
    fun relatedPartsOpen() {
        show()
        scrollTo(hasText("Example Classic Board"))
        compose.onNodeWithText("Example Classic Board").performClick()
        assertEquals(listOf("example/classic-board"), opened)
    }

    @Test
    fun aCheckedFileSaysWhoCheckedItWhenAndAgainstWhat() {
        val checked = FileStatus(VerificationLevel.Checked, listOf("alice"), "2026-10-04", listOf("s1"), null)
        show(ContentResult.Ok(SampleContent.devBoard.copy(partStatus = checked)))
        scrollTo(hasText("Checked by alice on 2026-10-04, against Example DevBoard Datasheet."))
    }

    @Test
    fun aCheckedPartSaysCheckedWithoutACount() {
        val checked = FileStatus(VerificationLevel.Checked, listOf("alice"), "2026-10-04", listOf("s1"), null)
        show(
            ContentResult.Ok(
                SampleContent.devBoard.copy(partStatus = checked, pinsStatus = checked, gotchasStatus = checked),
            ),
        )
        compose.onAllNodesWithText("Checked").onFirst().assertExists()
        compose.onNodeWithText("Checked by 1 person").assertDoesNotExist()
    }

    @Test
    fun theStatusBadgeExplainsItself() {
        show()
        compose.onAllNodesWithText("Not checked yet").onFirst().performClick()
        compose.onNodeWithText("What this status means").assertExists()
    }

    @Test
    fun anUnknownPartSaysSo() {
        show(ContentResult.NotFound)
        compose.onNodeWithText("This part isn't in your content pack.").assertExists()
    }
}
