package dev.partlore.tools.packer

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.content.testing.ContentFixture
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.LocalDate

class HeaderEdgeRowsTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test
    fun eachRunBecomesARowWithItsCitation() {
        val fixture = ContentFixture(tmp.newFolder("content")).apply { addBoard() }
        val result = Packer(fixture.root, tmp.newFolder("out"), LocalDate.of(2026, 10, 9)).pack(Mode.RELEASE)
        check(result is Packer.Result.Packed)
        val rows =
            BundledSQLiteDriver().open(result.file.path, SQLITE_OPEN_READONLY).use { db ->
                db.prepare("SELECT header_id, seq, edge, run, pins, first, row1, ord, source_id, page FROM header_edge")
                    .use { st ->
                        buildList {
                            while (st.step()) {
                                add(
                                    (0 until st.getColumnCount()).map {
                                        if (st.isNull(it)) null else st.getText(it)
                                    },
                                )
                            }
                        }
                    }
            }
        assertEquals(listOf(listOf("j1", "0", "left", null, "3", "top", null, null, "s1", "5")), rows)
    }
}
