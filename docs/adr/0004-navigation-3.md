# 0004. Navigation 3

- Status: accepted
- Date: 2026-10-02

## Context

The app has four top-level destinations (Library, Search, Tools, Bench), a part page reached from
many places, and adaptive layouts: list and detail side by side on tablets and foldables. Deep
links to a part must open with a sensible back stack.

Navigation 3 is now stable (1.2.0). In it, the back stack is a plain list that the app owns and
changes directly. The library only displays it.

## Decision

We use Navigation 3 with typed, serializable route keys. Each top-level tab keeps its own back
stack.

## Consequences

- Navigation state is ordinary app state: easy to test, save and restore.
- Adaptive list-detail layouts and predictive back are supported.
- Fewer examples and answers online than for Navigation 2, so we document our patterns here.
- Deep links build their back stack in our code, which we must test.
