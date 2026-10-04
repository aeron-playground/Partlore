package dev.partlore.tools.packer

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.content.testing.ContentFixture
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** Runs only through ./gradlew :tools:packer:scaleTest (CI). Targets come from the design, §9. */
class ScaleTest {
    @get:Rule val tmp = TemporaryFolder()

    @Before
    fun onlyWhenAsked() {
        assumeTrue("runs with ./gradlew :tools:packer:scaleTest", System.getProperty("partlore.scaleTest") == "true")
    }

    @Test
    fun fiveThousandPartsPackInAMinuteAndSearchInUnderTwentyMilliseconds() {
        val fixture = ContentFixture(tmp.newFolder("content"))
        repeat(PARTS) { fixture.addBoard("scale/board-%04d".format(it), extraPins = EXTRA_PINS) }
        val started = System.nanoTime()
        val result = Packer(fixture.root, tmp.newFolder("out"), TODAY).pack(Mode.RELEASE)
        val seconds = (System.nanoTime() - started) / 1e9
        check(result is Packer.Result.Packed) { result.diagnostics.take(5).joinToString("\n") { it.plain("") } }
        val millis = medianSearchMillis(result.file)
        val report = "scale: $PARTS parts in %.1f s, search median %.2f ms, %d bytes per part"
        println(report.format(seconds, millis, result.entry.size / PARTS))
        assertTrue("packing took %.1f s".format(seconds), seconds < 60)
        assertTrue("search took %.2f ms".format(millis), millis < 20)
    }

    private fun medianSearchMillis(pack: File): Double =
        BundledSQLiteDriver().open(pack.path, SQLITE_OPEN_READONLY).use { db ->
            repeat(5) { search(db) }
            List(21) {
                val started = System.nanoTime()
                search(db)
                (System.nanoTime() - started) / 1e6
            }.sorted()[10]
        }

    private fun search(db: SQLiteConnection): Int =
        db.prepare("SELECT DISTINCT part_id FROM search WHERE search MATCH ? LIMIT 50").use { st ->
            st.bindText(1, "\"oard-12\"")
            var rows = 0
            while (st.step()) rows++
            rows
        }

    private companion object {
        const val PARTS = 5_000
        const val EXTRA_PINS = 37 // 40 pins per board, about a real dev board
    }
}
