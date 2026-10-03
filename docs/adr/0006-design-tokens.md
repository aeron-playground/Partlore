# 0006. Design tokens

- Status: accepted
- Date: 2026-10-03

## Context

Every screen should look like it belongs to the same app, in light, dark and Bench mode, at any
text size. Colors and sizes copied by hand into screens drift apart over time, and contrast
problems only show up when someone happens to notice them.

## Decision

All design values live in the `core:designsystem` module and are read through `PartloreTheme`:
`colors`, `typography`, `spacing`, `shapes`, `elevation` and `motion`. Screens never use raw
color values or ad-hoc sizes. `PartloreTheme` also feeds a matching Material 3 color scheme, so
stock Material components fit in.

Values we chose where the original brief left a gap:

| Token | Light | Dark | Why |
|---|---|---|---|
| `surfaceSunken` | `#F3EEE6` | `#0D0B12` | Code blocks, inputs and table stripes in dark mode |
| `warningContainer` | `#FFF4D6` | `#3A2C0A` | Caution cards in dark mode |
| `dangerContainer` | `#FDE8EB` | `#3D1620` | Danger cards in dark mode |
| `primaryContainer` | `#E9E3FF` | `#2E2650` | Selected tab, "prefilled from" chips |
| `info` | `#0068A3` | `#56B4E9` | "Community-checked" badge |
| `accent` | `#AB4F0E` | `#F0A060` | Copper; the lighter `#B4530F` fell below 4.5:1 on sunken surfaces |

- **Bench mode** uses the dark colors with a darker background (`#0B0A0F`) and slightly softer
  text (`#E6E1EE`).
- **Shadows** come from one light at the top-left, in up to three layers, tinted violet
  (`#413663`). Dark themes don't show shadows well, so raised surfaces get a lighter color and a
  thin highlight along the top edge instead.
- **Fonts** are bundled: Bricolage Grotesque for headings, Atkinson Hyperlegible Next for text,
  JetBrains Mono for pins and values. All three use the SIL Open Font License; the licence texts
  ship with the app.
- **Motion** uses springs. When the system turns animations off, or the user picks reduced
  motion, springs become short fades.

Tests guard these decisions:

- A contrast test checks 117 text and background pairs against WCAG AA (4.5:1), and body text
  against 7:1.
- Unit tests check the type scale, the shadow rules and the reduced-motion rules.
- Screenshot tests (Roborazzi) record every token in light, dark and Bench mode, at 200% text and
  on a tablet. CI fails when a screenshot changes without being re-recorded.

The screenshot tests run on Robolectric's Android 16 image. On the Android 17 image, the Espresso
version Roborazzi brings in calls an input API that Android 17 removed.

## Consequences

- Changing a token is a visible, reviewed change: it needs a new ADR, and the screenshots must be
  re-recorded in the same PR.
- A color that makes text unreadable fails the build before anyone ships it.
- Pin function colors, components and a lint rule that bans raw colors outside the token files
  come in later PRs.
- Moving the screenshot tests to the Android 17 image needs a newer Espresso.
