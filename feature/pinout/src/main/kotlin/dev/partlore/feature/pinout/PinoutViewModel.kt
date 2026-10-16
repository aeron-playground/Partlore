package dev.partlore.feature.pinout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.partlore.core.content.ContentRepository
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.Pinout
import dev.partlore.core.model.PinoutLayout
import dev.partlore.core.model.Rotation
import dev.partlore.core.model.ThemeSetting
import dev.partlore.core.model.UserSettings
import dev.partlore.core.model.labelMode
import dev.partlore.core.model.labelModes
import dev.partlore.core.userdata.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// What lives only while the viewer is open: rotation (null = the part's default), filter, open pin, table.
private data class Local(
    val rotation: Rotation? = null,
    val filter: PinFilter? = null,
    val selected: String? = null,
    val table: Boolean = false,
)

class PinoutViewModel(partId: String, content: ContentRepository, private val settings: UserSettingsRepository) :
    ViewModel() {
    private val loaded = MutableStateFlow<ContentResult<Pinout>?>(null)
    private val local = MutableStateFlow(Local())

    /** Null until the pack has been read. */
    val state: StateFlow<ContentResult<PinoutState>?> =
        combine(loaded, settings.settings, local) { result, prefs, ui -> result?.let { toState(it, prefs, ui) } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch { loaded.value = content.pinout(partId) }
    }

    fun onAction(action: PinoutAction) {
        val ready = (state.value as? ContentResult.Ok)?.value ?: return
        when (action) {
            is PinoutAction.Select -> local.update { it.copy(selected = action.pinId) }
            is PinoutAction.Filter -> local.update { it.copy(filter = action.filter) }
            is PinoutAction.Labels -> viewModelScope.launch { settings.setPinLabel(action.mode) }
            PinoutAction.Rotate -> local.update { it.copy(rotation = ready.board.rotation.next()) }
            PinoutAction.ToggleTable -> local.update { it.copy(table = !it.table) }
            PinoutAction.ToggleKeepScreenOn -> viewModelScope.launch { settings.setKeepScreenOn(!ready.keepScreenOn) }
            PinoutAction.ToggleRedLight -> viewModelScope.launch { settings.setRedLight(!ready.redLight) }
        }
    }

    private fun toState(result: ContentResult<Pinout>, prefs: UserSettings, ui: Local): ContentResult<PinoutState> =
        when (result) {
            is ContentResult.Ok -> ContentResult.Ok(stateOf(result.value, prefs, ui))
            ContentResult.NotFound -> ContentResult.NotFound
            is ContentResult.Failed -> result
        }

    private fun stateOf(pinout: Pinout, prefs: UserSettings, ui: Local): PinoutState {
        val bench = prefs.theme == ThemeSetting.Bench
        return PinoutState(
            pinout = pinout,
            board = PinoutLayout.place(pinout, ui.rotation ?: PinoutLayout.defaultRotation(pinout)),
            labelMode = pinout.labelMode(prefs.pinLabel),
            labelModes = pinout.labelModes(),
            filters = pinout.filters(),
            filter = ui.filter,
            selected = ui.selected?.let { id -> pinout.pins.firstOrNull { it.id == id } },
            table = ui.table,
            keepScreenOn = prefs.keepScreenOn ?: bench,
            redLight = prefs.redLight,
            bench = bench,
            haptics = prefs.haptics,
        )
    }
}
