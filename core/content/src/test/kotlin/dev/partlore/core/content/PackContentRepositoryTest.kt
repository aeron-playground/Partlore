package dev.partlore.core.content

import dev.partlore.core.model.ContentProblem
import dev.partlore.core.model.ContentResult
import dev.partlore.tools.content.testing.ContentFixture.Companion.BOARD
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PackContentRepositoryTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test
    fun readsTheInstalledPack() = runTest {
        val pack = buildPack(tmp) { addBoard() }
        val repo =
            PackContentRepository({
                InstallResult.Ready(pack, replaced = false)
            }, StandardTestDispatcher(testScheduler))
        val library = repo.library()
        assertTrue(library is ContentResult.Ok && library.value.partCount == 1)
        assertTrue(repo.part(BOARD) is ContentResult.Ok)
    }

    @Test
    fun anInstallFailureBecomesAFailedResult() = runTest {
        val repo =
            PackContentRepository(
                { InstallResult.Failed(ContentProblem.Missing, "no asset") },
                StandardTestDispatcher(testScheduler),
            )
        assertEquals(ContentResult.Failed(ContentProblem.Missing, "no asset"), repo.library())
    }

    @Test
    fun aDamagedPackIsReinstalledOnce() = runTest {
        val good = buildPack(tmp) { addBoard() }
        val damaged = tmp.newFile("damaged.db").apply { writeText("not a database") }
        val calls = mutableListOf<Boolean>()
        val repo =
            PackContentRepository(
                { force ->
                    calls += force
                    InstallResult.Ready(if (force) good else damaged, replaced = force)
                },
                StandardTestDispatcher(testScheduler),
            )
        assertTrue(repo.library() is ContentResult.Ok)
        assertEquals(listOf(false, true), calls)
    }

    @Test
    fun anUnknownPartIsNotFound() = runTest {
        val pack = buildPack(tmp) { addBoard() }
        val repo =
            PackContentRepository({
                InstallResult.Ready(pack, replaced = false)
            }, StandardTestDispatcher(testScheduler))
        assertEquals(ContentResult.NotFound, repo.part("testmaker/nothing"))
        assertEquals(ContentResult.NotFound, repo.category("nothing"))
    }

    @Test
    fun thePackIsInstalledAndOpenedOnce() = runTest {
        val pack = buildPack(tmp) { addBoard() }
        var installs = 0
        val repo =
            PackContentRepository(
                {
                    installs++
                    InstallResult.Ready(pack, replaced = false)
                },
                StandardTestDispatcher(testScheduler),
            )
        repo.library()
        repo.category("test-boards")
        repo.part(BOARD)
        assertEquals(1, installs)
    }
}
