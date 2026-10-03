package dev.partlore.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.partlore.core.model.UserSettings
import dev.partlore.core.userdata.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(repository: UserSettingsRepository) : ViewModel() {
    /** `null` until the saved settings are read, so the first frame never guesses. */
    val settings: StateFlow<UserSettings?> = repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
