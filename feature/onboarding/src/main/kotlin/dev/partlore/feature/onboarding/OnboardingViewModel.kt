package dev.partlore.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.partlore.core.model.BuildInterest
import dev.partlore.core.userdata.UserSettingsRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(private val repository: UserSettingsRepository) : ViewModel() {
    fun finish(interests: Set<BuildInterest>) {
        viewModelScope.launch { repository.completeOnboarding(interests) }
    }
}
