package dev.partlore.tools.importer.rst

import dev.partlore.tools.importer.ImportException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

// Made-up tables in the two forms the guides use. Not copied from any guide.
class PinTablesTest {
    @Test
    fun aSimpleTableUnderItsHeadingIsRead() {
        val tables = PinTables.read(SIMPLE)
        assertEquals(listOf("J1"), tables.map { it.heading })
        assertEquals(
            listOf(
                TableRow(1, "3V3", "P", "3.3 V power supply", emptySet()),
                TableRow(2, "IO4", "I/O/T", "GPIO4, ADC1_CH3", setOf("2")),
                TableRow(3, "IO5", "I/O/T", "GPIO5, ADC1_CH4, TOUCH5", emptySet()),
                TableRow(4, "G", "G", "Ground", emptySet()),
            ),
            tables.single().rows,
        )
    }

    @Test
    fun aListTableIsReadAndOtherTablesAreSkipped() {
        val tables = PinTables.read(LIST)
        assertEquals(listOf("J2"), tables.map { it.heading })
        assertEquals(
            listOf(
                TableRow(1, "3V3", "P", "3.3 V power supply", emptySet()),
                TableRow(2, "IO25", "I/O", "GPIO25, ADC2_CH8, DAC_1", setOf("3")),
            ),
            tables.single().rows,
        )
    }

    @Test
    fun tablesKeepTheirOrderInTheFile() {
        assertEquals(listOf("J1", "J2"), PinTables.read(SIMPLE + "\n" + LIST).map { it.heading })
    }

    @Test
    fun aPinNumberThatIsNotANumberStopsTheRead() {
        val broken = SIMPLE.replace("4    G ", "x    G ")
        val error = assertThrows(ImportException::class.java) { PinTables.read(broken) }
        assertTrue(error.message, error.message!!.contains("'x'"))
    }

    private companion object {
        val SIMPLE =
            """
            Header Block
            ------------

            Two headers, one on each side.

            J1
            ^^^

            ===  ====  ==========  ====================
            No.  Name  Type [#]_   Function
            ===  ====  ==========  ====================
            1    3V3   P           3.3 V power supply
            2    IO4   I/O/T       GPIO4, ADC1_CH3 [2]_
            3    IO5   I/O/T       GPIO5, ADC1_CH4,
                                   TOUCH5
            4    G     G           Ground
            ===  ====  ==========  ====================
            """.trimIndent()

        val LIST =
            """
            Parts
            -----

            .. list-table::
                :header-rows: 1

                * - Part
                  - What it does
                * - USB port
                  - Power and data

            J2
            ^^^

            .. list-table::
                :header-rows: 1
                :widths: 10 20 20 50

                * - No.
                  - Name
                  - Type [1]_
                  - Function
                * - 1
                  - 3V3
                  - P
                  - 3.3 V power supply
                * - 2
                  - IO25
                  - I/O
                  - GPIO25, ADC2_CH8,
                    DAC_1 [3]_
            """.trimIndent()
    }
}
