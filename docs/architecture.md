# Skat – Architecture

Desktop Skat for Windows 7: one human player against two computer players,
German-suited deck, official German rules (ISkO). UI text is German only;
code and code documentation are English. Product decisions are recorded in
`decisions.md`.

## Technology

| Topic | Choice |
|---|---|
| Language level | Java 11 (`--release 11`) |
| UI | JavaFX 11 (OpenJFX 11.0.2 from Maven Central for building and testing), no FXML, layouts built in code |
| Build | Maven via Maven Wrapper (`mvnw`), so no Maven installation is needed |
| Tests | JUnit 5 |
| Quality gates | `mvnw verify`: javac `-Xlint:all -Werror`, Checkstyle (also test code), SpotBugs (effort max, threshold medium) |
| Runtime for delivery | Liberica JDK 11.0.20.1 Full, Windows x86 (32-bit, includes JavaFX; the last update whose notes list Windows 7), reduced with `jlink` to java.base, java.desktop, javafx.controls, jdk.unsupported, jdk.localedata (de, en): about 47 MB |
| Launcher | `Skat.exe` via launch4j-maven-plugin (Launch4j 3.50), GUI header, uses the bundled `runtime` folder only, one instance at a time, icon `src/packaging/skat.ico` (written by `IconFileTool` from `AppIcon`) |
| Delivery | `mvnw.cmd -P windows package` → `target/Skat-<version>.zip` (about 48 MB, 7 MB of it card images) with `Skat/Skat.exe`, `Skat/lib/skat.jar`, `Skat/runtime/`, `Skat/Liesmich.txt`. The JDK is expected in `.tools/jdk-x86/jdk-11.0.20.1-full` (not in Git) or given with `-Djdk.x86=...` |
| Persistence | None (no score sheet); settings in `%APPDATA%\Skat\settings.properties` |
| Sound | Synthesized in code, played with `javax.sound.sampled` (module `java.desktop`, needed in the jlink image) |

## Package structure

```
name.ulbricht.skat
│                Launcher (plain main class, so JavaFX may sit on the class
│                path), SkatApp (the JavaFX Application)
├── model        Immutable domain types
│                Suit, Rank, Card (cached instances), Seat (FOREHAND/MIDDLEHAND/
│                REARHAND, next/after), PlayerId (HUMAN, LEFT "Ludwig",
│                RIGHT "Richard"; seats rotate with the dealer), GameType (suit
│                games, Grand, Null), Declaration (type, hand, Schneider/Schwarz
│                announced, ouvert; validates the official combinations), Deal
├── rules        Pure functions over the model, fully unit-tested
│                CardOrder (trump membership and order per game type),
│                TrickRules (playable cards incl. Unter as trump, trick winner),
│                BidValues (18, 20, 22, …), Matadors ("mit/ohne"), GameValue
│                (multipliers, Null values, overbid value), GameResult (won,
│                Schneider, Schwarz, overbid), Dealer (shuffle, deal 3-skat-4-3)
├── engine       Game flow without UI dependencies
│                Game: passive state machine, phases BIDDING → SKAT_DECISION →
│                (DISCARD →) DECLARATION → PLAYING ⇄ TRICK_COMPLETE → FINISHED,
│                or PASSED_IN; every step is an action of the acting seat,
│                invalid actions throw; take back the own card while nobody has
│                played after it. Bidding (Reizen state machine), Session
│                (games in a row, rotating dealer, abandon a running game),
│                PlayerView (what one seat may know), CompletedTrick
├── ai           Computer players and helpers
│                ComputerPlayer (interface), ComputerMove (applies one decision),
│                HandEvaluator (best game type, bid value with the skat and
│                with a hand game, safe Null ouvert; strength of a suit game:
│                1 per trump, +0.25 for the trump ace and ten, 1 per side
│                ace, 0.5 per side ten with its ace, 0.5 for the Unter of
│                acorns; a suit game from 6.5, a hand game from 9),
│                CardMemory (unseen cards, voids)
│                Strength: CASUAL "Gemütlich" (CasualPlayer: rules of thumb
│                plus careless cards), NORMAL (SimplePlayer: rules of thumb
│                with card counting), STRONG "Stark" (StrongPlayer: Sampler
│                deals the unseen cards consistently with the play so far,
│                Playout plays each candidate card to the end, best average
│                wins; bidding as Normal). StrengthDuel (test sources)
│                compares the levels on the same deals.
│                Every decision of SimplePlayer is an Advice (move + reason);
│                it powers the "Tipp" button and the bidding help
├── sound        Sound (the five sounds), SoundSynth (computes them as PCM),
│                SoundPlayer (plays on a background thread; silent if there is
│                no sound device)
└── ui           JavaFX, no FXML, layouts built in code
                 MainWindow (window, icon, scene), TableScreen (the 738 pixels
                 high table: top bar, opponents, trick, panels, hand, hint bar,
                 windows in front of the darkened table, animations),
                 GameController (renders the game state, reacts to clicks,
                 paces the computer players, tips, sounds, all panels and
                 windows), CardDeck (CLEAR "Klares Blatt" drawn with shapes,
                 ALTENBURG and FRENCH from images), CardView (one card or back
                 of the chosen deck; images decoded at twice the shown size and
                 cached for the current deck only) + SuitSymbol (German or
                 French suit symbols), Portrait (drawn faces of Ludwig and Richard), AppIcon, HandGrouping (trump / suits),
                 Describe + Texts (German sentences from the resource bundle;
                 with the French deck "french.<key>" replaces "<key>", so the
                 texts say Kreuz, Pik, Herz, Karo, Bube, Dame), Announcements
                 ("Weitere Ansagen"), RulesView + RuleTopic ("Spielregeln",
                 also opened by "Warum?"), Settings (strength, deck and sound
                 in the user profile)

Resources: name/ulbricht/skat/i18n/messages.properties (all German UI texts),
name/ulbricht/skat/ui/skat.css (the look of the table), name/ulbricht/skat/ui/decks/<altenburg|french>/
(<suit>-<rank>.png for all 32 cards and back.png, 2:3 images; the French
suits stand for the German ones: clubs = acorns, spades = leaves, hearts =
hearts, diamonds = bells, jack = Unter, queen = Ober).
```

Dependencies point downwards only: `ui → ai → engine → rules → model`, and
`ui → sound`. Everything except `ui` runs without JavaFX, so the rules, the
engine and the computer players are testable and simulate whole games headless.

Tools in the test sources (run by hand, not unit tests): `ScreenshotTool`
(renders every phase to PNG without a window), `StrengthDuel` (compares the
strengths), `IconFileTool` (writes `src/packaging/skat.ico`).

## Threading

The engine and the computer players run on the JavaFX thread; a
`PauseTransition` lets each computer player act after a short pause. Even
"Stark" needs only milliseconds per card (at most 0.5 s by a time budget), so
no background task is needed. Only the sound plays on its own thread. A
finished trick stays on the table for 2 seconds (2.5 if the own card may still
be taken back) and is then collected; while it slides to its winner the table
takes no clicks, and the animation checks that the same game is still running.

## Screen

Designed for 1024×768 as the minimum. The table is laid out 738 pixels high
and scaled to the window height, so it scales up on larger screens. Its
middle part (trick, skat, panels, own hand) is always 1024 pixels wide; in a
wider window (up to 16:9) the top bar and hint bar grow and the computer
players move to the edges. A narrower window shows the 1024 pixels in full.
The window starts maximized.

## Milestones

1. Project skeleton (Maven Wrapper, JavaFX hello window) and the complete rule
   core with unit tests
2. Engine with simple computer players; a full game playable in a window
3. UI according to the chosen design (hint bar, large grouped cards) and the
   card renderer for the own deck
4. Helpers: Tipp, bidding help, "Warum?" with the rules, last trick, all tricks,
   take back, sound
5. Computer strength levels Gemütlich / Normal / Stark, settings window;
   computer players announce hand games and Null ouvert
6. Windows 7 package: 32-bit runtime via jlink, `Skat.exe`, ZIP (Maven profile `windows`)
