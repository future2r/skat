# Skat

Desktop Skat (JavaFX) for Windows 7: one player against the computer players Ludwig and Richard.

- Product decisions: `docs/decisions.md`; UI terms: `docs/terms.md`; structure and milestones:
  `docs/architecture.md`. Keep these three up to date with every change. `docs/main-window-draft.html`
  (UI draft) records the planning; the program wins.
- Languages: the user interface and the player instructions (`src/packaging/readme-player.txt`,
  shipped as `Liesmich.txt`) are German; code, identifiers, file names, Javadoc and all other
  documentation are English. German words appear in code only as quoted UI texts or Skat terms.
- The UI addresses the player with "du". All player-visible texts go into `src/main/resources/name/ulbricht/skat/i18n/messages.properties`, never into code.
- Always say "zugeben", never "bedienen". The top card is "Ass", the jacks are "Unter".
- Java 11 language level (`--release 11`) and JavaFX 11; do not use newer APIs (no records, no switch
  expressions, no `var` in lambdas beyond Java 11).
- Build with the Maven Wrapper: `./mvnw verify` (or `mvnw.cmd verify`).

## Looking at the UI

Never start the application (it opens a window on the user's working PC) without asking first.
To check the UI, render screenshots off-screen instead; no window opens:

```
mvnw.cmd -q test-compile dependency:build-classpath -Dmdep.outputFile=target\cp.txt
java -Djava.awt.headless=true -cp "target\classes;target\test-classes;<content of target\cp.txt>" name.ulbricht.skat.ui.ScreenshotTool 3
```

The PNGs land in `target/screenshots` (one per phase of a game). With the arguments `5 partner` the
human passes and a computer plays alone; these shots use a 16:9 window to check the wide layout.
With `1 ouvert` it searches for a deal where a computer player announces Null ouvert (open cards).
The deck is the Altenburg deck unless `-Ddeck=CLEAR` or `-Ddeck=FRENCH` is given to `java`.

To compare the computer strengths, run `name.ulbricht.skat.ai.StrengthDuel 300` with the same class path
(prints how often the declarer wins for each pairing of strengths).

## Windows package

`mvnw.cmd -P windows package` builds `target/dist/Skat` (Skat.exe, lib, runtime, Liesmich.txt) and
`target/Skat-<version>.zip`. It needs the 32-bit Liberica JDK 11.0.20.1 Full (with JavaFX) in
`.tools/jdk-x86/jdk-11.0.20.1-full` (ignored by Git) or `-Djdk.x86=<folder>`. Starting Skat.exe opens
a window, so ask first; to check the runtime without a window, run the ScreenshotTool with
`target\dist\Skat\runtime\bin\java.exe` and `-cp target\dist\Skat\lib\skat.jar;target\test-classes`.
After changing `AppIcon`, rewrite `src/packaging/skat.ico` with `name.ulbricht.skat.ui.IconFileTool`.

## Before every commit

Run `./mvnw verify` and commit only if it passes. It enforces:
- compiler warnings as errors (`-Xlint:all -Werror`),
- Checkstyle (`config/checkstyle/checkstyle.xml`),
- SpotBugs (`config/spotbugs/exclude.xml`, keep exclusions rare and justified),
- all unit tests.

Then review the staged diff for leftovers (debug output, commented-out code, German text in code,
missing tests for rule changes).
