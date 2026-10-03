package dev.partlore.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.partlore.core.model.UserSettings
import dev.partlore.core.userdata.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MS = 5_000L

class SettingsViewModel(private val repository: UserSettingsRepository) : ViewModel() {
    val settings: StateFlow<UserSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UserSettings())

    fun onChange(change: SettingsChange) {
        viewModelScope.launch {
            when (change) {
                is SettingsChange.Theme -> repository.setTheme(change.theme)
                is SettingsChange.Motion -> repository.setMotion(change.motion)
                is SettingsChange.Haptics -> repository.setHaptics(change.enabled)
                is SettingsChange.Sounds -> repository.setSounds(change.enabled)
            }
        }
    }
}
