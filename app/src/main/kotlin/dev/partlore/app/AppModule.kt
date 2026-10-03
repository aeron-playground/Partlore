package dev.partlore.app

import dev.partlore.core.userdata.userDataModule
import dev.partlore.feature.onboarding.onboardingModule
import dev.partlore.feature.settings.settingsModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val appModule = module { viewModelOf(::MainViewModel) }

val appModules = listOf(userDataModule, settingsModule, onboardingModule, appModule)
