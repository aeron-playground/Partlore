package dev.partlore.core.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentLinksTest {
    @Test
    fun reportAProblemEncodesTheSlashInThePartId() {
        assertEquals(
            "https://github.com/aeron-playground/Partlore/issues/new?template=content_error.yml" +
                "&part_id=espressif%2Fesp32-wroom-32e&pack_version=0.1.0",
            ContentLinks.reportProblem("espressif/esp32-wroom-32e", "0.1.0"),
        )
    }

    @Test
    fun aPlusInTheVersionIsNotTurnedIntoASpace() {
        assertTrue(ContentLinks.reportProblem("a/b", "1.0+local").endsWith("pack_version=1.0%2Blocal"))
    }

    @Test
    fun aPinReportNamesItsField() {
        assertTrue(
            ContentLinks.reportProblem("a/b", "1.0", fieldPath = "pins.gpio2").endsWith("&field_path=pins.gpio2"),
        )
    }

    @Test
    fun editOnGithubKeepsThePartIdAsAPath() {
        assertEquals(
            "https://github.com/aeron-playground/Partlore/tree/main/content/parts/espressif/esp32-wroom-32e",
            ContentLinks.editOnGithub("espressif/esp32-wroom-32e"),
        )
    }
}
