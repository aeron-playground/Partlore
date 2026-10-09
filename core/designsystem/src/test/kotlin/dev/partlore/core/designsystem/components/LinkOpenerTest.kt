package dev.partlore.core.designsystem.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.testing.registerComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class LinkOpenerTest {
    private val compose = createComposeRule()

    @get:Rule val rules: RuleChain = RuleChain.outerRule(registerComponentActivity()).around(compose)

    private fun show(handler: UriHandler) = compose.setContent {
        CompositionLocalProvider(LocalUriHandler provides handler) {
            PartloreTheme {
                val open = rememberLinkOpener()
                PlButton("Open", onClick = { open("https://example.com/part") })
            }
        }
    }

    @Test
    fun aLinkOpensInTheBrowser() {
        val opened = mutableListOf<String>()
        show(
            object : UriHandler {
                override fun openUri(uri: String) {
                    opened += uri
                }
            },
        )
        compose.onNodeWithText("Open").performClick()
        assertEquals(listOf("https://example.com/part"), opened)
    }

    // Android's link handler throws IllegalArgumentException when no app can open links.
    @Test
    fun withoutABrowserTheLinkIsCopiedInsteadOfCrashing() {
        show(
            object : UriHandler {
                override fun openUri(uri: String): Unit = throw IllegalArgumentException("Can't open $uri.")
            },
        )
        compose.onNodeWithText("Open").performClick()
        compose.waitForIdle()
        assertEquals("No app on this phone can open links. The link was copied.", ShadowToast.getTextOfLatestToast())
        val clipboard = ApplicationProvider.getApplicationContext<Context>().getSystemService(
            ClipboardManager::class.java,
        )
        assertEquals("https://example.com/part", clipboard.primaryClip?.getItemAt(0)?.text.toString())
    }
}
