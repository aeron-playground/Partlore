package dev.partlore.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class BundledAssetsTest {
    private val assets = ApplicationProvider.getApplicationContext<Context>().assets

    // Debug builds carry the preview pack (drafts), release builds the release pack (checked parts only).
    @Test
    fun theApkCarriesThePackForItsBuildType() {
        val manifest = assets.open("content/manifest.json").bufferedReader().use { it.readText() }
        assertTrue(manifest, manifest.contains("\"preview\" : ${BuildConfig.DEBUG}"))
        val file = checkNotNull(Regex("\"file\" : \"([^\"]+)\"").find(manifest)).groupValues[1]
        assets.open("content/$file").use { assertTrue(it.read() >= 0) }
    }
}
