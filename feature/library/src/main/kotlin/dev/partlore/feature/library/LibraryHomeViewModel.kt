package dev.partlore.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.partlore.core.content.ContentRepository
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.LibraryHome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryHomeViewModel(private val repository: ContentRepository) : ViewModel() {
    private val loaded = MutableStateFlow<ContentResult<LibraryHome>?>(null)

    /** Null until the pack has been read. */
    val state: StateFlow<ContentResult<LibraryHome>?> = loaded.asStateFlow()

    init {
        viewModelScope.launch { loaded.value = repository.library() }
    }
}
