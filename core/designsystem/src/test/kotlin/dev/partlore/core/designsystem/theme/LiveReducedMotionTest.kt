package dev.partlore.core.designsystem.theme

import android.os.Looper
import android.provider.Settings
import androidx.compose.ui.test.junit4.createComposeRule
import dev.partlore.core.designsystem.testing.registerComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/** Turning system animations off while the app is open must switch the theme to reduced motion. */
@RunWith(RobolectricTestRunner::class)
class LiveReducedMotionTest {
    @get:Rule(order = 0)
    val activity = registerComponentActivity()

    @get:Rule(order = 1)
    val compose = createComposeRule()

    @Test
    fun followsTheSystemSettingWhileShowing() {
        val resolver = RuntimeEnvironment.getApplication().contentResolver
        Settings.Global.putFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        var reduced: Boolean? = null
        compose.setContent { PartloreTheme { reduced = PartloreTheme.motion.reduced } }
        compose.waitForIdle()
        assertEquals(false, reduced)

        // The user turns animations off in system settings while the screen is showing.
        Settings.Global.putFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
        resolver.notifyChange(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), null)
        shadowOf(Looper.getMainLooper()).idle()
        compose.waitForIdle()
        assertEquals(true, reduced)
    }
}
