# 0003. Koin for dependency injection

- Status: accepted
- Date: 2026-10-02

## Context

The app needs dependency injection to wire repositories, the content database and settings into
screens without global singletons. The two common choices on Android are Hilt and Koin.

Hilt is built on Dagger and generates code at compile time. It only works on Android and the JVM.
Koin is a Kotlin library with no code generation and supports Kotlin Multiplatform, including iOS.

## Decision

We use Koin, with its BOM to keep module versions aligned.

## Consequences

- The same wiring code can follow `core:*` modules to iOS later.
- No annotation processing step, so builds stay simpler.
- Koin resolves dependencies at runtime, so a missing binding shows up as a crash, not a compile
  error. To catch this early, a unit test checks the full Koin module graph.
