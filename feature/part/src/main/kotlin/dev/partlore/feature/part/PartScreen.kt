package dev.partlore.feature.part

import android.content.ClipData
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.partlore.core.designsystem.components.PlEmptyState
import dev.partlore.core.designsystem.components.PlErrorState
import dev.partlore.core.designsystem.components.PlSectionChips
import dev.partlore.core.designsystem.components.PlTopBar
import dev.partlore.core.designsystem.theme.PartloreSprings
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.Cite
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.PartPage
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The sections below the top area, in reading order. */
internal enum class PartSection(@StringRes val chip: Int, @StringRes val title: Int) {
    Specs(R.string.part_section_specs, R.string.part_key_specs),
    I2c(R.string.part_section_i2c, R.string.part_i2c_title),
    Gotchas(R.string.part_section_gotchas, R.string.part_section_gotchas),
    Related(R.string.part_section_related, R.string.part_section_related),
    Sources(R.string.part_section_sources, R.string.part_sources_title),
}

/** The sections this part has something for. Sources is always there. */
internal fun PartPage.sections(): List<PartSection> = PartSection.entries.filter { section ->
    when (section) {
        PartSection.Specs -> specs.isNotEmpty() || absoluteMax.isNotEmpty()
        PartSection.I2c -> i2c.isNotEmpty()
        PartSection.Gotchas -> gotchas.isNotEmpty()
        PartSection.Related -> uses != null || related.isNotEmpty() || usedBy.isNotEmpty()
        PartSection.Sources -> true
    }
}

/** What the blocks of the page may ask for. */
internal class PartActions(
    val openPart: (String) -> Unit,
    val showSource: (Cite) -> Unit,
    val showStatus: () -> Unit,
    val copy: (String) -> Unit,
    val goTo: (PartSection) -> Unit,
)

@Composable
fun PartRoute(
    id: String,
    onBack: () -> Unit,
    onOpenPart: (String) -> Unit,
    modifier: Modifier = Modifier,
    appVersion: String? = null,
    viewModel: PartViewModel = koinViewModel(parameters = { parametersOf(id) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PartScreen(state, onBack, onOpenPart, modifier, appVersion)
}

@Composable
fun PartScreen(
    state: ContentResult<PartPage>?,
    onBack: () -> Unit,
    onOpenPart: (String) -> Unit,
    modifier: Modifier = Modifier,
    appVersion: String? = null,
) {
    Column(modifier.fillMaxSize().background(PartloreTheme.colors.bg)) {
        if (state is ContentResult.Ok) {
            PartContent(state.value, onBack, onOpenPart)
        } else {
            PlTopBar(title = "", onBack = onBack)
            when (state) {
                ContentResult.NotFound -> PlEmptyState(stringResource(R.string.part_not_found))
                is ContentResult.Failed -> PlErrorState(state, appVersion = appVersion)
                else -> Unit
            }
        }
    }
}

@Composable
private fun PartContent(
    page: PartPage,
    onBack: () -> Unit,
    onOpenPart: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PartloreTheme.spacing
    val list = rememberLazyListState()
    val sections = remember(page) { page.sections() }
    val scrolled by remember { derivedStateOf { list.firstVisibleItemIndex > 0 } }
    val current by remember(sections) { derivedStateOf { currentSection(list, sections.size) } }
    var sheet by remember { mutableStateOf<PartSheet?>(null) }
    val actions = rememberPartActions(list, sections, onOpenPart, onSheet = { sheet = it })
    Column(modifier) {
        PlTopBar(title = if (scrolled) page.name else "", onBack = onBack)
        SectionChipsBar(scrolled, sections, current, onSelect = actions.goTo)
        LazyColumn(
            state = list,
            contentPadding = PaddingValues(start = spacing.space16, end = spacing.space16, bottom = spacing.space32),
            verticalArrangement = Arrangement.spacedBy(spacing.space24),
        ) {
            item(key = "top") { PartTop(page, actions) }
            items(sections, key = { it.name }) { section -> PartSectionBlock(section, page, actions) }
        }
        PartSheetHost(sheet, page, onDismiss = { sheet = null })
    }
}

/** The section being read: the first visible one, or the last once the page can't scroll further. */
private fun currentSection(list: LazyListState, count: Int): Int =
    if (!list.canScrollForward && list.firstVisibleItemIndex > 0) {
        count - 1
    } else {
        (list.firstVisibleItemIndex - 1).coerceIn(0, count - 1)
    }

@Composable
private fun rememberPartActions(
    list: LazyListState,
    sections: List<PartSection>,
    onOpenPart: (String) -> Unit,
    onSheet: (PartSheet) -> Unit,
): PartActions {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val reduced = PartloreTheme.motion.reduced
    return remember(list, sections, onOpenPart, reduced) {
        PartActions(
            openPart = onOpenPart,
            showSource = { onSheet(PartSheet.Source(it)) },
            showStatus = { onSheet(PartSheet.Status) },
            copy = { text -> scope.launch { clipboard.setClipEntry(ClipData.newPlainText(text, text).toClipEntry()) } },
            goTo = { section ->
                // Item 0 is the top area; the sections follow in order.
                val index = sections.indexOf(section) + 1
                scope.launch { if (reduced) list.scrollToItem(index) else list.animateScrollToItem(index) }
            },
        )
    }
}

@Composable
private fun SectionChipsBar(
    visible: Boolean,
    sections: List<PartSection>,
    current: Int,
    onSelect: (PartSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = PartloreTheme.motion
    AnimatedVisibility(
        visible = visible && sections.size > 1,
        modifier = modifier,
        enter = fadeIn(motion.fade()) + expandVertically(motion.spring(PartloreSprings.glide)),
        exit = fadeOut(motion.fadeFast()) + shrinkVertically(motion.spring(PartloreSprings.glide)),
    ) {
        PlSectionChips(sections.map { stringResource(it.chip) }, current, onSelect = { onSelect(sections[it]) })
    }
}
