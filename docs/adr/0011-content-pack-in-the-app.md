# 0011. The content pack inside the app

- Status: accepted
- Date: 2026-10-04

## Context

The app must work offline from the first start. `content/` is built into one SQLite pack (ADR
0009). Android can't open a database that sits inside the APK, and a broken or half-copied pack
must never crash the app or show wrong data.

## Decision

- Every build bundles a pack built from the same commit. Debug builds bundle the preview pack,
  which includes drafts and says so on Library home. Release builds bundle the release pack
  (ADR 0010 decides what goes in). A Gradle task runs the packer and adds the pack to the
  variant's generated assets.
- On first use the app copies the pack into app storage: a temporary file, its SHA-256 compared
  with the manifest, flushed to disk, then renamed over the old pack in one step. A copy that
  doesn't match is deleted and the previous pack is kept.
- The app reads the pack read-only with the bundled SQLite build from `androidx.sqlite`, the same
  one the packer writes with, on one background thread. We don't use Room: it checks a schema
  hash, needs code generation and doesn't give us FTS5.
- A missing, damaged or too new pack shows an error screen with "Copy details", never a crash. A
  damaged installed pack is copied again from the APK once.
- The app makes no network calls. "Report a problem" and "Edit on GitHub" open the browser.

## Consequences

- The bundled SQLite adds 1.1 to 1.25 MB of native code per CPU type: about 4.7 MB in a universal
  APK, about 1.2 MB per device through Google Play. Per-CPU APKs for GitHub and F-Droid are
  decided at the next release.
- Release builds show an empty library until parts are checked.
- Robolectric can't load the native library, so app and feature tests use a fake repository. The
  real reader is tested on the JVM in `core:content`.
- `core:content` is built for the JVM, where `androidx.sqlite.SQLiteException` is a class. On Android
  it is only another name for `android.database.SQLException`, so naming it in `core:content` crashes
  the app. SQLite errors are caught through `catchingSqlite`; `AndroidCompatibilityTest` checks this.
- The pack updater can reuse the same install steps for downloaded packs.
