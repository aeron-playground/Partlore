package dev.partlore.core.content

import dev.partlore.core.model.CategoryPage
import dev.partlore.core.model.ContentProblem
import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.LibraryHome
import dev.partlore.core.model.PartPage
import dev.partlore.core.model.Pinout
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Installs the pack on first use, opens it once and answers every read on one background thread
 * (a SQLite connection must not be shared between threads). A damaged pack is re-copied once.
 */
class PackContentRepository(
    private val install: (force: Boolean) -> InstallResult,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1),
) : ContentRepository {
    // Only touched on [dispatcher].
    private var reader: PackReader? = null
    private var recopied = false

    override suspend fun library(): ContentResult<LibraryHome> = read { it.library() }

    override suspend fun category(id: String): ContentResult<CategoryPage> = read { it.category(id) }

    override suspend fun part(id: String): ContentResult<PartPage> = read { it.part(id) }

    override suspend fun pinout(partId: String): ContentResult<Pinout> = read { it.pinout(partId) }

    private suspend fun <T : Any> read(query: (PackReader) -> T?): ContentResult<T> = withContext(dispatcher) {
        val first = readOnce(query, force = false)
        // A damaged pack, found when it opens or later in a page, is copied again from the APK once per run.
        if (first.isDamaged() && !recopied) {
            recopied = true
            readOnce(query, force = true)
        } else {
            first
        }
    }

    private fun <T : Any> readOnce(query: (PackReader) -> T?, force: Boolean): ContentResult<T> =
        when (val opened = openReader(force)) {
            is ContentResult.Ok -> ask(opened.value, query)
            is ContentResult.Failed -> opened
            ContentResult.NotFound -> ContentResult.NotFound
        }

    private fun <T : Any> ask(open: PackReader, query: (PackReader) -> T?): ContentResult<T> =
        catchingSqlite<ContentResult<T>>({ query(open)?.let { ContentResult.Ok(it) } ?: ContentResult.NotFound }) { e ->
            reader = null
            open.close()
            ContentResult.Failed(ContentProblem.Damaged, e.message.orEmpty())
        }

    /**
     * The open reader, or a newly installed and opened pack. After a failure [reader] is null, so [force]
     * reaches the installer.
     */
    private fun openReader(force: Boolean): ContentResult<PackReader> {
        reader?.let { return ContentResult.Ok(it) }
        val result = open(force)
        if (result is ContentResult.Ok) reader = result.value
        return result
    }

    private fun open(force: Boolean): ContentResult<PackReader> = when (val installed = installSafely(force)) {
        is InstallResult.Ready -> PackReader.open(installed.file)
        is InstallResult.KeptPrevious -> PackReader.open(installed.file)
        is InstallResult.Failed -> ContentResult.Failed(installed.problem, installed.detail)
    }

    // Copying can fail on a full disk or a storage error; that shows the error screen, never a crash.
    private fun installSafely(force: Boolean): InstallResult = try {
        install(force)
    } catch (e: IOException) {
        InstallResult.Failed(ContentProblem.Damaged, "The content pack can't be installed: ${e.message}")
    } catch (e: SecurityException) {
        InstallResult.Failed(ContentProblem.Damaged, "The content pack can't be installed: ${e.message}")
    }
}

private fun ContentResult<*>.isDamaged() = this is ContentResult.Failed && problem == ContentProblem.Damaged
