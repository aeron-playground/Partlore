package dev.partlore.feature.settings

import app.cash.turbine.test
import dev.partlore.core.model.MotionSetting
import dev.partlore.core.model.ThemeSetting
import dev.partlore.core.model.UserSettings
import dev.partlore.core.testing.FakeUserSettingsRepository
import dev.partlore.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test
    fun choicesReachTheRepositoryAndComeBackAsState() = runTest {
        val repo = FakeUserSettingsRepository()
        val vm = SettingsViewModel(repo)
        vm.settings.test {
            assertEquals(UserSettings(), awaitItem())
            vm.onChange(SettingsChange.Theme(ThemeSetting.Bench))
            assertEquals(ThemeSetting.Bench, awaitItem().theme)
            vm.onChange(SettingsChange.Motion(MotionSetting.Reduced))
            assertEquals(MotionSetting.Reduced, awaitItem().motion)
            vm.onChange(SettingsChange.Haptics(false))
            assertEquals(false, awaitItem().haptics)
            vm.onChange(SettingsChange.Sounds(true))
            assertEquals(true, awaitItem().sounds)
        }
        assertEquals(ThemeSetting.Bench, repo.settings.value.theme)
    }
}
