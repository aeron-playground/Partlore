# Contributing to Partlore

Thanks for helping. There are two kinds of contributions:

- **Content**: pin data, specs, gotchas and articles in `content/`. You don't need to know Kotlin.
- **Code**: the Android app and the content tools.

Not sure where to start? Look for issues labelled
[`good first issue`](https://github.com/aeron-playground/Partlore/labels/good%20first%20issue),
or ask in [Discussions](https://github.com/aeron-playground/Partlore/discussions).

## Setup

You need:

- **JDK 21** (for example [Temurin](https://adoptium.net/))
- **Android SDK** with platform 37 (Android Studio installs it, or use the command-line tools)

Then, once per clone:

```bash
git config core.hooksPath .githooks   # turn on the commit checks
./gradlew check                       # build and run every check
```

If Gradle can't find the SDK, create `local.properties` with `sdk.dir=/path/to/Android/Sdk`.
This file is ignored by git.

### Commands

| Command | What it does |
|---|---|
| `./gradlew check` | Formatting, detekt, Android Lint, unit tests, screenshot comparison, content checks and the content pack |
| `./gradlew spotlessApply` | Fix formatting |
| `./gradlew assemblePlayDebug` | Build the Google Play debug APK |
| `./gradlew assembleFossDebug` | Build the F-Droid debug APK |
| `./gradlew installPlayDebug` | Install the debug app on a connected device or emulator |
| `./gradlew :<module>:recordRoborazziDebug` | Re-record a module's screenshots after an intended UI change |
| `./gradlew :app:recordRoborazziPlayDebug` | The same for the app module (it has flavors) |
| `./gradlew verifyRoborazziDebug verifyRoborazziPlayDebug` | Compare screenshots with the committed ones (part of `check`) |
| `./gradlew validateContent` | Check every part in `content/` |
| `./gradlew packContent` | Build the content pack (`-Ppreview` includes drafts) |
| `./gradlew contentChecklist -Ppart=<maker>/<part>` | Write a page-grouped checklist for checking a part |
| `./gradlew :core:content:speedTest` | Read pages from a 5,000-part pack and check each read stays under 20 ms |

### Project layout

| Module | What it holds |
|---|---|
| `app` | The activity, navigation and the wiring of everything else |
| `core:designsystem` | Theme, tokens, `Pl*` components, icons, the Design Catalog |
| `core:model` | Plain Kotlin types shared by everything |
| `core:userdata` | What the user chooses, saved on the device |
| `core:testing` | Test helpers and fakes |
| `core:packformat` | The tables of the content pack, shared by the packer and the app |
| `core:content` | Reads the content pack in the app, and installs the pack bundled in the APK |
| `feature:*` | One screen or flow each, for example `feature:settings` |
| `tools:validator` | Checks the files in `content/` (runs on your computer, not in the app) |
| `tools:packer` | Builds the content pack from `content/` |
| `build-logic` | Shared build settings (convention plugins) |

A feature module depends only on `core:*` modules. Only `app` knows about navigation. See
[ADR 0008](docs/adr/0008-modules-and-convention-plugins.md) for how to add a module.

### Checks on every pull request

| Check | What it runs |
|---|---|
| `lint` | `./gradlew spotlessCheck detekt lint` |
| `test` | `./gradlew test verifyRoborazziDebug verifyRoborazziPlayDebug validateContent packContent :tools:packer:scaleTest :core:content:speedTest` (unit tests, screenshots, content checks, and the speed targets for packing and reading 5,000 parts) |
| `build` | Debug APKs for both flavors, an unsigned F-Droid release build (checks R8), and the preview content pack with checklists as an artifact |
| `dco` | Every commit has a matching `Signed-off-by` |
| `dependency-review` | New dependencies have no known high-severity vulnerabilities |
| `pr-title` | The PR title follows the commit format |
| `Analyze (…)` | CodeQL security scan of the Kotlin code and the workflows |

### Git hooks

`core.hooksPath .githooks` turns on two checks:

- **pre-commit**: checks Kotlin formatting when Kotlin files are staged.
- **commit-msg**: checks the commit message format and the DCO sign-off.

## The workflow

1. **Open or pick an issue.** Every change starts with one. Say you're working on it.
2. **Create a branch** from `main`: `<type>/<scope>-<short-name>`, for example
   `feat/pinout-filter-chips` or `fix/content-esp32-gpio12`.
   Types: `feat`, `fix`, `chore`, `docs`, `refactor`, `test`, `build`, `ci`.
3. **Commit with a sign-off** (see DCO below).
4. **Open a pull request.** Fill in the template and link the issue with `Closes #123`.
5. **Wait for checks**, fix anything that fails, and answer review comments.
6. A maintainer **squash-merges** the PR. Your branch is deleted automatically.

Nobody pushes to `main` directly, maintainers included.

## Commit messages

We use [Conventional Commits](https://www.conventionalcommits.org/). One line, lowercase,
imperative, at most 72 characters:

```
type(scope): what this commit does
```

Examples:

```
feat(app): add filter chips to the pinout viewer
fix(content): correct esp32 gpio12 strapping level
docs(docs): explain the verified status
```

- **Types:** `feat`, `fix`, `perf`, `refactor`, `test`, `docs`, `build`, `ci`, `chore`, `revert`
- **Scopes:** `app`, `design`, `content`, `tools`, `ci`, `docs`, `deps`, `planner`, `ocr`,
  `repo`, `release`, `build`

The PR title must follow the same format, because it becomes the commit on `main`.

## DCO sign-off

Every commit needs a `Signed-off-by` line. It means you agree to the
[Developer Certificate of Origin](https://developercertificate.org/): you wrote the change, or
otherwise have the right to submit it under this project's licences.

Git adds the line for you:

```bash
git commit -s -m "fix(content): correct esp32 gpio12 strapping level"
```

The name and email must match the commit author. Forgot to sign off? Fix the whole branch:

```bash
git rebase --signoff origin/main
git push --force-with-lease
```

## Content rules

Wrong pin data can damage someone's hardware. These rules are strict on purpose.

- **Every fact needs a source.** A datasheet, reference manual, errata sheet or official
  document, with the page number. Community sources are fine as a second source, not as the only
  one.
- **Write in your own words.** Numbers are facts and can be used. Tables, diagrams and text from
  datasheets cannot be copied. Summarise and redraw.
- **Link only, never copy,** from: iFixit, WikiChip, TechPowerUp, Octopart/Nexar, and any
  datasheet PDF. Their licences don't allow it.
- **No manufacturer images or logos.** Board art is drawn by contributors.
- **Never** use leaked schematics, boardviews or confidential documents.
- New parts start as `draft`. Before a file can become `checked`, a person compares every value
  with the place it cites and records that in the file's `status`. `verified` needs a second,
  different person.

How to add, cite and check a part: [content/README.md](content/README.md).

Content in `content/` is licensed CC BY-SA 4.0. By contributing it, you agree to that licence.

## Code rules

- Format and lint before you push. The checks in CI are the same ones you run locally.
- UI code uses design tokens only: no raw colours, sizes or animation timings.
- New UI components go into the Design Catalog first, then into screens.
- UI changes need screenshots in the PR: light and dark theme, normal and 200% font size.
- Don't add a dependency without discussing it in the issue first.
- No analytics, ads, trackers or crash-reporting SDKs. The app collects nothing.

Code is licensed Apache-2.0. By contributing it, you agree to that licence.

## Releases

Releases are automatic. A bot keeps one open "release PR" that collects the changes since the
last release from the commit messages on `main`. Merging that PR tags the release (`app-v0.1.0`)
and publishes it on GitHub.

- Below 1.0.0: `feat`, `fix` and `perf` commits bump the patch version (0.0.1 → 0.0.2), and a
  breaking change (`feat!:`) bumps the minor version (0.0.2 → 0.1.0).
- Never edit `CHANGELOG.md` or `version.txt` by hand. The release PR updates both.

## Reporting problems

- Wrong pin or spec? Use the **Content error** issue form.
- App bug? Use the **Bug report** form.
- Security problem? **Don't open an issue.** Follow [SECURITY.md](SECURITY.md).
