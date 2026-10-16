package dev.partlore.core.testing

import dev.partlore.core.model.BoardEdge
import dev.partlore.core.model.CategoryPage
import dev.partlore.core.model.CategoryTile
import dev.partlore.core.model.Cite
import dev.partlore.core.model.FileStatus
import dev.partlore.core.model.FiveVoltTolerance
import dev.partlore.core.model.Glance
import dev.partlore.core.model.GotchaItem
import dev.partlore.core.model.HeaderRun
import dev.partlore.core.model.LibraryHome
import dev.partlore.core.model.PartCard
import dev.partlore.core.model.PartPage
import dev.partlore.core.model.PartRef
import dev.partlore.core.model.PinDirection
import dev.partlore.core.model.PinFunction
import dev.partlore.core.model.PinHeader
import dev.partlore.core.model.PinInfo
import dev.partlore.core.model.PinSafety
import dev.partlore.core.model.Pinout
import dev.partlore.core.model.RunAxis
import dev.partlore.core.model.Severity
import dev.partlore.core.model.Side
import dev.partlore.core.model.SourceItem
import dev.partlore.core.model.SpecRow
import dev.partlore.core.model.Strapping
import dev.partlore.core.model.Tag
import dev.partlore.core.model.VerificationLevel

/** Made-up parts for tests and screenshots. They look real but describe no real product. */
object SampleContent {
    private val wifi = Tag("wifi", "Wi-Fi")
    private val bluetooth = Tag("bluetooth", "Bluetooth")
    private val logic33 = Tag("3v3-logic", "3.3 V logic")
    private val logic5 = Tag("5v-logic", "5 V logic")
    private val usbB = Tag("usb-b", "USB-B")

    val devCard =
        PartCard(
            "example/devboard-v1",
            "Example DevBoard V1",
            "board",
            "dev-boards",
            VerificationLevel.Draft,
            listOf(bluetooth, logic33, wifi),
        )
    val classicCard =
        PartCard(
            "example/classic-board",
            "Example Classic Board",
            "board",
            "classic-boards",
            VerificationLevel.Checked,
            listOf(logic5, usbB),
        )
    val moduleCard =
        PartCard(
            "example/radio-module",
            "Example Radio Module",
            "module",
            "radio-modules",
            VerificationLevel.Verified,
            listOf(wifi),
        )

    val library =
        LibraryHome(
            isPreview = true,
            partCount = 3,
            starterBoards = listOf(classicCard, devCard),
            categories = listOf(CategoryTile("boards", "Boards", 2), CategoryTile("modules", "Modules", 1)),
        )

    val emptyLibrary =
        LibraryHome(
            false,
            0,
            emptyList(),
            listOf(CategoryTile("boards", "Boards", 0), CategoryTile("modules", "Modules", 0)),
        )

    val boards =
        CategoryPage(
            id = "boards",
            name = "Boards",
            children = listOf(
                CategoryTile("classic-boards", "Classic boards", 1),
                CategoryTile("dev-boards", "Dev boards", 1),
            ),
            tags = listOf(logic33, logic5, bluetooth, usbB, wifi),
            parts = listOf(classicCard, devCard),
            partsByChild = mapOf("classic-boards" to setOf(classicCard.id), "dev-boards" to setOf(devCard.id)),
        )

    // Named arguments say what each made-up number is.
    private val datasheet = Cite(sourceId = "s1", page = 28, section = null, ref = null)
    private val guide = Cite("s2", null, "Power options", null)

    private val devSources =
        listOf(
            SourceItem(
                "s1",
                "datasheet",
                "Example DevBoard Datasheet",
                "Example Labs",
                "https://example.com/devboard.pdf",
                "1.2",
                "2026-10-04",
                "link-only",
            ),
            SourceItem(
                "s2",
                "web-doc",
                "Example DevBoard guide",
                "Example Labs",
                "https://example.com/devboard",
                null,
                "2026-10-04",
                "CC-BY-SA-4.0",
            ),
        )

    private val pinCite = Cite(sourceId = "s1", page = 12, section = null, ref = null)

    // A made-up board, never real content: 4 pins on each side, one of each kind the viewer draws.
    val devBoardPinout =
        Pinout(
            partId = devCard.id,
            partName = devCard.name,
            headers = listOf(sampleHeader("left", "J1", BoardEdge.Left), sampleHeader("right", "J2", BoardEdge.Right)),
            pins =
            listOf(
                samplePin("3v3", "left", index = 1, names = Triple(null, "3V3", "3V3"), direction = PinDirection.Power),
                samplePin(
                    "io2",
                    "left",
                    index = 2,
                    names = Triple("GPIO2", "IO2", "D4"),
                    functions = listOf(PinFunction("gpio", null, false), PinFunction("adc", "ADC2_CH2", false)),
                    safe = PinSafety.Caution,
                    strapping = Strapping("boot-mode", "low", "reset"),
                ),
                samplePin("io6", "left", index = 3, names = Triple("GPIO6", "SCK", "CLK"), safe = PinSafety.Avoid),
                samplePin(
                    "gnd-left",
                    "left",
                    index = 4,
                    names = Triple(null, "GND", "GND"),
                    direction = PinDirection.Ground,
                ),
                samplePin(
                    "gnd-right",
                    "right",
                    index = 1,
                    names = Triple(null, "GND", "GND"),
                    direction = PinDirection.Ground,
                ),
                samplePin(
                    "io21",
                    "right",
                    index = 2,
                    names = Triple("GPIO21", "IO21", "D21"),
                    functions = listOf(PinFunction("i2c", "SDA", true)),
                ),
                samplePin(
                    "io22",
                    "right",
                    index = 3,
                    names = Triple("GPIO22", "IO22", "D22"),
                    functions = listOf(PinFunction("i2c", "SCL", true)),
                ),
                samplePin(
                    "io34",
                    "right",
                    index = 4,
                    names = Triple("GPIO34", "IO34", "D34"),
                    direction = PinDirection.In,
                ),
            ),
            sources = devSources,
            status = FileStatus(VerificationLevel.Draft, emptyList(), null, emptyList(), null),
            packVersion = "0.1.0",
        )

    val devBoard =
        PartPage(
            id = devCard.id,
            name = devCard.name,
            aliases = listOf("EDB1"),
            kind = "board",
            manufacturer = "Example Labs",
            summary = "A made-up development board used in tests.",
            uses = PartRef(moduleCard.id, moduleCard.name),
            usedBy = emptyList(),
            related = listOf(PartRef(classicCard.id, classicCard.name)),
            glance =
            Glance(
                logicLevelV = 3.3,
                fiveVoltTolerant = FiveVoltTolerance.None,
                gpioCount = 26,
                adcChannels = 16,
                wifi = "802.11 b/g/n",
                bluetooth = "5.0",
            ),
            specs =
            listOf(
                spec("Input voltage", typ = 5.0, condition = "USB or the 5V pin", cite = guide),
                spec("Supply voltage", min = 3.0, typ = 3.3, max = 3.6),
                spec("Logic level", number = 3.3),
                spec("Wi-Fi", unit = null, text = "802.11 b/g/n, 2.4 GHz"),
            ),
            absoluteMax = listOf(spec("Supply voltage", min = -0.3, max = 3.6)),
            i2c = emptyList(),
            gotchas =
            listOf(
                GotchaItem(
                    "one-power",
                    Severity.Danger,
                    "Power the board from one source only",
                    "USB, the 5V pin or the 3V3 pin: never two at once.",
                    emptyList(),
                    listOf(guide),
                ),
                GotchaItem(
                    "boot-pin",
                    Severity.Caution,
                    "Keep IO0 high at reset",
                    "Low at reset starts download mode.",
                    listOf("IO0"),
                    listOf(datasheet),
                ),
                GotchaItem(
                    "boot-button",
                    Severity.Info,
                    "Boot plus Reset starts download mode",
                    "Hold Boot and press Reset.",
                    emptyList(),
                    listOf(guide),
                ),
            ),
            sources = devSources,
            partStatus = FileStatus(VerificationLevel.Draft, emptyList(), null, emptyList(), null),
            pinsStatus = FileStatus(VerificationLevel.Draft, emptyList(), null, emptyList(), null),
            gotchasStatus = FileStatus(VerificationLevel.Draft, emptyList(), null, emptyList(), null),
            packVersion = "0.1.0",
            pinout = devBoardPinout,
        )

    private fun sampleHeader(id: String, label: String, edge: BoardEdge) = PinHeader(
        id = id,
        label = label,
        type = "header",
        rows = 1,
        pinsPerRow = 4,
        pitchMm = 2.54,
        runs = listOf(
            HeaderRun(edge, RunAxis.Vertical, pins = 4, first = Side.Top, row1 = null, order = null, cite = pinCite),
        ),
        cite = pinCite,
    )

    private fun samplePin(
        id: String,
        header: String,
        index: Int,
        names: Triple<String?, String?, String?>,
        direction: PinDirection = PinDirection.Io,
        functions: List<PinFunction> = emptyList(),
        safe: PinSafety = PinSafety.Ok,
        strapping: Strapping? = null,
    ) = PinInfo(
        id = id,
        headerId = header,
        row = 1,
        index = index,
        chipName = names.first,
        modulePad = names.second,
        boardLabel = names.third,
        arduino = null,
        aliases = emptyList(),
        direction = direction,
        voltage = if (direction == PinDirection.Ground) null else 3.3,
        fiveVoltTolerant = if (direction == PinDirection.Io) false else null,
        functions = functions,
        safe = safe,
        strapping = strapping,
        note = null,
        cites = listOf(pinCite),
        gotchas = emptyList(),
    )

    private fun spec(
        label: String,
        unit: String? = "V",
        min: Double? = null,
        typ: Double? = null,
        max: Double? = null,
        number: Double? = null,
        text: String? = null,
        condition: String? = null,
        cite: Cite = datasheet,
    ) = SpecRow(label, unit, min, typ, max, number, text, condition, cite)
}
