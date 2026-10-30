# Skat – Decisions

As of 2026-10-29. German words in quotes are texts of the user interface, which is German only.

## Technology
| Topic | Decision |
|---|---|
| Target system | Windows 7 (32 and 64 bit) |
| Language/framework | Java 11 (LTS) + JavaFX 11 (OpenJFX), built with Maven |
| Runtime architecture | 32-bit Java runtime (runs on 32- and 64-bit Windows) |
| Delivery | ZIP with the folder `Skat`: `Skat.exe`, a bundled 32-bit Java runtime (Liberica JDK 11.0.20.1 with JavaFX, reduced with jlink) and the German player instructions `Liesmich.txt`; no installation. A second double click brings the open window to the front instead of starting Skat again |
| Screen | Designed for at least 1024×768, scaled up; in wider windows (up to 16:9) the top bar, the hint bar and the distance to Ludwig and Richard grow, the middle of the table stays the same |
| Language | User interface and player instructions German only; code, code documentation and all other project documentation English |

## Rules (ISkO, the official German Skat rules)
| Topic | Decision |
|---|---|
| Card deck | Three decks to choose from in the settings (see below); the default is the German-suited Altenburg deck (acorns, leaves, hearts, bells – "Eichel, Grün, Rot, Schellen"; ace, 10, king, Ober, Unter, 9, 8, 7) |
| All players pass | Deal again ("eingepasst") |
| Kontra/Re | No |
| Scoring | No score sheet; the end of a game only shows the result of that game, nothing is stored |
| Game types | All official ones; hand is asked before taking up the skat, announcing Schneider/Schwarz and Ouvert are behind "Weitere Ansagen" |

## Handling and help
| Topic | Decision |
|---|---|
| Playable cards | Always highlighted; the others are dimmed and cannot be clicked |
| Last trick | Can always be looked at |
| Tip button ("Tipp") | Yes (bidding, skat, discarding, announcement, card): names the move with a short reason and marks it (the button or card glows; when discarding, the cards are already selected); nothing is played automatically |
| "Warum?" | Opens the rules at the chapter that fits the situation (bidding, skat and discarding, game value, following suit and trumping, winning) |
| Rules ("Spielregeln") | In the menu; eight short chapters, shown as big buttons on the left |
| All tricks ("Alle Stiche ansehen") | At the end of a game: all tricks with their winner, and the skat |
| Bidding help ("Reiz-Hilfe") | Yes, shows the reachable bid; if a higher value only holds in a hand game, it names both values ("… bis 22. Wenn du Hand spielst …, reichen sie bis 33.") |
| Taking back a card | Yes, the own card, but only as long as nobody has played after it; the next computer player waits 2.5 seconds for that |
| Teams | Coloured label next to the name ("spielt allein" red, "dein Partner" blue, "dein Gegner" grey) and the top bar line "Du und Ludwig gegen Richard" |
| Top bar | Game name, who plays with whom, trick, bid, points, "Ton an/aus", "Menü". If there is not enough room, the bid is left out first, then the trick counter; texts are never cut short |
| Counting points | Yes, running display |
| Computer strength | Chosen in the menu under "Einstellungen …": "Gemütlich" (casual) / "Normal" (default) / "Stark" (strong); applies at once and is stored. Casual now and then plays a careless card, sometimes stops bidding early and never plays hand; Normal remembers the played cards; Strong plays out many possible distributions before every card. From Normal on, the computer players play hand with very strong cards, and Null ouvert hand with a safe Null. The tip always follows the rules of Normal |
| Collecting a trick | Automatically: a complete trick stays on the table for 2 seconds (2.5 seconds if the own card may still be taken back), then slides to its winner; there is no "Weiter" button, the last trick can be looked at any time. The computer players lay their cards slowly one after another |
| Bidding | A single plain question with "Ja, halte ich" / "Passe" (or "22 sagen") |
| Announcement | Big buttons with symbol and game value, a note whether the bid is reached; "Weitere Ansagen …" |
| Sound | Quiet sounds computed by the program: laying a card, collecting a trick, "your turn", a short tune for won or lost. Switch "Ton an/aus" at the top right and in the settings; the choice is stored |

## Language and names
| Topic | Decision |
|---|---|
| Computer players | Ludwig (left) and Richard (right) |
| Addressing the player | "du" (informal) |
| Highest card | "Ass" (corner index A), not "Daus" |
| Jack | "Unter" (corner index U) |
| Suits | "Eichel, Grün, Rot, Schellen" |
| Following suit | Always "zugeben", never "bedienen" |
| All terms | see `terms.md` |

## GUI
A combination of the drafts: the **learning mode with a hint bar (5)** as the basis, plus the **large, non-overlapping cards of the large-print draft (2)** and the **hand grouped by trump and suits (7)**.

Look (visual draft "felt table with wooden frame" plus the faces of the "tavern" draft, same layout): green felt that is lighter in the middle as if lit from above, a wooden frame around the table, the top bar in dark wood, the hint bar as cream paper below a wooden edge, soft shadows under the cards, the cards in the trick lying slightly askew as if just put down, a dark red card back for the drawn deck, and drawn faces for the computer players: Ludwig with a flat cap and a moustache, Richard with grey hair and glasses.

## Card deck
The player chooses the deck in the settings ("Mit welchen Karten möchtest du spielen?"): three big buttons, each with two sample cards (Ober/queen and ace) and the name; the chosen deck has a green frame. The choice applies at once and is stored with the other settings.

| Deck | Description |
|---|---|
| "Altenburger Blatt" (default) | Scans of a German-suited Altenburg deck with its own back, from `decks/altenburg` |
| "Französisches Blatt" | French-suited deck from `decks/french`. All texts then use the French names: "Kreuz, Pik, Herz, Karo" for the suits, "Bube" and "Dame" for Unter and Ober ("Du musst Pik zugeben", "Kreuz-Spiel", "Trumpf: Buben und Herz"), the announcement buttons show ♣ ♠ ♥ ♦, and the rules chapters use the same names |
| "Klares Blatt" | The own deck drawn by the program, described below |

The images are shown as they are, only reduced; the frames for playable (yellow), selected (blue) and tip (green) lie on top as with the drawn deck.

**"Klares Blatt": an own German-suited deck drawn by the program**: large corner indices top left and bottom right, clear suit symbols, simplified double-headed figures for king, Ober and Unter, the ace with two large symbols. It is drawn with JavaFX shapes; there are no image files.
