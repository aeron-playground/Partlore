# 0010. Verification levels and imported data

- Status: accepted
- Date: 2026-10-04

## Context

Partlore wants to cover a very large number of parts, but a wrong pin can damage hardware. The
project starts with one maintainer, so rules that require two reviewers would keep every part out
of the app, and bulk data from open datasets will arrive faster than people can check it.

## Decision

- Every value cites its source and where in it: a page for PDFs, a section for web pages, a path
  for datasets. A person must check each value against that place before it leaves `draft`. We
  record who checked, when and against which sources; we don't record how a draft was written.
- Levels: `draft`, `imported`, `checked` (one person), `verified` (two different people),
  `needs-review`, `disputed`. The app will show the level as it is, for example "Checked by 1
  person".
- `part.yaml`, `pins.yaml` and `gotchas.yaml` each have their own level.
- Imported parts ship with their name, maker, specs, I²C addresses and source links, marked as
  imported. Their pins and gotchas ship only after a person checks them, because pin data is
  where mistakes do damage. Gotchas are written by people and are never imported.
- Sources must have a licence that allows our use (CC0, CC BY, CC BY-SA, Apache-2.0, MIT, BSD) or
  be `link-only`, in which case we store facts and locations but never copied text or pictures.

## Consequences

- Release packs contain only data a person has checked, except the clearly marked basics of
  imported parts.
- The library can grow from open datasets without lowering the bar for pinouts.
- "Check this pinout" becomes a clear way for the community to help.

## Update 2026-10-05

The status badge shows only the level, for example "Checked" or "Verified", without a count of
people. Who checked each file, when, and against which sources is still shown on the part's page,
in the Sources section and the status sheet. The badge still shows the least checked of the part's
files.
