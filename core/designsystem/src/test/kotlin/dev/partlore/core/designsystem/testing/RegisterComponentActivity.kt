package dev.partlore.core.designsystem.testing

import android.content.ComponentName
import androidx.activity.ComponentActivity
import org.junit.rules.TestRule
import org.junit.runners.model.Statement
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/**
 * `createComposeRule()` opens a blank [ComponentActivity], which library modules don't declare.
 * This rule registers it with Robolectric. Give it a lower rule order than the compose rule.
 */
fun registerComponentActivity(): TestRule = TestRule { base, _ ->
    object : Statement() {
        override fun evaluate() {
            val app = RuntimeEnvironment.getApplication()
            shadowOf(app.packageManager)
                .addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
            base.evaluate()
        }
    }
}
