package dev.partlore.feature.library

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.ContentResult
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
class CategoryScreenTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    private val actions = mutableListOf<CategoryAction>()
    private val opened = mutableListOf<String>()

    private fun show(state: ContentResult<CategoryState>) = compose.setContent {
        PartloreTheme {
            CategoryScreen(state, onBack = {}, onOpenPart = { opened += it }, onAction = { actions += it })
        }
    }

    @Test
    fun showsChipsAndParts() {
        show(ContentResult.Ok(CategoryState(SampleContent.boards)))
        compose.onNodeWithText("Boards").assertExists()
        compose.onNodeWithText("All").assertIsSelected()
        compose.onNodeWithText("Example Classic Board").assertExists()
        compose.onNodeWithText("Example DevBoard V1").performClick()
        assertEquals(listOf("example/devboard-v1"), opened)
    }

    @Test
    fun chipsSendFilterActions() {
        show(ContentResult.Ok(CategoryState(SampleContent.boards)))
        compose.onNodeWithText("Dev boards").performClick()
        compose.onNodeWithText("Wi-Fi").performClick()
        assertEquals(listOf(CategoryAction.SelectChild("dev-boards"), CategoryAction.ToggleTag("wifi")), actions)
    }

    @Test
    fun filtersThatMatchNothingOfferToClearThem() {
        show(ContentResult.Ok(CategoryState(SampleContent.boards, CategoryFilter(tags = setOf("wifi", "usb-b")))))
        compose.onNodeWithText("No parts match these filters.").assertExists()
        compose.onNodeWithText("Clear filters").performClick()
        assertEquals(listOf<CategoryAction>(CategoryAction.ClearFilters), actions)
    }

    @Test
    fun anUnknownCategorySaysSo() {
        show(ContentResult.NotFound)
        compose.onNodeWithText("This category isn't in your content pack.").assertExists()
    }
}
