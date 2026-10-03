package dev.partlore.core.userdata

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val userDataModule =
    module {
        single<DataStore<Preferences>> {
            userSettingsDataStore { androidContext().preferencesDataStoreFile("user_settings") }
        }
        single<UserSettingsRepository> { DataStoreUserSettingsRepository(get()) }
    }
