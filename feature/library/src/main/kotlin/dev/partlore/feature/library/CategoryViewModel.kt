package dev.partlore.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.partlore.core.content.ContentRepository
import dev.partlore.core.model.ContentResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryViewModel(private val id: String, private val repository: ContentRepository) : ViewModel() {
    private val loaded = MutableStateFlow<ContentResult<CategoryState>?>(null)

    /** Null until the pack has been read. */
    val state: StateFlow<ContentResult<CategoryState>?> = loaded.asStateFlow()

    init {
        viewModelScope.launch {
            loaded.value =
                when (val result = repository.category(id)) {
                    is ContentResult.Ok -> ContentResult.Ok(CategoryState(result.value))
                    ContentResult.NotFound -> ContentResult.NotFound
                    is ContentResult.Failed -> result
                }
        }
    }

    fun onAction(action: CategoryAction) {
        loaded.update { current ->
            if (current is ContentResult.Ok) {
                ContentResult.Ok(current.value.copy(filter = current.value.filter.after(action)))
            } else {
                current
            }
        }
    }

    private fun CategoryFilter.after(action: CategoryAction): CategoryFilter = when (action) {
        is CategoryAction.SelectChild -> copy(child = action.childId)

        is CategoryAction.ToggleTag -> copy(
            tags = if (action.tagId in
                tags
            ) {
                tags - action.tagId
            } else {
                tags + action.tagId
            },
        )

        CategoryAction.ClearFilters -> CategoryFilter()
    }
}
