package dev.partlore.feature.part

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.partlore.core.content.ContentRepository
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.PartPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PartViewModel(private val id: String, private val repository: ContentRepository) : ViewModel() {
    private val loaded = MutableStateFlow<ContentResult<PartPage>?>(null)

    /** Null until the pack has been read. */
    val state: StateFlow<ContentResult<PartPage>?> = loaded.asStateFlow()

    init {
        viewModelScope.launch { loaded.value = repository.part(id) }
    }
}
