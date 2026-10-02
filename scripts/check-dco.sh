#!/bin/sh
# Checks that every commit in a range has a DCO sign-off that matches its author.
#
#   check-dco.sh <base-sha> <head-sha>
#
# Dependabot signs off with a different address than it commits with. Its commits pass with any
# sign-off, but only when PR_AUTHOR says Dependabot opened the PR: nobody can open a PR as a bot.
set -eu

base=${1:?missing base sha}
head=${2:?missing head sha}
pr_author=${PR_AUTHOR:-}
bad=0

# Merge commits only join existing work, so they are skipped.
for sha in $(git rev-list --no-merges "$base..$head"); do
    author=$(git log -1 --format='%an <%ae>' "$sha")
    msg=$(git log -1 --format='%B' "$sha")

    if printf '%s\n' "$msg" | grep -Fqx "Signed-off-by: $author"; then
        continue
    fi
    if [ "$pr_author" = "dependabot[bot]" ] &&
        [ "$(git log -1 --format='%an' "$sha")" = "dependabot[bot]" ] &&
        printf '%s\n' "$msg" | grep -q '^Signed-off-by: '; then
        continue
    fi

    echo "::error::$(git log -1 --format='%h %s' "$sha"): missing \"Signed-off-by: $author\""
    bad=1
done

if [ "$bad" -ne 0 ]; then
    echo "Sign off every commit: git rebase --signoff origin/main && git push --force-with-lease" >&2
    exit 1
fi
echo "Every commit is signed off."
