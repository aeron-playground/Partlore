# Security policy

## Reporting a vulnerability

Please report security problems **privately**, through GitHub:

**[Report a vulnerability](https://github.com/aeron-playground/Partlore/security/advisories/new)**

Don't open a public issue, discussion or PR for a security problem.

Include:

- what the problem is and what an attacker could do with it
- steps to reproduce, or a proof of concept
- the app version or commit you tested

You'll get a first reply within 7 days. We'll keep you updated while we work on a fix, and credit
you in the advisory unless you'd rather stay anonymous.

## In scope

- The Android app
- Content pack updates: download, signature and hash checks, and how the pack is swapped in
- Parsing of content files and Markdown in the app
- The content tools in `tools/` and the GitHub Actions workflows

## Not a security issue

Wrong pin data, specs or gotchas are serious, but they are content errors. Use the
**Content error** issue form so others can see and check the fix. If following the wrong data
could damage hardware, say so in the report.

## Supported versions

Partlore has no public release yet. Once it does, only the latest release gets security fixes.

## We will never

- ask for your password, keys or tokens
- ask you to install anything to "verify" a report
