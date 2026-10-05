package dev.partlore.feature.part

import org.junit.Assert.assertEquals
import org.junit.Test

class GithubLinksTest {
    @Test
    fun reportAProblemEncodesTheSlashInThePartId() {
        assertEquals(
            "https://github.com/aeron-playground/Partlore/issues/new?template=content_error.yml" +
                "&part_id=espressif%2Fesp32-wroom-32e&pack_version=0.1.0",
            GithubLinks.reportProblem("espressif/esp32-wroom-32e", "0.1.0"),
        )
    }

    @Test
    fun aPlusInTheVersionIsNotTurnedIntoASpace() {
        assertEquals(true, GithubLinks.reportProblem("a/b", "1.0+local").endsWith("pack_version=1.0%2Blocal"))
    }

    @Test
    fun editOnGithubKeepsThePartIdAsAPath() {
        assertEquals(
            "https://github.com/aeron-playground/Partlore/tree/main/content/parts/espressif/esp32-wroom-32e",
            GithubLinks.editOnGithub("espressif/esp32-wroom-32e"),
        )
    }
}
