# 0013. Importers write facts, people write judgments

- Status: accepted
- Date: 2026-10-31

## Context

ADR 0010 allows `imported` files, and ADR 0012 asks for layout runs on every header. Open datasets
list pins, but not everything a pin file needs. Espressif's board guides (esp-dev-kits) give each
header's pins in order with their names, types and functions. They don't say which board edge a
header is on, the pin pitch, or whether a pin is safe to use: those come from the board drawing,
the schematic and the chip datasheet. Some of that is a judgment, and a wrong one can damage
hardware.

## Decision

- An importer copies only what its dataset states: here the pin tables (order, names, types,
  signals).
- Everything else comes from a hints file a person writes for each board, in
  `content/import/<importer>/`: layout runs, pitch, `safe` for every pin, strapping, voltage,
  5 V tolerance, notes, and board labels that differ from the dataset. Every hint cites its source.
- The importer stops instead of guessing: an unknown signal, a pin without `safe`, a hint for a pin
  the dataset doesn't have, or a guide with uncommitted changes.
- It lists what the person checking the board must look at: footnotes, pins the guide's notes
  name, words that aren't pin functions, and board labels.
- Its files are `imported` and record their origin (`imported_from` and the dataset commit). It
  never overwrites a file it didn't write, or one a person has moved past `imported`.
- A test re-imports a board a person already checked (ESP32-DevKitC V4) and compares every value
  the importer works out itself. It needs a local clone of the dataset, so CI doesn't run it.

## Consequences

- Imported boards ship their pins only after a person checks them, as ADR 0010 says.
- A hints file is real work: about one line per pin, plus the sources. It is still far less than
  writing the pins by hand, and the risky values are the ones a person writes.
- Hints are content (CC BY-SA 4.0), so they live in `content/`, not next to the importer's code.
- Each new dataset needs its own reader; the hints format and these rules stay the same.
