package dev.partlore.app

import androidx.test.core.app.ApplicationProvider
import dev.partlore.core.content.ContentRepository
import dev.partlore.core.content.PackContentRepository
import dev.partlore.core.testing.FakeContentRepository
import dev.partlore.core.userdata.UserSettingsRepository
import dev.partlore.feature.library.CategoryViewModel
import dev.partlore.feature.library.LibraryHomeViewModel
import dev.partlore.feature.onboarding.OnboardingViewModel
import dev.partlore.feature.part.PartViewModel
import dev.partlore.feature.settings.SettingsViewModel
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class) // don't start the real app's Koin here
class KoinGraphTest {
    @After fun tearDown() = stopKoin()

    @Test
    fun everyScreenDependencyResolves() {
        // The fake keeps the ViewModels from opening the real pack, which Robolectric can't load.
        val koin =
            koinApplication {
                androidContext(ApplicationProvider.getApplicationContext())
                modules(appModules + module { single<ContentRepository> { FakeContentRepository() } })
            }.koin
        assertNotNull(koin.get<UserSettingsRepository>())
        assertNotNull(koin.get<MainViewModel>())
        assertNotNull(koin.get<SettingsViewModel>())
        assertNotNull(koin.get<OnboardingViewModel>())
        assertNotNull(koin.get<LibraryHomeViewModel>())
        assertNotNull(koin.get<CategoryViewModel> { parametersOf("boards") })
        assertNotNull(koin.get<PartViewModel> { parametersOf("example/devboard-v1") })
    }

    @Test
    fun theAppReadsTheBundledPack() {
        val koin =
            koinApplication {
                androidContext(ApplicationProvider.getApplicationContext())
                modules(appModules)
            }.koin
        assertTrue(koin.get<ContentRepository>() is PackContentRepository)
    }
}
