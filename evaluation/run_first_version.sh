#!/usr/bin/env bash
# Runs the known-answer scenarios against the first version: the team's final code as restored in
# commit 21e2794, unchanged except that module-info.java is set aside so the tests run on the class
# path, with this version's pom.xml for the same JavaFX plus the test libraries. Needs JDK 21.
set -euo pipefail
here=$(cd "$(dirname "$0")/.." && pwd)
work=$(mktemp -d)
trap 'git -C "$here" worktree remove --force "$work/first" >/dev/null 2>&1 || true; rm -rf "$work"' EXIT
git -C "$here" worktree add --detach "$work/first" 21e2794 >/dev/null 2>&1
cd "$work/first"
rm src/main/java/module-info.java
cp "$here/pom.xml" pom.xml
mkdir -p src/test/java/com/library/management/librarymanagementsystem src/test/resources
cp "$here/evaluation/FirstVersionKnownAnswersTest.java" src/test/java/com/library/management/librarymanagementsystem/
cp src/main/resources/Inventory.csv src/test/resources/Inventory.shipped.csv
./mvnw -B test 2>&1 | grep -E "Tests run:|AssertionFailedError" || true
