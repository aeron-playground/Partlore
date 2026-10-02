# 0002. Tech stack

- Status: accepted
- Date: 2026-10-02

## Context

Partlore is an offline-first Android app with a lot of structured content and custom drawing
(pinouts). We want an iOS version later, so code that has no UI should be easy to share. We also
want to ship on F-Droid, which rules out proprietary libraries in that build.

## Decision

| Area | Choice |
|---|---|
| Language and build | Kotlin, Android Gradle Plugin 9 (built-in Kotlin support), Gradle version catalog |
| UI | Jetpack Compose via the Compose BOM, Material 3 (stable only, see 0005) |
| Navigation | Navigation 3, see 0004 |
| Dependency injection | Koin, see 0003 |
| Content | Read-only SQLite pack built by CI from `content/`, with full-text search |
| User data | Room in a separate database file; DataStore for settings |
| Network | Ktor client, only for content pack updates |
| Images | Coil 3 with SVG support |
| Tests | JUnit, Turbine, Compose UI tests, Roborazzi screenshot tests on Robolectric |
| Quality | Spotless + ktlint, detekt, Android Lint with warnings as errors |

Two product flavors:

- `play`: Google Play build. May use Google libraries that run on the device.
- `foss`: F-Droid build. Only open-source dependencies.

Exact versions live in `gradle/libs.versions.toml`. We always use the latest **stable** release.

Code without UI (`core:model`, `core:planner`, `core:calc`) stays pure Kotlin, with no Android
imports, so it can move to Kotlin Multiplatform later.

## Consequences

- One language for app, content tools and tests.
- Content is reviewed as text in PRs, then compiled into a fast database.
- Two flavors double some CI work, but keep the F-Droid build honest from day one.
- Whether the bundled SQLite driver supports FTS5 is still open. A spike will decide it in its
  own ADR, with FTS4 as the fallback.
