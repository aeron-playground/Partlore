package dev.partlore.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.partlore.core.designsystem.components.PlChip
import dev.partlore.core.designsystem.components.PlEmptyState
import dev.partlore.core.designsystem.components.PlErrorState
import dev.partlore.core.designsystem.components.PlPartCard
import dev.partlore.core.designsystem.components.PlTopBar
import dev.partlore.core.designsystem.components.rememberLinkOpener
import dev.partlore.core.designsystem.theme.PartloreSprings
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.ContentResult
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CategoryRoute(
    id: String,
    onBack: () -> Unit,
    onOpenPart: (String) -> Unit,
    modifier: Modifier = Modifier,
    appVersion: String? = null,
    viewModel: CategoryViewModel = koinViewModel(parameters = { parametersOf(id) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CategoryScreen(state, onBack, onOpenPart, viewModel::onAction, modifier, appVersion)
}

@Composable
fun CategoryScreen(
    state: ContentResult<CategoryState>?,
    onBack: () -> Unit,
    onOpenPart: (String) -> Unit,
    onAction: (CategoryAction) -> Unit,
    modifier: Modifier = Modifier,
    appVersion: String? = null,
) {
    val title = (state as? ContentResult.Ok)?.value?.page?.name.orEmpty()
    Column(modifier.fillMaxSize().background(PartloreTheme.colors.bg)) {
        PlTopBar(title = title, onBack = onBack)
        when (state) {
            null -> Unit
            is ContentResult.Ok -> CategoryBody(state.value, onOpenPart, onAction)
            ContentResult.NotFound -> PlEmptyState(stringResource(R.string.category_not_found))
            is ContentResult.Failed -> PlErrorState(state, appVersion = appVersion)
        }
    }
}

@Composable
private fun CategoryBody(
    state: CategoryState,
    onOpenPart: (String) -> Unit,
    onAction: (CategoryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PartloreTheme.spacing
    val motion = PartloreTheme.motion
    val parts = state.visibleParts
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = spacing.space16, end = spacing.space16, bottom = spacing.space24),
        verticalArrangement = Arrangement.spacedBy(spacing.space12),
    ) {
        if (state.page.children.isNotEmpty()) item(key = "children") { ChildChips(state, onAction) }
        if (state.page.tags.isNotEmpty()) item(key = "tags") { TagChips(state, onAction) }
        items(parts, key = { it.id }) { card ->
            PlPartCard(
                name = card.name,
                line = card.line(),
                kind = card.kind,
                level = card.level,
                onClick = { onOpenPart(card.id) },
                // Filtering moves cards into place; with reduced motion they only fade.
                modifier =
                Modifier.animateItem(
                    fadeInSpec = motion.fade(),
                    placementSpec = if (motion.reduced) null else motion.spring(PartloreSprings.glide),
                    fadeOutSpec = motion.fadeFast(),
                ),
            )
        }
        if (parts.isEmpty()) item(key = "empty") { NoParts(filtered = state.filter != CategoryFilter(), onAction) }
    }
}

@Composable
private fun ChildChips(state: CategoryState, onAction: (CategoryAction) -> Unit, modifier: Modifier = Modifier) {
    ChipRow(modifier) {
        PlChip(stringResource(R.string.category_all), state.filter.child == null, {
            onAction(CategoryAction.SelectChild(null))
        })
        state.page.children.forEach { child ->
            PlChip(child.name, state.filter.child == child.id, { onAction(CategoryAction.SelectChild(child.id)) })
        }
    }
}

@Composable
private fun TagChips(state: CategoryState, onAction: (CategoryAction) -> Unit, modifier: Modifier = Modifier) {
    ChipRow(modifier) {
        state.page.tags.forEach { tag ->
            PlChip(tag.label, tag.id in state.filter.tags, { onAction(CategoryAction.ToggleTag(tag.id)) })
        }
    }
}

@Composable
private fun ChipRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(
        modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space8),
    ) { content() }
}

@Composable
private fun NoParts(filtered: Boolean, onAction: (CategoryAction) -> Unit, modifier: Modifier = Modifier) {
    val openLink = rememberLinkOpener()
    if (filtered) {
        PlEmptyState(
            message = stringResource(R.string.category_no_match),
            modifier = modifier,
            actionLabel = stringResource(R.string.category_clear_filters),
            onAction = { onAction(CategoryAction.ClearFilters) },
        )
    } else {
        PlEmptyState(
            message = stringResource(R.string.category_empty),
            modifier = modifier,
            actionLabel = stringResource(R.string.category_contribute),
            onAction = { openLink(HOW_TO_ADD_URL) },
        )
    }
}
