package dev.partlore.feature.library

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.ContentProblem
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.LibraryHome
import dev.partlore.core.testing.SampleContent
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h2400dp-xhdpi")
class LibraryHomeScreenTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    private val opened = mutableListOf<String>()
    private val links = mutableListOf<String>()
    private var searches = 0

    private fun show(state: ContentResult<LibraryHome>) {
        val uri = object : UriHandler {
            override fun openUri(uri: String) {
                links += uri
            }
        }
        compose.setContent {
            CompositionLocalProvider(LocalUriHandler provides uri) {
                PartloreTheme {
                    LibraryHomeScreen(
                        state = state,
                        onOpenCategory = { opened += "category:$it" },
                        onOpenPart = { opened += "part:$it" },
                        onOpenSearch = { searches++ },
                    )
                }
            }
        }
    }

    @Test
    fun showsStarterBoardsAndCategories() {
        show(ContentResult.Ok(SampleContent.library))
        compose.onNodeWithText("Starter boards").assertExists()
        compose.onNodeWithText("Example DevBoard V1").assertExists()
        compose.onNodeWithText("2 parts").assertExists()
        compose.onNodeWithText("Preview build: includes parts nobody has checked yet.").assertExists()
    }

    @Test
    fun aReleasePackHasNoPreviewNote() {
        show(ContentResult.Ok(SampleContent.library.copy(isPreview = false)))
        compose.onNodeWithText("Preview build: includes parts nobody has checked yet.").assertDoesNotExist()
    }

    @Test
    fun tappingOpensCategoriesPartsAndSearch() {
        show(ContentResult.Ok(SampleContent.library))
        compose.onNodeWithText("Boards").performClick()
        compose.onNodeWithText("Example DevBoard V1").performClick()
        compose.onNodeWithText("Search parts, pins, I²C addresses…").performClick()
        assertEquals(listOf("category:boards", "part:example/devboard-v1"), opened)
        assertEquals(1, searches)
    }

    @Test
    fun anEmptyLibraryReplacesTheBody() {
        show(ContentResult.Ok(SampleContent.emptyLibrary))
        compose.onNodeWithText("Categories").assertDoesNotExist()
        compose.onNodeWithText("How to add a part").performClick()
        assertEquals(listOf("https://github.com/aeron-playground/Partlore/blob/main/content/README.md"), links)
    }

    @Test
    fun failureShowsCopyDetails() {
        show(ContentResult.Failed(ContentProblem.Missing, "no pack"))
        compose.onNodeWithText("Content couldn't be loaded.").assertExists()
        compose.onNodeWithText("Copy details").assertExists()
    }
}
