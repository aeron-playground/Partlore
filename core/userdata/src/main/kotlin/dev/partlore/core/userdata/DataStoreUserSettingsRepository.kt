package dev.partlore.core.userdata

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dev.partlore.core.model.BuildInterest
import dev.partlore.core.model.MotionSetting
import dev.partlore.core.model.ThemeSetting
import dev.partlore.core.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreUserSettingsRepository(private val store: DataStore<Preferences>) : UserSettingsRepository {
    // A file that can't be read gives the defaults instead of crashing the app.
    override val settings: Flow<UserSettings> =
        store.data
            .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
            .map { it.toUserSettings() }

    override suspend fun setTheme(theme: ThemeSetting) {
        store.edit { it[Keys.THEME] = theme.name }
    }

    override suspend fun setMotion(motion: MotionSetting) {
        store.edit { it[Keys.MOTION] = motion.name }
    }

    override suspend fun setHaptics(enabled: Boolean) {
        store.edit { it[Keys.HAPTICS] = enabled }
    }

    override suspend fun setSounds(enabled: Boolean) {
        store.edit { it[Keys.SOUNDS] = enabled }
    }

    override suspend fun completeOnboarding(interests: Set<BuildInterest>) {
        store.edit {
            it[Keys.ONBOARDING_DONE] = true
            it[Keys.INTERESTS] = interests.mapTo(mutableSetOf()) { interest -> interest.name }
        }
    }
}

// Saved names are part of the on-device format: renaming a key or an enum value needs a migration.
private object Keys {
    val THEME = stringPreferencesKey("theme")
    val MOTION = stringPreferencesKey("motion")
    val HAPTICS = booleanPreferencesKey("haptics")
    val SOUNDS = booleanPreferencesKey("sounds")
    val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    val INTERESTS = stringSetPreferencesKey("interests")
}

private fun Preferences.toUserSettings(): UserSettings {
    val defaults = UserSettings()
    return UserSettings(
        theme = enumOrNull<ThemeSetting>(this[Keys.THEME]) ?: defaults.theme,
        motion = enumOrNull<MotionSetting>(this[Keys.MOTION]) ?: defaults.motion,
        haptics = this[Keys.HAPTICS] ?: defaults.haptics,
        sounds = this[Keys.SOUNDS] ?: defaults.sounds,
        onboardingDone = this[Keys.ONBOARDING_DONE] ?: defaults.onboardingDone,
        interests = this[Keys.INTERESTS].orEmpty().mapNotNullTo(mutableSetOf()) { enumOrNull<BuildInterest>(it) },
    )
}

private inline fun <reified E : Enum<E>> enumOrNull(name: String?): E? = enumValues<E>().firstOrNull { it.name == name }
