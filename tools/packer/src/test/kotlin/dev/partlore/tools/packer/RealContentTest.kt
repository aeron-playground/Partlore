package dev.partlore.tools.packer

import dev.partlore.tools.content.Validator
import dev.partlore.tools.content.ship.Mode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** The real content/ folder: valid, and every part packs with every pin. */
class RealContentTest {
    @get:Rule val tmp = TemporaryFolder()

    private val content = File(System.getProperty("partlore.contentDir"))

    @Test
    fun theRealContentIsValid() {
        val problems = Validator(content).validate(Mode.RELEASE).diagnostics
        assertEquals("", problems.joinToString("\n") { it.plain("content/") })
    }

    @Test
    fun everyPartPacksWithEveryPinInPreview() {
        val result = Packer(content, tmp.root).pack(Mode.PREVIEW) as Packer.Result.Packed
        Validator(content).validate(Mode.PREVIEW).content.parts.forEach { part ->
            val positions = part.pins?.headers?.sumOf { it.rows * it.pinsPerRow } ?: 0
            val pins = single(result.file, "SELECT count(*) FROM pin WHERE part_id = ?", part.id)
            assertEquals(part.id, positions.toString(), pins)
        }
        val ids = query(result.file, "SELECT id FROM part ORDER BY id").map { it.single() }
        assertEquals(listOf("espressif/esp32-wroom-32e"), ids)
    }
}
