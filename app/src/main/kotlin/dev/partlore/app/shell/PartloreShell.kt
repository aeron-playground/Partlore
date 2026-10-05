package dev.partlore.app.shell

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.partlore.app.navigation.BenchHomeKey
import dev.partlore.app.navigation.CategoryKey
import dev.partlore.app.navigation.LibraryHomeKey
import dev.partlore.app.navigation.PartKey
import dev.partlore.app.navigation.SearchHomeKey
import dev.partlore.app.navigation.SettingsKey
import dev.partlore.app.navigation.Tab
import dev.partlore.app.navigation.TabsState
import dev.partlore.app.navigation.ToolsHomeKey
import dev.partlore.app.navigation.rememberTabsState
import dev.partlore.core.designsystem.components.PlNavItem
import dev.partlore.core.designsystem.components.PlScaffold
import dev.partlore.core.designsystem.theme.PartloreMotion
import dev.partlore.core.designsystem.theme.PartloreSprings
import dev.partlore.core.designsystem.theme.PartloreTheme

// Page transition B: the new page slides in over the old one; the old page moves a quarter left and dims a little.
private const val UNDER_FRACTION = 4
private const val UNDER_ALPHA = 0.85f
private const val BELOW = -1f

@Composable
fun PartloreShell(screens: ShellScreens, modifier: Modifier = Modifier, tabs: TabsState = rememberTabsState()) {
    val nav = rememberShellNav(tabs)
    val provider =
        entryProvider<NavKey> {
            entry<LibraryHomeKey> { screens.library(nav) }
            entry<CategoryKey> { key -> screens.category(key.id, nav) }
            entry<PartKey> { key -> screens.part(key.id, nav) }
            entry<SearchHomeKey> { SearchHomeScreen() }
            entry<ToolsHomeKey> { ToolsHomeScreen() }
            entry<BenchHomeKey> { BenchHomeScreen(onOpenSettings = { tabs.navigate(SettingsKey) }) }
            entry<SettingsKey> { screens.settings(nav) }
        }
    // Each tab's pages get their own decorators, so a hidden tab keeps its saved state and ViewModels.
    val entriesByTab: Map<Tab, List<NavEntry<NavKey>>> =
        Tab.entries.associateWith { tab ->
            key(tab) {
                val saveable = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
                val viewModels = rememberViewModelStoreNavEntryDecorator<NavKey>()
                val decorators = remember(saveable, viewModels) { listOf(saveable, viewModels) }
                rememberDecoratedNavEntries(tabs.stack(tab), decorators, provider)
            }
        }
    val motion = PartloreTheme.motion
    PlScaffold(
        items = Tab.entries.map { tab ->
            PlNavItem(stringResource(tab.label), tab.icon, selected = tab == tabs.current, onClick = {
                tabs.select(tab)
            })
        },
        modifier = modifier,
    ) {
        // NavDisplay handles Back while a tab has pages above its start page; this handles the start page.
        BackHandler(enabled = tabs.current != Tab.Library && tabs.currentStack.size == 1) { tabs.back() }
        // Tabs fade into each other. Pages inside a tab use transition B, or only fade with reduced motion.
        Crossfade(targetState = tabs.current, animationSpec = motion.fade(), label = "tab") { tab ->
            NavDisplay(
                entries = entriesByTab.getValue(tab),
                onBack = { tabs.back() },
                transitionSpec = { pageTransition(motion, forward = true) },
                popTransitionSpec = { pageTransition(motion, forward = false) },
                predictivePopTransitionSpec = { _ -> pageTransition(motion, forward = false) },
            )
        }
    }
}

@Composable
private fun rememberShellNav(tabs: TabsState): ShellNav = remember(tabs) {
    ShellNav(
        back = { tabs.back() },
        openCategory = { tabs.navigate(CategoryKey(it)) },
        openPart = { tabs.navigate(PartKey(it)) },
        openSearch = { tabs.select(Tab.Search) },
    )
}

internal fun pageTransition(motion: PartloreMotion, forward: Boolean): ContentTransform {
    if (motion.reduced) return fadeIn(motion.fade()) togetherWith fadeOut(motion.fade())
    val slide = motion.spring<IntOffset>(PartloreSprings.glide)
    val dim = motion.spring<Float>(PartloreSprings.glide)
    val fromEdge = { width: Int -> width }
    val under = { width: Int -> -width / UNDER_FRACTION }
    return if (forward) {
        val leave = slideOutHorizontally(slide, under) + fadeOut(dim, UNDER_ALPHA)
        slideInHorizontally(slide, fromEdge) togetherWith leave
    } else {
        // Back: the page underneath returns below the page that leaves.
        val enter = slideInHorizontally(slide, under) + fadeIn(dim, UNDER_ALPHA)
        (enter togetherWith slideOutHorizontally(slide, fromEdge)).apply { targetContentZIndex = BELOW }
    }
}
