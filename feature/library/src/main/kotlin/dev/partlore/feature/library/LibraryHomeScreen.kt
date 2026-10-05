package dev.partlore.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.partlore.core.designsystem.components.PlCategoryTile
import dev.partlore.core.designsystem.components.PlEmptyState
import dev.partlore.core.designsystem.components.PlErrorState
import dev.partlore.core.designsystem.components.PlIcons
import dev.partlore.core.designsystem.components.PlPartCard
import dev.partlore.core.designsystem.components.PlSearchField
import dev.partlore.core.designsystem.components.PlTopBar
import dev.partlore.core.designsystem.theme.PartloreLayout
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.ContentProblem
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.LibraryHome
import dev.partlore.core.model.PartCard
import org.koin.compose.viewmodel.koinViewModel

private const val COMPACT_COLUMNS = 2
private const val EXPANDED_COLUMNS = 4

@Composable
fun LibraryHomeRoute(
    onOpenCategory: (String) -> Unit,
    onOpenPart: (String) -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
    appVersion: String? = null,
    viewModel: LibraryHomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LibraryHomeScreen(state, onOpenCategory, onOpenPart, onOpenSearch, modifier, appVersion)
}

@Composable
fun LibraryHomeScreen(
    state: ContentResult<LibraryHome>?,
    onOpenCategory: (String) -> Unit,
    onOpenPart: (String) -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
    appVersion: String? = null,
) {
    Column(modifier.fillMaxSize().background(PartloreTheme.colors.bg)) {
        PlTopBar(title = stringResource(R.string.library_title), large = true)
        when (state) {
            // Reading takes milliseconds: no spinner, the page draws when the data is there.
            null -> Unit

            is ContentResult.Ok ->
                if (state.value.partCount == 0) {
                    EmptyLibrary()
                } else {
                    LibraryBody(state.value, onOpenCategory, onOpenPart, onOpenSearch)
                }

            is ContentResult.Failed -> PlErrorState(state, appVersion = appVersion)

            ContentResult.NotFound -> PlErrorState(
                ContentResult.Failed(ContentProblem.Missing, "library"),
                appVersion = appVersion,
            )
        }
    }
}

@Composable
private fun LibraryBody(
    home: LibraryHome,
    onOpenCategory: (String) -> Unit,
    onOpenPart: (String) -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PartloreTheme.spacing
    val searchHint = stringResource(R.string.library_search_hint)
    BoxWithConstraints(modifier.fillMaxSize()) {
        val columns = if (maxWidth >= PartloreLayout.expandedWidth) EXPANDED_COLUMNS else COMPACT_COLUMNS
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(start = spacing.space16, end = spacing.space16, bottom = spacing.space24),
            horizontalArrangement = Arrangement.spacedBy(spacing.space12),
            verticalArrangement = Arrangement.spacedBy(spacing.space12),
        ) {
            fullWidth("search") { PlSearchField(searchHint, onClick = onOpenSearch) }
            if (home.isPreview) fullWidth("preview") { PreviewNote() }
            if (home.starterBoards.isNotEmpty()) {
                fullWidth("starter-title") { SectionTitle(stringResource(R.string.library_starter_boards)) }
                fullWidth("starter") { StarterBoards(home.starterBoards, onOpenPart) }
            }
            fullWidth("categories-title") { SectionTitle(stringResource(R.string.library_categories)) }
            items(home.categories, key = { "category:${it.id}" }) { tile ->
                PlCategoryTile(
                    name = tile.name,
                    countLabel = pluralStringResource(R.plurals.library_part_count, tile.partCount, tile.partCount),
                    artKind = artKindFor(tile.id),
                    onClick = { onOpenCategory(tile.id) },
                )
            }
        }
    }
}

private fun LazyGridScope.fullWidth(key: String, content: @Composable () -> Unit) =
    item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }

@Composable
private fun StarterBoards(boards: List<PartCard>, onOpenPart: (String) -> Unit, modifier: Modifier = Modifier) {
    val spacing = PartloreTheme.spacing
    // Vertical padding leaves room for the cards' shadows.
    LazyRow(
        modifier,
        contentPadding = PaddingValues(vertical = spacing.space4),
        horizontalArrangement = Arrangement.spacedBy(spacing.space12),
    ) {
        items(boards, key = { it.id }) { card ->
            PlPartCard(card.name, card.line(), card.kind, card.level, onClick = { onOpenPart(card.id) }, compact = true)
        }
    }
}

@Composable
private fun PreviewNote(modifier: Modifier = Modifier) {
    val colors = PartloreTheme.colors
    Text(
        text = stringResource(R.string.library_preview_note),
        style = PartloreTheme.typography.bodyM,
        color = colors.textPrimary,
        modifier =
        modifier
            .fillMaxWidth()
            .clip(PartloreTheme.shapes.sm)
            .background(colors.warningContainer)
            .padding(PartloreTheme.spacing.space12),
    )
}

@Composable
private fun EmptyLibrary(modifier: Modifier = Modifier) {
    val uri = LocalUriHandler.current
    PlEmptyState(
        message = stringResource(R.string.library_empty),
        modifier = modifier,
        icon = PlIcons.Library,
        actionLabel = stringResource(R.string.library_how_to_add),
        onAction = { uri.openUri(HOW_TO_ADD_URL) },
    )
}
