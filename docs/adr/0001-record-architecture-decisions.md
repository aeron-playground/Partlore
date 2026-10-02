# 0001. Record architecture decisions

- Status: accepted
- Date: 2026-10-02

## Context

Partlore is meant to live for years. People who join later need to know why the project looks
the way it does, not only what it looks like. Chat threads and PR comments get lost.

## Decision

We write one short Markdown file per significant decision in `docs/adr/`, numbered in order
(`0001-…`, `0002-…`). Each file has a status, a date, and three sections: Context, Decision,
Consequences.

We write an ADR when we:

- pick or replace a framework, library or tool that is hard to swap out
- change the content schema or the content pack format
- change a design token (colour, type, spacing, elevation, motion)
- choose **not** to adopt a newer version on purpose

An accepted ADR is never edited to say something different. A new ADR replaces it, and the old
one gets the status `superseded by NNNN`.

## Consequences

- Decisions are easy to find and review in PRs.
- A small cost per decision: a few minutes of writing.
- Reviewers can ask "where's the ADR?" when a PR makes a big choice silently.
