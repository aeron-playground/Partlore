package dev.partlore.tools.packer

import dev.partlore.tools.content.model.GotchaFile
import dev.partlore.tools.content.model.PinFile

internal fun Rows.pins(partId: String, file: PinFile) {
    file.headers.forEach { h ->
        insert("header", listOf(partId, h.id, h.label, h.type, h.rows, h.pinsPerRow, h.pitchMm) + cite(h.cite))
    }
    file.pins.forEach { p ->
        insert(
            "pin",
            listOf(
                partId, p.id, p.header, p.row, p.index, p.chipName, p.modulePad, p.boardLabel, p.arduino, p.direction,
                p.voltage, p.fiveVTolerant, p.safe, p.strapping?.role, p.strapping?.mustBe, p.strapping?.at, p.note,
            ),
        )
        p.cites.forEachIndexed { seq, c -> insert("pin_cite", listOf(partId, p.id, seq) + cite(c)) }
        p.names.forEach { insert("pin_alias", listOf(partId, p.id, it, it.lowercase())) }
        p.functions.forEachIndexed { seq, f ->
            insert("pin_function", listOf(partId, p.id, seq, f.type, f.signal, f.isDefault))
        }
    }
}

internal fun Rows.gotchas(partId: String, file: GotchaFile) = file.gotchas.forEach { g ->
    insert("gotcha", listOf(partId, g.id, g.severity, g.title, g.body))
    g.pins.forEach { insert("gotcha_pin", listOf(partId, g.id, it)) }
    g.cites.forEachIndexed { seq, c -> insert("gotcha_cite", listOf(partId, g.id, seq) + cite(c)) }
}
