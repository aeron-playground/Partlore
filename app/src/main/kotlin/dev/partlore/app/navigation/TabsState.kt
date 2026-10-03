package dev.partlore.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

/**
 * One back stack per tab. Re-tapping the current tab returns to its start page. Back closes the top
 * page; at the start page of another tab it goes to Library; at Library's start page it lets the app close.
 */
class TabsState(
    private val stacks: Map<Tab, MutableList<NavKey>>,
    private val currentTab: MutableState<Tab> = mutableStateOf(Tab.Library),
) {
    val current: Tab get() = currentTab.value

    val currentStack: List<NavKey> get() = stacks.getValue(current)

    fun stack(tab: Tab): List<NavKey> = stacks.getValue(tab)

    fun select(tab: Tab) {
        if (tab == current) {
            val stack = stacks.getValue(tab)
            while (stack.size > 1) stack.removeAt(stack.lastIndex)
        } else {
            currentTab.value = tab
        }
    }

    fun navigate(key: NavKey) {
        stacks.getValue(current).add(key)
    }

    fun back(): Boolean {
        val stack = stacks.getValue(current)
        return when {
            stack.size > 1 -> {
                stack.removeAt(stack.lastIndex)
                true
            }

            current != Tab.Library -> {
                currentTab.value = Tab.Library
                true
            }

            else -> false
        }
    }
}

/** Survives rotation and process death: both the stacks and the current tab are saved. */
@Composable
fun rememberTabsState(): TabsState {
    val library = rememberNavBackStack(Tab.Library.root)
    val search = rememberNavBackStack(Tab.Search.root)
    val tools = rememberNavBackStack(Tab.Tools.root)
    val bench = rememberNavBackStack(Tab.Bench.root)
    val current = rememberSaveable { mutableStateOf(Tab.Library) }
    return remember(library, search, tools, bench, current) {
        TabsState(
            mapOf(Tab.Library to library, Tab.Search to search, Tab.Tools to tools, Tab.Bench to bench),
            current,
        )
    }
}
