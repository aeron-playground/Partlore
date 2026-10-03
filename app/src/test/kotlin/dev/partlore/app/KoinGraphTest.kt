package dev.partlore.app

import androidx.test.core.app.ApplicationProvider
import dev.partlore.core.userdata.UserSettingsRepository
import dev.partlore.feature.onboarding.OnboardingViewModel
import dev.partlore.feature.settings.SettingsViewModel
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class) // don't start the real app's Koin here
class KoinGraphTest {
    @After fun tearDown() = stopKoin()

    @Test
    fun everyScreenDependencyResolves() {
        val koin =
            koinApplication {
                androidContext(ApplicationProvider.getApplicationContext())
                modules(appModules)
            }.koin
        assertNotNull(koin.get<UserSettingsRepository>())
        assertNotNull(koin.get<MainViewModel>())
        assertNotNull(koin.get<SettingsViewModel>())
        assertNotNull(koin.get<OnboardingViewModel>())
    }
}
