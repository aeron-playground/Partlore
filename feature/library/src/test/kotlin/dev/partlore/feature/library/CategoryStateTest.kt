package dev.partlore.feature.library

import dev.partlore.core.testing.SampleContent
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryStateTest {
    private val page = SampleContent.boards

    @Test
    fun allShowsEveryPart() {
        assertEquals(page.parts, CategoryState(page).visibleParts)
    }

    @Test
    fun aChildShowsOnlyItsParts() {
        val state = CategoryState(page, CategoryFilter(child = "dev-boards"))
        assertEquals(listOf(SampleContent.devCard), state.visibleParts)
    }

    @Test
    fun aPartMustHaveEveryChosenTag() {
        assertEquals(
            listOf(SampleContent.devCard),
            CategoryState(page, CategoryFilter(tags = setOf("wifi", "3v3-logic"))).visibleParts,
        )
        assertEquals(emptyList<Any>(), CategoryState(page, CategoryFilter(tags = setOf("wifi", "usb-b"))).visibleParts)
    }
}
