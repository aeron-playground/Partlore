package dev.partlore.core.content

import dev.partlore.tools.content.ship.Mode
import dev.partlore.tools.content.testing.ContentFixture
import dev.partlore.tools.packer.Packer
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Runs only through ./gradlew :core:content:speedTest (CI). Target from the step-4 design, §6. */
class SpeedTest {
    @get:Rule val tmp = TemporaryFolder()

    @Before
    fun onlyWhenAsked() = assumeTrue(System.getProperty("partlore.speedTest") == "true")

    @Test
    fun everyScreenReadsInUnderTwentyMilliseconds() {
        val fixture = ContentFixture(tmp.newFolder("content"))
        // 50 categories of 100 boards, each with 40 pins: a realistic shape at 5,000 parts.
        fixture.write(
            "categories.yaml",
            "categories:\n" +
                (0 until CATEGORIES).joinToString("") { "  - { id: cat-%02d, name: Cat %02d }\n".format(it, it) },
        )
        repeat(PARTS) { n ->
            val id = "scale/board-%04d".format(n)
            fixture.addBoard(id, extraPins = EXTRA_PINS)
            fixture.edit("parts/$id/part.yaml") {
                it.replace("category: test-boards", "category: cat-%02d".format(n % CATEGORIES))
            }
        }
        val pack = Packer(fixture.root, tmp.newFolder("out"), TODAY).pack(Mode.RELEASE)
        check(pack is Packer.Result.Packed) { pack.diagnostics.take(5).joinToString("\n") { it.plain("") } }
        openPack(pack.file).use { reader ->
            val timings =
                mapOf(
                    "library" to median { reader.library() },
                    "category" to median { reader.category("cat-07") },
                    "part" to median { reader.part("scale/board-2500") },
                )
            println("speed: " + timings.entries.joinToString { "%s %.2f ms".format(it.key, it.value) })
            timings.forEach { (name, ms) -> assertTrue("$name took $ms ms", ms < LIMIT_MS) }
        }
    }

    private fun median(read: () -> Any?): Double {
        repeat(WARMUP) { read() }
        return List(RUNS) {
            val started = System.nanoTime()
            read()
            (System.nanoTime() - started) / 1e6
        }.sorted()[RUNS / 2]
    }

    private companion object {
        const val PARTS = 5_000
        const val CATEGORIES = 50
        const val EXTRA_PINS = 37
        const val WARMUP = 5
        const val RUNS = 21
        const val LIMIT_MS = 20.0
    }
}
