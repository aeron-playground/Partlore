package dev.partlore.app

import android.content.Context
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import dev.partlore.core.content.ContentRepository
import dev.partlore.core.testing.FakeContentRepository
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

private const val LOAD_TIMEOUT_MS = 5_000L

// The real app: real Koin, real DataStore, a fresh install. Content comes from the fake repository.
@RunWith(RobolectricTestRunner::class)
@Config(application = PartloreApplication::class)
class MainActivityTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    // Robolectric can't load the bundled SQLite library; the real reader is tested in core:content.
    // The Library ViewModel is created after onboarding, so this override is in place in time.
    @Before
    fun useSampleContent() {
        loadKoinModules(module { single<ContentRepository> { FakeContentRepository() } })
    }

    @After
    fun tearDown() {
        stopKoin()
        val context = ApplicationProvider.getApplicationContext<Context>()
        File(context.filesDir, "datastore/user_settings.preferences_pb").delete()
    }

    @Test
    fun firstLaunchShowsOnboardingThenTheLibrary() {
        // Settings are read from disk on a background thread, so wait for each screen.
        waitForText("A reference for the bench")
        compose.onNodeWithText("Skip").performClick()
        waitForText("Starter boards")
        compose.onNodeWithText("Search").assertExists()
    }

    private fun waitForText(text: String) =
        compose.waitUntil(LOAD_TIMEOUT_MS) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
}
