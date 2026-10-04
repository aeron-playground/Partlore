# Content

Everything the app knows about parts lives here as text files, licensed CC BY-SA 4.0 (see
[LICENSE](LICENSE)). The tools in `tools/` check these files and build the content pack the app
reads.

## Layout

```
version.txt              version of the content pack
categories.yaml          the category tree of the Library tab
tags.yaml                tags parts may use
schema/                  the rules for every file (JSON Schema) and the allowed spec keys
parts/<maker>/<part>/    one folder per part; the folder path is the part's id
  part.yaml              name, maker, kind, specs, I²C addresses, status
  sources.yaml           the documents every fact comes from
  pins.yaml              every pin, by header (optional)
  gotchas.yaml           traps the datasheet doesn't make obvious (optional)
  article.md             a longer text in our own words (optional)
```

Each YAML file starts with a `# yaml-language-server: $schema=…` line. Editors with YAML
language support, such as VS Code with the Red Hat YAML extension, use it to autocomplete keys
and underline mistakes while you type.

## Adding a part

1. Make the folder `parts/<maker>/<part>/`. The maker is the manufacturer's name in lowercase
   with hyphens; the part is its name the same way. The id never changes once published.
2. List every document in `sources.yaml` with its link, the date you downloaded it and its
   licence. PDFs also need their version and the SHA-256 of the exact file you read. Datasheets
   are `license: link-only`: we store facts and where they are, never copied text or pictures.
3. Write `part.yaml`, `pins.yaml` and `gotchas.yaml` with every value cited:
   - PDF documents: `page`, the page number your PDF viewer shows (the first page of the file
     is 1).
   - Web pages: `section`, the heading the value is under.
   - Open datasets: `ref`, the path inside the dataset.
4. Leave out anything a document doesn't state clearly. Missing is better than wrong.
5. Start with `status: { level: draft }` and run the checks below.

## Status levels

| Level | Meaning | In release packs |
|---|---|---|
| `draft` | Work in progress | No (preview packs only) |
| `imported` | Copied by a tool from an open dataset; no person has checked it | The part yes; its pins and gotchas no |
| `checked` | One person checked every value against its cited place | Yes |
| `verified` | Two different people checked independently | Yes |
| `needs-review` | A source changed or a problem was reported | Yes, marked as needing review |
| `disputed` | Sources disagree | Yes, marked as disputed |

`part.yaml`, `pins.yaml` and `gotchas.yaml` each have their own status. Gotchas are written by
people, so `gotchas.yaml` is never `imported`.

## Commands

| Command | What it does |
|---|---|
| `./gradlew validateContent` | Checks every rule; problems print as `content/…:line:column` |
| `./gradlew contentChecklist -Ppart=<maker>/<part>` | Writes a checklist grouped by document and page to `build/content/checklists/` |
| `./gradlew packContent` | Builds the release pack in `build/content/pack/`; with `-Ppreview`, a pack that includes drafts in `build/content/preview/` |
