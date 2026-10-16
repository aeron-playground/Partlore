package dev.partlore.tools.content.testing

import java.io.File

/**
 * A throwaway content folder: the real schema files, small test categories and tags, and complete,
 * valid, checked boards on request. Values here are made up for tests and never ship.
 */
class ContentFixture(val root: File) {
    init {
        val schemaDir = System.getProperty(SCHEMA_DIR_PROPERTY) ?: error("set the $SCHEMA_DIR_PROPERTY system property")
        File(schemaDir).copyRecursively(File(root, "schema"), overwrite = true)
        write("version.txt", "0.0.1\n")
        write("categories.yaml", CATEGORIES)
        write("tags.yaml", TAGS)
        File(root, "parts").mkdirs()
    }

    fun write(path: String, text: String) {
        File(root, path).apply { parentFile.mkdirs() }.writeText(text)
    }

    fun read(path: String): String = File(root, path).readText()

    fun edit(path: String, change: (String) -> String) = write(path, change(read(path)))

    fun delete(path: String) {
        File(root, path).delete()
    }

    /** Line number (from 1) of the first line of [path] that contains [needle]. */
    fun lineOf(path: String, needle: String): Int = read(path).lines().indexOfFirst { needle in it } + 1

    /** A valid, checked board: 3 standard pins (gpio1, gpio0, gnd) plus [extraPins] plain I/O pins. */
    fun addBoard(id: String = BOARD, extraPins: Int = 0) {
        val dir = "parts/$id"
        write("$dir/part.yaml", PART.replace("__ID__", id).replace("__MAKER__", id.substringBefore('/')))
        write("$dir/sources.yaml", SOURCES)
        write("$dir/pins.yaml", pins(extraPins))
        write("$dir/gotchas.yaml", GOTCHAS)
        write("$dir/article.md", "A board that exists only in tests.\n")
    }

    private fun pins(extra: Int): String = PINS.replace("__COUNT__", (STANDARD_PINS + extra).toString()) +
        (1..extra).joinToString("") { extraPin(STANDARD_PINS + it) }

    private fun extraPin(index: Int): String =
        """
        |  - id: io$index
        |    header: j1
        |    row: 1
        |    index: $index
        |    board_label: "IO$index"
        |    direction: io
        |    voltage: 3.3
        |    five_v_tolerant: false
        |    functions:
        |      - { type: gpio }
        |    safe: ok
        |    cites:
        |      - { source: s1, page: 5 }
        |
        """.trimMargin()

    companion object {
        const val SCHEMA_DIR_PROPERTY = "partlore.schemaDir"
        const val BOARD = "testmaker/test-board"
        const val BOARD_DIR = "parts/$BOARD"
        private const val STANDARD_PINS = 3

        private val CATEGORIES =
            """
            categories:
              - id: test-boards
                name: Test boards
            """.trimIndent() + "\n"

        private val TAGS =
            """
            tags:
              - id: test-tag
                label: Test tag
            """.trimIndent() + "\n"

        private val PART =
            """
            id: __ID__
            kind: board
            name: Test Board
            aliases: [TB-1]
            manufacturer: __MAKER__
            category: test-boards
            tags: [test-tag]
            summary: A board that exists only in tests.
            specs:
              - { key: supply.voltage, unit: V, min: 3.0, typ: 3.3, max: 3.6, source: s1, page: 2 }
              - { key: io.logic_level, unit: V, value: 3.3, source: s1, page: 2 }
              - { key: radio.wifi, value: "802.11 b/g/n", source: s2, section: Radio }
            absolute_max:
              - { key: supply.voltage, unit: V, max: 3.9, source: s1, page: 3 }
            i2c:
              - { address: "0x3c", default: true, select: "ADDR to GND", source: s1, page: 4 }
            status:
              level: checked
              checked_by: [tester]
              checked_on: 2026-01-01
              against: [s1, s2]
            """.trimIndent() + "\n"

        private val SOURCES =
            """
            sources:
              - id: s1
                type: datasheet
                title: Test Board Datasheet
                publisher: TestMaker
                url: https://example.com/test-board.pdf
                version: "1.0"
                retrieved: 2026-01-01
                sha256: "0000000000000000000000000000000000000000000000000000000000000000"
                license: link-only
              - id: s2
                type: web-doc
                title: Test Board Guide
                publisher: TestMaker
                url: https://example.com/guide
                retrieved: 2026-01-01
                license: CC-BY-SA-4.0
            """.trimIndent() + "\n"

        private val PINS =
            """
            status:
              level: checked
              checked_by: [tester]
              checked_on: 2026-01-01
              against: [s1]
            headers:
              - id: j1
                label: J1
                type: header
                rows: 1
                pins_per_row: __COUNT__
                pitch_mm: 2.54
                source: s1
                page: 5
                edges:
                  - { edge: left, pins: __COUNT__, first: top, source: s1, page: 5 }
            pins:
              - id: gpio1
                header: j1
                row: 1
                index: 1
                chip_name: GPIO1
                board_label: "D1"
                label_aliases: [SDA]
                arduino: 1
                direction: io
                voltage: 3.3
                five_v_tolerant: false
                functions:
                  - { type: gpio }
                  - { type: i2c, signal: SDA, default: true }
                  - { type: adc, signal: ADC1_CH0 }
                safe: ok
                cites:
                  - { source: s1, page: 5 }
              - id: gpio0
                header: j1
                row: 1
                index: 2
                chip_name: GPIO0
                board_label: "D0"
                direction: io
                voltage: 3.3
                five_v_tolerant: false
                functions:
                  - { type: gpio }
                strapping: { role: boot-mode, must_be: high, at: reset }
                safe: caution
                cites:
                  - { source: s1, page: 5 }
              - id: gnd
                header: j1
                row: 1
                index: 3
                board_label: GND
                direction: ground
                safe: ok
                cites:
                  - { source: s1, page: 5 }
            """.trimIndent() + "\n"

        private val GOTCHAS =
            """
            status:
              level: checked
              checked_by: [tester]
              checked_on: 2026-01-01
              against: [s1]
            gotchas:
              - id: boot-pin
                severity: caution
                title: GPIO0 must be high at reset
                body: Pulling it low at reset starts the bootloader.
                pins: [gpio0]
                cites:
                  - { source: s1, page: 6 }
            """.trimIndent() + "\n"
    }
}
