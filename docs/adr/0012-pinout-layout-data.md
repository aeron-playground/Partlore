# 0012. Pinout layout data

- Status: accepted
- Date: 2026-10-09

## Context

The pinout viewer draws pins around the board. The pin data said which header each pin is in and its
place in that header, but not where the header sits on the board or which end its first pin is at.
Drawing a header backwards shows wrong pins at the bench, which can damage hardware.

## Decision

- Each header in `pins.yaml` has `edges`: one or more runs, in pin order. A run names the board edge
  (`left`, `right`, `top`, `bottom`, or `inside` with a `run` direction), how many pin positions it
  holds, which end its first position is at (`first`), the side of row 1 for 2-row headers (`row1`),
  an `order` when headers share an edge, and its own source and place in that source.
- The drawing follows the board's edges, not its exact millimetres. A header that wraps around a
  module has several runs (ESP32-WROOM-32E: left, bottom, right).
- The validator checks that the runs add up to the header, that `first` and `row1` fit the run's
  direction, and every run's citation.
- The pack gets a `header_edge` table; the pack format goes from 1 to 2.
- A run value this app doesn't know keeps that header off the drawing; its pins stay in the table view.
  The app never draws pins in a guessed order.

## Consequences

- Every part with pins needs a few layout lines with a source; they're checked like other facts.
- `edges` is required, so an importer can only write `pins.yaml` when its dataset says where the
  headers sit. Otherwise the part comes in without pins until a person adds the layout lines with a
  source.
- Packs written by older tools (format 1) are rejected as damaged; packs ship inside the APK, so
  this only matters once the pack updater exists.
