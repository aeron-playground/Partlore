package dev.partlore.core.content

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READWRITE
import androidx.sqlite.execSQL
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
import java.io.File
import java.io.IOException

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
    fun aPinoutComesThroughTheRepository() = runTest {
        val pack = buildPack(tmp) { addBoard() }
        val repo =
            PackContentRepository({
                InstallResult.Ready(pack, replaced = false)
            }, StandardTestDispatcher(testScheduler))
        val result = repo.pinout(BOARD)
        assertTrue(result is ContentResult.Ok)
        assertEquals(3, (result as ContentResult.Ok).value.pins.size)
        assertEquals(ContentResult.NotFound, repo.pinout("testmaker/nothing"))
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

    // Damage that shows only after the pack opened (here a missing table) also gets one re-copy.
    @Test
    fun aPackDamagedAfterOpeningIsRecopiedOnce() = runTest {
        val good = buildPack(tmp) { addBoard() }
        val damaged = withoutTagTable(good)
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
    fun aPackThatStaysDamagedIsRecopiedOnlyOncePerRun() = runTest {
        val damaged = withoutTagTable(buildPack(tmp) { addBoard() })
        val calls = mutableListOf<Boolean>()
        val repo =
            PackContentRepository(
                { force ->
                    calls += force
                    InstallResult.Ready(damaged, replaced = force)
                },
                StandardTestDispatcher(testScheduler),
            )
        assertTrue(repo.library() is ContentResult.Failed)
        assertTrue(repo.library() is ContentResult.Failed)
        assertEquals(listOf(false, true, false), calls)
    }

    private fun withoutTagTable(pack: File): File {
        val copy = File(tmp.newFolder(), "damaged.db")
        pack.copyTo(copy)
        BundledSQLiteDriver().open(copy.path, SQLITE_OPEN_READWRITE).use { it.execSQL("DROP TABLE part_tag") }
        return copy
    }

    // A full disk or a storage error while installing shows the error screen instead of crashing the app.
    @Test
    fun anInstallerThatThrowsGivesAFailedResult() = runTest {
        val errors = listOf(IOException("No space left on device"), SecurityException("Storage not allowed"))
        errors.forEach { error ->
            val repo = PackContentRepository({ throw error }, StandardTestDispatcher(testScheduler))
            val result = repo.library()
            assertTrue("$error gave $result", result is ContentResult.Failed)
            assertEquals(ContentProblem.Damaged, (result as ContentResult.Failed).problem)
            assertTrue(result.detail, error.message.orEmpty() in result.detail)
        }
    }
}
