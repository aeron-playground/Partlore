package dev.partlore.feature.pinout

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.partlore.core.designsystem.components.PlEmptyState
import dev.partlore.core.designsystem.components.PlErrorState
import dev.partlore.core.designsystem.components.PlIcons
import dev.partlore.core.designsystem.components.PlPinoutBoard
import dev.partlore.core.designsystem.components.PlSheet
import dev.partlore.core.designsystem.components.PlStatusBadge
import dev.partlore.core.designsystem.components.PlTopBar
import dev.partlore.core.designsystem.components.rememberBoardPins
import dev.partlore.core.designsystem.components.rememberLinkOpener
import dev.partlore.core.designsystem.components.rememberPlZoomState
import dev.partlore.core.designsystem.theme.PartloreHaptic
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreRedLight
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.designsystem.theme.rememberPartloreHaptics
import dev.partlore.core.model.ContentResult
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val HOW_TO_ADD_URL = "https://github.com/aeron-playground/Partlore/blob/main/content/README.md"

@Composable
fun PinoutRoute(
    partId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    appVersion: String? = null,
    viewModel: PinoutViewModel = koinViewModel(parameters = { parametersOf(partId) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PinoutScreen(state, onBack, viewModel::onAction, modifier, appVersion)
}

@Composable
fun PinoutScreen(
    state: ContentResult<PinoutState>?,
    onBack: () -> Unit,
    onAction: (PinoutAction) -> Unit,
    modifier: Modifier = Modifier,
    appVersion: String? = null,
) {
    val ready = (state as? ContentResult.Ok)?.value
    PartloreRedLight(enabled = ready?.redLight == true) {
        // The app hides its tabs here, so the viewer keeps its bottom row clear of the phone's own navigation bar.
        Column(
            modifier.fillMaxSize().background(PartloreTheme.colors.bg).windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            val status = ready?.pinout?.status
            PlTopBar(
                title = ready?.pinout?.partName.orEmpty(),
                onBack = onBack,
                badge = status?.let { { PlStatusBadge(it.level) } },
            )
            when (state) {
                null -> Unit
                is ContentResult.Ok -> PinoutBody(state.value, onAction)
                ContentResult.NotFound -> NoPinout()
                is ContentResult.Failed -> PlErrorState(state, appVersion = appVersion)
            }
        }
    }
}

@Composable
private fun PinoutBody(state: PinoutState, onAction: (PinoutAction) -> Unit, modifier: Modifier = Modifier) {
    KeepScreenOn(state.keepScreenOn)
    val haptics = rememberPartloreHaptics(state.haptics)
    val select: (String?) -> Unit = { id ->
        if (id != null) haptics.perform(PartloreHaptic.PinTap)
        onAction(PinoutAction.Select(id))
    }
    // Back closes the open pin first (the phone sheet handles Back itself).
    BackHandler(enabled = state.selected != null) { onAction(PinoutAction.Select(null)) }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val wide = maxWidth >= PartloreLayout.expandedWidth
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f)) {
                if (!state.pinsChecked) UncheckedWarning()
                Controls(state, onAction)
                if (state.board.unplacedHeaders.isNotEmpty()) TableOnlyNote()
                PinsArea(state, select, Modifier.weight(1f))
                Filters(state, onAction)
            }
            if (wide && state.selected != null) SidePanel(state, onClose = { onAction(PinoutAction.Select(null)) })
        }
        if (!wide && state.selected != null) {
            PlSheet(onDismiss = { onAction(PinoutAction.Select(null)) }) { PinDetails(state) }
        }
    }
}

@Composable
private fun PinsArea(state: PinoutState, select: (String?) -> Unit, modifier: Modifier = Modifier) {
    val pins = rememberBoardPins(state.pinout, state.labelMode)
    val zoom = rememberPlZoomState()
    Crossfade(
        state.table,
        modifier.fillMaxWidth().clipToBounds(),
        PartloreTheme.motion.fade(),
        label = "view",
    ) { table ->
        if (table) {
            PinTable(state, pins, onPick = select)
        } else {
            PlPinoutBoard(
                board = state.board,
                pins = pins,
                modifier = Modifier.fillMaxSize(),
                dimmed = state.dimmed,
                selected = state.selected?.id,
                large = state.bench,
                zoom = zoom,
                onPinClick = select,
            )
        }
    }
}

/** Keeps the screen awake while the viewer is shown; leaving the viewer always lets it sleep again. */
@Composable
private fun KeepScreenOn(on: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, on) {
        view.keepScreenOn = on
        onDispose { view.keepScreenOn = false }
    }
}

@Composable
private fun NoPinout(modifier: Modifier = Modifier) {
    val openLink = rememberLinkOpener()
    PlEmptyState(
        message = stringResource(R.string.pinout_none),
        modifier = modifier,
        actionLabel = stringResource(R.string.pinout_how_to_add),
        onAction = { openLink(HOW_TO_ADD_URL) },
    )
}

/** The open pin on wide screens, beside the board, with a close button at the top. */
@Composable
private fun SidePanel(state: PinoutState, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.width(PartloreLayout.pinDetailsWidth).fillMaxHeight()) {
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
            Icon(
                painter = painterResource(PlIcons.Close),
                contentDescription = stringResource(R.string.pinout_close),
                tint = PartloreTheme.colors.textPrimary,
            )
        }
        PinDetails(
            state,
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = PartloreTheme.spacing.space16,
                    end = PartloreTheme.spacing.space16,
                    bottom = PartloreTheme.spacing.space16,
                ),
        )
    }
}
