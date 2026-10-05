package dev.partlore.feature.library

import dev.partlore.core.model.CategoryPage
import dev.partlore.core.model.PartCard

/** The chips that are on: one child category (null means All) and any number of tags. */
data class CategoryFilter(val child: String? = null, val tags: Set<String> = emptySet())

data class CategoryState(val page: CategoryPage, val filter: CategoryFilter = CategoryFilter()) {
    /** The parts in the chosen child category that have every chosen tag. */
    val visibleParts: List<PartCard>
        get() {
            val inChild = filter.child?.let { page.partsByChild[it].orEmpty() }
            return page.parts.filter { card ->
                (inChild == null || card.id in inChild) && card.tags.map { it.id }.containsAll(filter.tags)
            }
        }
}

sealed interface CategoryAction {
    data class SelectChild(val childId: String?) : CategoryAction

    data class ToggleTag(val tagId: String) : CategoryAction

    data object ClearFilters : CategoryAction
}
