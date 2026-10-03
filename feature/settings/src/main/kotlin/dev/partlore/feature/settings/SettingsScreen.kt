package dev.partlore.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.partlore.core.designsystem.components.PlSegmented
import dev.partlore.core.designsystem.components.PlSwitchRow
import dev.partlore.core.designsystem.components.PlTextButton
import dev.partlore.core.designsystem.components.PlTopBar
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.MotionSetting
import dev.partlore.core.model.ThemeSetting
import dev.partlore.core.model.UserSettings
import org.koin.compose.viewmodel.koinViewModel

private const val SOURCE_URL = "https://github.com/aeron-playground/Partlore"

@Composable
fun SettingsRoute(
    versionName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    SettingsScreen(
        settings = settings,
        versionName = versionName,
        onBack = onBack,
        onChange = viewModel::onChange,
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    settings: UserSettings,
    versionName: String,
    onBack: () -> Unit,
    onChange: (SettingsChange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PartloreTheme.spacing
    val themes = ThemeSetting.entries
    val motions = MotionSetting.entries
    Column(modifier.fillMaxSize().background(PartloreTheme.colors.bg)) {
        PlTopBar(title = stringResource(R.string.settings_title), onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = spacing.space16)) {
            SectionLabel(stringResource(R.string.settings_appearance))
            PlSegmented(
                options = themes.map { stringResource(it.labelRes()) },
                selectedIndex = themes.indexOf(settings.theme),
                onSelect = { onChange(SettingsChange.Theme(themes[it])) },
            )
            SectionLabel(stringResource(R.string.settings_motion))
            PlSegmented(
                options = motions.map { stringResource(it.labelRes()) },
                selectedIndex = motions.indexOf(settings.motion),
                onSelect = { onChange(SettingsChange.Motion(motions[it])) },
            )
            PlSwitchRow(
                stringResource(R.string.settings_haptics),
                checked = settings.haptics,
                onCheckedChange = { onChange(SettingsChange.Haptics(it)) },
            )
            PlSwitchRow(
                title = stringResource(R.string.settings_sounds),
                subtitle = stringResource(R.string.settings_sounds_subtitle),
                checked = settings.sounds,
                onCheckedChange = { onChange(SettingsChange.Sounds(it)) },
            )
            SectionLabel(stringResource(R.string.settings_privacy))
            BodyText(stringResource(R.string.settings_privacy_body))
            SectionLabel(stringResource(R.string.settings_about))
            BodyText(stringResource(R.string.settings_version, versionName))
            val uriHandler = LocalUriHandler.current
            PlTextButton(stringResource(R.string.settings_source_code), onClick = { uriHandler.openUri(SOURCE_URL) })
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = PartloreTheme.typography.label,
        color = PartloreTheme.colors.textSecondary,
        modifier =
        modifier
            .padding(top = PartloreTheme.spacing.space24, bottom = PartloreTheme.spacing.space8)
            .semantics { heading() },
    )
}

@Composable
private fun BodyText(text: String, modifier: Modifier = Modifier) {
    Text(text, style = PartloreTheme.typography.bodyM, color = PartloreTheme.colors.textPrimary, modifier = modifier)
}

private fun ThemeSetting.labelRes(): Int = when (this) {
    ThemeSetting.System -> R.string.settings_theme_system
    ThemeSetting.Light -> R.string.settings_theme_light
    ThemeSetting.Dark -> R.string.settings_theme_dark
    ThemeSetting.Bench -> R.string.settings_theme_bench
}

private fun MotionSetting.labelRes(): Int = when (this) {
    MotionSetting.System -> R.string.settings_motion_system
    MotionSetting.Reduced -> R.string.settings_motion_reduced
    MotionSetting.Full -> R.string.settings_motion_full
}
