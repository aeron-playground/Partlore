package dev.partlore.feature.part

import dev.partlore.core.model.Cite
import dev.partlore.core.model.SpecRow
import org.junit.Assert.assertEquals
import org.junit.Test

class SpecFormatTest {
    private fun row(
        min: Double? = null,
        typ: Double? = null,
        max: Double? = null,
        number: Double? = null,
        text: String? = null,
        unit: String? = "V",
    ) = SpecRow("Label", unit, min, typ, max, number, text, null, Cite("s1", 1, null, null))

    @Test fun aRangeShowsItsTypicalValue() = assertEquals("3 – 3.6 V (typ 3.3)", formatSpec(row(3.0, 3.3, 3.6)))

    @Test fun aTypicalValueAloneIsJustTheNumber() = assertEquals("5 V", formatSpec(row(typ = 5.0)))

    @Test fun anUpperLimitOnly() = assertEquals("≤ 240 mA", formatSpec(row(max = 240.0, unit = "mA")))

    @Test fun aLowerLimitOnly() = assertEquals("≥ -40 °C", formatSpec(row(min = -40.0, unit = "°C")))

    @Test fun aNegativeRange() = assertEquals("-0.3 – 3.6 V", formatSpec(row(min = -0.3, max = 3.6)))

    @Test fun aSingleNumber() = assertEquals("3.3 V", formatSpec(row(number = 3.3)))

    @Test fun textIsShownAsWritten() = assertEquals("802.11 b/g/n", formatSpec(row(text = "802.11 b/g/n", unit = null)))

    @Test fun noUnitMeansNoTrailingSpace() = assertEquals("26", formatSpec(row(number = 26.0, unit = null)))

    @Test fun bigNumbersHaveNoExponent() = assertEquals("240000000 Hz", formatSpec(row(number = 2.4e8, unit = "Hz")))

    @Test fun i2cAddressesAreHex() = assertEquals("0x3C", hexAddress(0x3C))
}
