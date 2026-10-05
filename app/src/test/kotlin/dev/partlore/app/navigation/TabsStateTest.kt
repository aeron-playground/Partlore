package dev.partlore.app.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TabsStateTest {
    private fun newState(): TabsState =
        TabsState(Tab.entries.associateWith { mutableListOf<NavKey>(it.root) }, mutableStateOf(Tab.Library))

    @Test
    fun startsOnLibrary() {
        val tabs = newState()
        assertEquals(Tab.Library, tabs.current)
        assertEquals(listOf(LibraryHomeKey), tabs.currentStack)
    }

    @Test
    fun eachTabKeepsItsOwnPages() {
        val tabs = newState()
        tabs.select(Tab.Bench)
        tabs.navigate(SettingsKey)
        tabs.select(Tab.Search)
        tabs.select(Tab.Bench)
        assertEquals(listOf(BenchHomeKey, SettingsKey), tabs.currentStack)
    }

    @Test
    fun reselectingTheCurrentTabReturnsToItsStartPage() {
        val tabs = newState()
        tabs.select(Tab.Bench)
        tabs.navigate(SettingsKey)
        tabs.select(Tab.Bench)
        assertEquals(listOf(BenchHomeKey), tabs.currentStack)
    }

    @Test
    fun backClosesTheTopPageFirst() {
        val tabs = newState()
        tabs.select(Tab.Bench)
        tabs.navigate(SettingsKey)
        assertTrue(tabs.back())
        assertEquals(Tab.Bench, tabs.current)
        assertEquals(listOf(BenchHomeKey), tabs.currentStack)
    }

    @Test
    fun backAtAnotherTabsStartPageGoesToLibrary() {
        val tabs = newState()
        tabs.select(Tab.Tools)
        assertTrue(tabs.back())
        assertEquals(Tab.Library, tabs.current)
    }

    @Test
    fun backAtLibrarysStartPageLetsTheAppClose() {
        assertFalse(newState().back())
    }

    @Test
    fun openingAPageThatIsAlreadyOpenGoesBackToIt() {
        val tabs = newState()
        tabs.navigate(PartKey("a/one"))
        tabs.navigate(PartKey("b/two"))
        tabs.navigate(PartKey("a/one"))
        assertEquals(listOf(LibraryHomeKey, PartKey("a/one")), tabs.currentStack)
    }
}
