#!/bin/sh
# Checks a commit message or a PR title against the project rules.
#
#   check-commit-msg.sh title "<text>"   header format only (PR titles in CI)
#   check-commit-msg.sh file <path>      header format + DCO sign-off (commit-msg hook)
set -eu

types='feat|fix|perf|refactor|test|docs|build|ci|chore|revert'
scopes='app|design|content|tools|ci|docs|deps|planner|ocr|repo|release|build'
pattern="^($types)(\\(($scopes)\\))?!?: [a-z0-9].*\$"

fail() {
    printf 'commit check: %s\n' "$1" >&2
    exit 1
}

check_header() {
    header=$1
    case $header in
        # Local helper commits; they are squashed away before merge.
        'fixup! '* | 'squash! '* | 'amend! '*) return 0 ;;
    esac
    printf '%s\n' "$header" | grep -Eq "$pattern" ||
        fail "\"$header\" must look like: type(scope): summary
  types:  $(printf '%s' "$types" | tr '|' ' ')
  scopes: $(printf '%s' "$scopes" | tr '|' ' ')
  summary starts lowercase"
    [ "${#header}" -le 72 ] || fail "header is ${#header} characters; the limit is 72"
}

check_file() {
    # Git drops comment lines from the message, so ignore them here too.
    msg=$(sed '/^#/d' "$1")
    check_header "$(printf '%s\n' "$msg" | sed -n '1p')"

    author=$(git var GIT_AUTHOR_IDENT | sed 's/ [0-9]* [-+][0-9]*$//')
    printf '%s\n' "$msg" | grep -Fqx "Signed-off-by: $author" ||
        fail "missing \"Signed-off-by: $author\". Commit with: git commit -s"
}

case ${1:-} in
    title) check_header "${2:?missing title}" ;;
    file) check_file "${2:?missing file}" ;;
    *) fail "usage: $0 title <text> | file <path>" ;;
esac
