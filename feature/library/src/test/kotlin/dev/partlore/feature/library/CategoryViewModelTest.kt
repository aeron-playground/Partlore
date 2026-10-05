package dev.partlore.feature.library

import dev.partlore.core.model.ContentResult
import dev.partlore.core.testing.FakeContentRepository
import dev.partlore.core.testing.MainDispatcherRule
import dev.partlore.core.testing.SampleContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CategoryViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test
    fun theCategoryArrivesWithNoFilters() {
        val vm = CategoryViewModel("boards", FakeContentRepository())
        assertEquals(ContentResult.Ok(CategoryState(SampleContent.boards)), vm.state.value)
    }

    @Test
    fun anUnknownCategoryIsNotFound() {
        assertEquals(ContentResult.NotFound, CategoryViewModel("nope", FakeContentRepository()).state.value)
    }

    @Test
    fun actionsChangeTheFilter() {
        val vm = CategoryViewModel("boards", FakeContentRepository())
        vm.onAction(CategoryAction.SelectChild("dev-boards"))
        vm.onAction(CategoryAction.ToggleTag("wifi"))
        assertEquals(CategoryFilter("dev-boards", setOf("wifi")), filter(vm))
        vm.onAction(CategoryAction.ToggleTag("wifi"))
        assertEquals(CategoryFilter("dev-boards"), filter(vm))
        vm.onAction(CategoryAction.ClearFilters)
        assertEquals(CategoryFilter(), filter(vm))
    }

    private fun filter(vm: CategoryViewModel) = (vm.state.value as ContentResult.Ok).value.filter
}
