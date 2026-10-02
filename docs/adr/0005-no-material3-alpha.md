# 0005. No Material 3 alpha on main

- Status: accepted
- Date: 2026-10-02

## Context

Material 3 1.4.0 is the latest stable release. The "Expressive" APIs (new motion and shape
morphing) are only in 1.5.0, which is still in alpha. Alpha APIs can change or disappear between
releases.

## Decision

`main` uses stable Material 3 only. We get the expressive feel from our own design tokens:

- spring-based motion tokens in our design system
- shape morphing with `androidx.graphics:graphics-shapes`, which is stable

We revisit this when Material 3 1.5.0 is stable, in a new ADR.

## Consequences

- No surprise breakage from alpha API changes.
- Some code we write now may later be replaced by Material 3 components.
- Dependency update PRs that propose a Material 3 alpha must be declined.
