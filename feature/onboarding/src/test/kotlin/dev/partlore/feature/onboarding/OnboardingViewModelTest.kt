package dev.partlore.feature.onboarding

import dev.partlore.core.model.BuildInterest
import dev.partlore.core.testing.FakeUserSettingsRepository
import dev.partlore.core.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test
    fun finishingSavesTheAnswersAndMarksOnboardingDone() {
        val repo = FakeUserSettingsRepository()
        OnboardingViewModel(repo).finish(setOf(BuildInterest.Pico))
        assertTrue(repo.settings.value.onboardingDone)
        assertEquals(setOf(BuildInterest.Pico), repo.settings.value.interests)
    }
}
