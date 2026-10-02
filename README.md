# Partlore

**What the datasheet doesn't tell you.**

Partlore is an offline Android reference for microcontroller boards, chips and modules.
Tap any pin to see what it does, filter pins by function, and read the gotchas that cost
people an afternoon: strapping pins, flash pins, input-only pins, ADC limits. Every number
links to the document it came from.

> **Status: early development.** There is no app to install yet. Follow the
> [roadmap](https://github.com/orgs/aeron-playground/projects/2) to see what's being built.

## What makes it different

- **Pins are data, not pictures.** Pinouts are drawn from structured files, so every pin can be
  tapped, searched, filtered and read by a screen reader.
- **Every fact has a source.** Each spec, pin and gotcha points to a datasheet page or official
  document, and shows how well it has been checked.
- **Works offline.** All content ships inside the app. No account, no ads, no tracking.
- **One GPIO, every name.** See the chip name, module pad, board label and Arduino pin number
  side by side.

## How it's built

| Part | Tech |
|---|---|
| App | Kotlin, Jetpack Compose, Material 3 |
| Content | YAML and Markdown in [`content/`](content/), checked by a schema |
| Content pack | One SQLite file built by CI and bundled with the app |

Big decisions are written down in [`docs/adr/`](docs/adr/).

## Build from source

Needs JDK 21 and the Android SDK (platform 37).

```bash
git clone https://github.com/aeron-playground/Partlore.git
cd Partlore
./gradlew check               # format, lint and tests
./gradlew assemblePlayDebug   # APK in app/build/outputs/apk/
```

There are two flavors: `play` for Google Play and `foss` for F-Droid. See
[CONTRIBUTING.md](CONTRIBUTING.md#setup) for the full setup.

## Contributing

Pin data, fixes and code are all welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) first.
Found a security problem? Please follow [SECURITY.md](SECURITY.md) and don't open a public issue.

## Licence

- Code: [Apache-2.0](LICENSE)
- Content in [`content/`](content/): [CC BY-SA 4.0](content/LICENSE)

Partlore is not affiliated with or endorsed by Arduino, Espressif, Raspberry Pi or any other
manufacturer. Product names are used only to describe the parts they refer to.
