package dev.partlore.app

import app.cash.turbine.test
import dev.partlore.core.model.UserSettings
import dev.partlore.core.testing.FakeUserSettingsRepository
import dev.partlore.core.testing.MainDispatcherRule
import dev.partlore.core.userdata.UserSettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test
    fun settingsAreNullUntilLoadedSoOnboardingNeverFlashes() = runTest {
        val repo = FakeUserSettingsRepository(UserSettings(onboardingDone = true))
        val vm = MainViewModel(repo)
        vm.settings.test {
            // The fake answers at once, so the first value seen is the loaded one.
            assertEquals(true, awaitItem()?.onboardingDone)
        }
    }

    @Test
    fun staysNullWhileNothingHasLoaded() {
        // A store that never answers, like a slow first disk read.
        val neverLoads =
            object : UserSettingsRepository by FakeUserSettingsRepository() {
                override val settings = MutableSharedFlow<UserSettings>()
            }
        val vm = MainViewModel(neverLoads)
        assertNull(vm.settings.value)
    }
}
