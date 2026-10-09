package dev.partlore.core.packformat

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_CREATE
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READWRITE
import androidx.sqlite.execSQL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PackFormatTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun emptyPack(): SQLiteConnection =
        BundledSQLiteDriver().open(File(tmp.root, "pack.db").path, SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE)
            .also { db -> PackFormat.ddl.forEach { db.execSQL(it) } }

    @Test
    fun packFileNameCarriesTheContentVersion() {
        assertEquals("partlore-content-0.1.0.db", PackFormat.packFileName("0.1.0"))
    }

    @Test
    fun everyTableIsCreatedOnTheBundledSqlite() {
        emptyPack().use { db ->
            val tables = mutableListOf<String>()
            db.prepare(
                "SELECT name FROM sqlite_schema WHERE type = 'table' " +
                    "AND name NOT GLOB 'search_*' AND name NOT GLOB 'sqlite_*' ORDER BY name",
            ).use { st -> while (st.step()) tables += st.getText(0) }
            assertEquals(EXPECTED_TABLES, tables)
        }
    }

    @Test
    fun trigramSearchFindsAPieceOfAPartNumber() {
        emptyPack().use { db ->
            db.execSQL("INSERT INTO search VALUES ('arduino/uno-r3', 'part', '', 'ATmega328P')")
            db.prepare("SELECT part_id FROM search WHERE search MATCH ?").use { st ->
                st.bindText(1, "\"328\"")
                assertTrue(st.step())
                assertEquals("arduino/uno-r3", st.getText(0))
            }
        }
    }

    private companion object {
        val EXPECTED_TABLES =
            listOf(
                "article", "category", "glance", "gotcha", "gotcha_cite", "gotcha_pin", "header",
                "header_edge", "i2c_address",
                "meta", "part", "part_alias", "part_related", "part_tag", "pin", "pin_alias", "pin_cite",
                "pin_function", "search", "source", "spec", "status", "tag",
            )
    }
}
