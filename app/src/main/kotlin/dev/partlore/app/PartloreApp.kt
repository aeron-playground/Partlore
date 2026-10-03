package dev.partlore.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.partlore.app.shell.PartloreShell
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.feature.onboarding.OnboardingRoute
import dev.partlore.feature.settings.SettingsRoute
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PartloreApp(modifier: Modifier = Modifier, viewModel: MainViewModel = koinViewModel()) {
    val loaded by viewModel.settings.collectAsStateWithLifecycle()
    // Until settings are read, the window background shows; nothing guesses theme or onboarding.
    val settings = loaded ?: return
    PartloreTheme(
        mode = settings.theme.toThemeMode(isSystemInDarkTheme()),
        motionPreference = settings.motion.toMotionPreference(),
    ) {
        if (settings.onboardingDone) {
            PartloreShell(settings = { onBack ->
                SettingsRoute(versionName = BuildConfig.VERSION_NAME, onBack = onBack)
            }, modifier = modifier)
        } else {
            OnboardingRoute(modifier = modifier)
        }
    }
}
