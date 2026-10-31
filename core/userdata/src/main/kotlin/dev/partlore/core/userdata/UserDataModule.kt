package dev.partlore.core.userdata

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val settingsScope = named("userSettingsScope")

val userDataModule =
    module {
        // The settings file's own background work. Koin cancels it when it closes, which closes the file, so
        // a new DataStore (a new Koin, as each test makes) never finds an old one still holding the file.
        single(settingsScope) { CoroutineScope(Dispatchers.IO + SupervisorJob()) } withOptions {
            onClose { it?.cancel() }
        }
        single<DataStore<Preferences>> {
            userSettingsDataStore(scope = get(settingsScope)) {
                androidContext().preferencesDataStoreFile("user_settings")
            }
        }
        single<UserSettingsRepository> { DataStoreUserSettingsRepository(get()) }
    }
