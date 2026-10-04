package dev.partlore.tools.packer

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_CREATE
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READWRITE
import androidx.sqlite.execSQL
import dev.partlore.core.packformat.PackFormat
import dev.partlore.tools.content.model.Content
import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.content.ship.Shipping
import java.io.File

/** Writes content into a fresh pack file, in a fixed order, so the same content gives the same bytes. */
class PackWriter(private val driver: BundledSQLiteDriver = BundledSQLiteDriver()) {
    /** Returns how many parts went in. */
    fun write(content: Content, mode: Mode, contentSha256: String, file: File): Int {
        file.delete()
        val parts = content.parts.filter { Shipping.partShips(it, mode) }
        val keys = content.specKeys.associateBy { it.key }
        driver.open(file.path, SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE).use { db ->
            db.execSQL("PRAGMA page_size = $PAGE_SIZE")
            db.execSQL("PRAGMA journal_mode = OFF")
            db.execSQL("BEGIN")
            PackFormat.ddl.forEach { db.execSQL(it) }
            Rows(db).use { rows ->
                rows.meta(content, mode, contentSha256, parts.size)
                rows.shared(content)
                parts.forEach { rows.part(it, mode, keys) }
            }
            db.execSQL("COMMIT")
            // One search segment and a compact file: smaller, faster, and the same bytes every time.
            db.execSQL("INSERT INTO search (search) VALUES ('optimize')")
            db.execSQL("VACUUM")
        }
        return parts.size
    }

    private companion object {
        const val PAGE_SIZE = 4096
    }
}
