package dev.partlore.core.userdata

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dev.partlore.core.model.BuildInterest
import dev.partlore.core.model.MotionSetting
import dev.partlore.core.model.ThemeSetting
import dev.partlore.core.model.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

// UnconfinedTestDispatcher runs DataStore's work at once, so reads and writes need no manual advancing.
@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreUserSettingsRepositoryTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun storeFile() = File(tmp.root, "user_settings.preferences_pb")

    private fun newStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope, produceFile = ::storeFile)

    @Test
    fun freshInstallGivesDefaults() = runTest(UnconfinedTestDispatcher()) {
        val repo = DataStoreUserSettingsRepository(newStore(backgroundScope))
        assertEquals(UserSettings(), repo.settings.first())
    }

    @Test
    fun choicesAreSavedAndSurviveARestart() = runTest(UnconfinedTestDispatcher()) {
        val firstScope = CoroutineScope(Job() + UnconfinedTestDispatcher(testScheduler))
        val first = DataStoreUserSettingsRepository(newStore(firstScope))
        first.setTheme(ThemeSetting.Bench)
        first.setMotion(MotionSetting.Reduced)
        first.setHaptics(false)
        first.setSounds(true)
        first.completeOnboarding(setOf(BuildInterest.Esp32, BuildInterest.Pico))
        firstScope.cancel() // only one DataStore may own a file at a time

        val reopened = DataStoreUserSettingsRepository(newStore(backgroundScope))
        assertEquals(
            UserSettings(
                theme = ThemeSetting.Bench,
                motion = MotionSetting.Reduced,
                haptics = false,
                sounds = true,
                onboardingDone = true,
                interests = setOf(BuildInterest.Esp32, BuildInterest.Pico),
            ),
            reopened.settings.first(),
        )
    }

    @Test
    fun aDamagedFileIsReplacedWithDefaultsAndSavingStillWorks() = runTest(UnconfinedTestDispatcher()) {
        // Not a valid settings file: a field that claims 127 bytes, followed by 2.
        storeFile().writeBytes(byteArrayOf(0x0A, 0x7F, 0x13, 0x00))
        val repo = DataStoreUserSettingsRepository(userSettingsDataStore(backgroundScope, ::storeFile))
        assertEquals(UserSettings(), repo.settings.first())
        repo.setTheme(ThemeSetting.Bench)
        assertEquals(ThemeSetting.Bench, repo.settings.first().theme)
    }

    @Test
    fun unknownSavedValuesFallBackToDefaults() = runTest(UnconfinedTestDispatcher()) {
        val store = newStore(backgroundScope)
        store.edit {
            it[stringPreferencesKey("theme")] = "Sepia"
            it[stringPreferencesKey("motion")] = ""
            it[stringSetPreferencesKey("interests")] = setOf("Esp32", "Teensy")
        }
        val settings = DataStoreUserSettingsRepository(store).settings.first()
        assertEquals(ThemeSetting.System, settings.theme)
        assertEquals(MotionSetting.System, settings.motion)
        assertEquals(setOf(BuildInterest.Esp32), settings.interests)
    }
}
