# 0007. Static font files instead of variable fonts

- Status: accepted (updates the font part of 0006)
- Date: 2026-10-03

## Context

0006 bundled one variable font file per family and picked weights through the font's weight axis.
On a real phone (Android 11) the weights were not applied: `label` (600) and `caption` (500)
drew exactly like regular text, and Bricolage headings fell back to the font's heavy default.
Robolectric screenshots showed the same, so the committed screenshots had locked in wrong output.
The existing typography test only checked that each weight was declared, not that it was drawn.

## Decision

Bundle the publishers' official static files, one per weight we use, unmodified:

| Family | Files |
|---|---|
| Atkinson Hyperlegible Next | Regular, Medium, SemiBold |
| Bricolage Grotesque | SemiBold, Bold |
| JetBrains Mono | Medium |

No static file exists for weight 650, so `headline` moves from 650 to 700 (Bold). Headings stay
distinct through size (24, 32 and 40 sp).

A new test draws each declared weight and checks that heavier weights put more ink on screen.

## Consequences

- Weights look the same on every Android version, with no dependence on variation support.
- Total font size stays about the same (about 700 KB).
- Adding a weight means adding a file, and the rendering test must show it drawing heavier.
