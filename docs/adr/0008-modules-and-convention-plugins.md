# 0008. Modules and convention plugins

- Status: accepted
- Date: 2026-10-03

## Context

Until now the app had two modules, and each build file repeated the same Android settings: SDK
levels, Java version, Lint rules, Compose, Robolectric options. The app shell adds five more
modules. Copying those settings into every new build file would let them drift apart.

## Decision

The code is split into these modules:

| Module | What it holds |
|---|---|
| `app` | The activity, navigation (tabs, back stacks, page keys) and the wiring of everything else |
| `core:designsystem` | Theme, tokens, `Pl*` components, icons, the Design Catalog |
| `core:model` | Plain Kotlin types shared by everything (settings, and later parts and pins) |
| `core:userdata` | What the user chooses, saved on the device with DataStore |
| `core:testing` | Test helpers and fakes, used only by tests |
| `feature:settings` | The Settings screen and its ViewModel |
| `feature:onboarding` | The first-launch pages and their ViewModel |

Two rules keep the graph simple:

- A `feature:*` module depends only on `core:*` modules, never on another feature.
- Only `app` knows about navigation. A feature screen gets callbacks such as `onBack`, and `app`
  decides where they lead.

Shared build settings live in Gradle convention plugins in `build-logic/`:

| Plugin | Adds |
|---|---|
| `partlore.android.application` | SDK levels, Java 17, Lint as errors (app) |
| `partlore.android.library` | The same for libraries |
| `partlore.android.compose` | The Compose compiler, the Compose BOM and preview tooling |
| `partlore.android.screenshots` | Robolectric, Roborazzi and their test options; `check` compares screenshots |
| `partlore.android.feature` | All of the above plus the core modules, Koin and Lifecycle that every screen uses |
| `partlore.jvm.library` | Plain Kotlin modules with no Android code |

The security floor for libraries that AGP and Android Lint pull in still comes from the root
build, so it applies to every module, including new ones.

## Consequences

- A new feature module needs a three-line build file:

  ```kotlin
  plugins { id("partlore.android.feature") }
  android { namespace = "dev.partlore.feature.<name>" }
  dependencies { /* only what this feature adds */ }
  ```

- A change to an SDK level or a Lint rule happens in one place.
- Each module with Robolectric tests has its own `src/test/resources/robolectric.properties`
  that pins the Android image (see 0006).
- Build logic is code too: it is formatted and checked like the rest.
