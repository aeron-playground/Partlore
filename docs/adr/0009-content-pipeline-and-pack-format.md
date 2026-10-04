# 0009. Content pipeline and pack format

- Status: accepted
- Date: 2026-10-04

## Context

Parts are written by people in a public repository and read by the app offline. A typo in a pin
can damage hardware, so mistakes have to be caught before a pull request is merged, and the app
needs the data in a form it can search instantly.

## Decision

- Parts are YAML files under `content/parts/<maker>/<part>/`. The folder path is the part's id
  (for example `espressif/esp32-wroom-32e`). It will also be the part's web address, and it never
  changes.
- JSON Schema files in `content/schema/` define what each file may contain. Editors use them for
  autocomplete. The validator (`tools/validator`) checks them and adds the rules a schema can't
  express: links between files, citations, pin positions, safety, licences and status.
- The validator reads everything into one checked model. Writers turn that model into outputs:
  today the SQLite pack, later data for the website.
- The packer (`tools/packer`) writes one read-only SQLite file with the bundled SQLite build the
  app will use, including an FTS5 search index with the trigram tokenizer, so any three
  characters of a name find it. The table definitions live in `core:packformat`, shared by the
  packer and, later, the app.
- The same content always gives a byte-identical pack: rows are written in a fixed order, no
  timestamps are stored, and the file is compacted at the end.
- `manifest.json` lists the packs with size and SHA-256. It is a list so packs can be split by
  family later. Signing comes with the pack updater.
- The tools run inside the Gradle build with no network access, so anyone, including F-Droid,
  can rebuild the pack from source. `./gradlew check` validates and packs.

## Consequences

- A contributor gets every problem with file, line and column, locally and on the pull request.
- Changing the pack's tables raises its schema version; packs are always rebuilt from source.
- The website can be built from the same checked data without a second pipeline.
