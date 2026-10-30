# Terms in the program

The German terms the player sees, with their meaning. The player is addressed with "du". The exact
wording of every text is in `src/main/resources/name/ulbricht/skat/i18n/messages.properties`; this list is the
reference for choosing words.

## People and seats
| Term (German) | Meaning |
|---|---|
| Du | the human player |
| Ludwig | computer player (left) |
| Richard | computer player (right) |
| Geber | dealer (with three players always rearhand) |
| Vorhand, Mittelhand, Hinterhand | forehand, middlehand, rearhand: the seats in bidding and playing order |
| Alleinspieler | declarer: won the bidding and plays alone |
| Gegenspieler | the two opponents of the declarer |

## Cards
| Term (German) | Corner index | Note |
|---|---|---|
| Eichel, Grün, Rot, Schellen | symbol | acorns, leaves, hearts, bells; ranked in this order |
| Ass | A | ace, 11 points |
| Zehn | 10 | ten, 10 points |
| König | K | king, 4 points |
| Ober | O | 3 points |
| Unter | U | 2 points, always trump (except in Null) |
| Neun, Acht, Sieben | 9, 8, 7 | nine, eight, seven, 0 points |
| card name | – | suit + rank, e.g. "Eichel-Unter", "Rot-Ass", "Schellen-Zehn" |
| Trumpf | – | trump, e.g. the group label "Trumpf: Unter und Grün" |
| Augen | – | card points, 120 in total |

## Course of a game
| Term (German) | Where / meaning |
|---|---|
| Karten geben | dealing, start of a game |
| Reizen | bidding |
| sagen / hören (halten) | to say a value / to hold it: "Bisher gereizt: 20. Sagst du 22?" / "Ludwig sagt 18. Hältst du 18?" |
| Passe | pass, button during the bidding |
| Skat | the two face-down cards in the middle |
| Skat aufnehmen / Hand spielen | take up the skat / play hand, asked after winning the bidding |
| Drücken | discard: put two cards into the skat |
| Spiel ansagen | announce the game |
| Ausspielen, Zugeben, Stechen, Abwerfen | lead, follow suit, trump, discard while playing; always "zugeben", never "bedienen" |
| Stich | trick, three played cards: "Du bekommst den Stich: …" / "Ludwig bekommt den Stich: …" |
| Gewonnen / Verloren | won / lost, end of the game |
| Eingepasst, neu geben | all passed, deal again |

## Game types and announcements
| Term (German) | Note |
|---|---|
| Eichel-Spiel, Grün-Spiel, Rot-Spiel, Schellen-Spiel | suit games, base values 12, 11, 10, 9 |
| Grand | base value 24, only the Unter are trump |
| Null, Null Hand, Null ouvert, Null ouvert Hand | values 23, 35, 46, 59 |
| Hand | without taking up the skat |
| Schneider, Schwarz | a party with at most 30 points / without a trick |
| Schneider angesagt, Schwarz angesagt, Ouvert | announced Schneider/Schwarz, open cards; under "Weitere Ansagen …" |
| mit 1, ohne 2 … | matadors ("Spitzen"): the top trumps without a gap from the Unter of acorns that one holds ("mit") or lacks ("ohne") |
| Spiel 2 | multiplier: matadors + 1 (+ hand, Schneider …) |
| Rechenweg | calculation, e.g. "mit 1, Spiel 2 × Grün 11 = 22" |
| Überreizt | overbid: game value below the bid, the game is lost |

## Buttons and menu
| Term (German) | Where |
|---|---|
| Ja, halte ich · Passe · 22 sagen | bidding |
| Skat aufnehmen · Hand spielen | after the bidding |
| Diese 2 Karten drücken · Auswahl aufheben | discarding |
| Weitere Ansagen … → Ansagen: Schneider · Schwarz · Ouvert | announcing the game |
| Neues Spiel? · Ja, neu geben · Weiterspielen | question when a game is running |
| Skat beenden? · Ja, beenden · Weiterspielen | question when quitting |
| Letzter Stich · Schließen | window with the last trick |
| Tipp · Warum? · Letzter Stich · Zurücknehmen | hint bar (tip, why, last trick, take back) |
| Nächstes Spiel · Alle Stiche ansehen | end of the game |
| Alle Stiche · "3. Stich für Ludwig" · "Im Skat: 14 Augen" | window with all tricks at the end of the game |
| Ton an · Ton aus · Menü | top right |
| Neues Spiel · Letzten Stich ansehen · Einstellungen … · Spielregeln · Beenden · Zurück zum Spiel | menu |
| Einstellungen · "Wie gut sollen Ludwig und Richard spielen?" · Ton | settings window |
| Spielregeln: Worum geht es? · Karten und Augen · Trumpf · Zugeben und Stechen · Reizen · Skat und Drücken · Spielwert · Gewinnen | chapters of the rules window; "Warum?" opens the fitting one |
| Gemütlich · Normal · Stark | strengths in the settings (casual, normal, strong), each with one sentence |
| Mit welchen Karten möchtest du spielen? · Klares Blatt · Altenburger Blatt · Französisches Blatt | deck choice in the settings |

## Names with the French-suited deck
| German deck | French deck |
|---|---|
| Eichel, Grün, Rot, Schellen | Kreuz, Pik, Herz, Karo |
| Unter (die Unter) | Bube (die Buben), e.g. "Trumpf: Buben und Herz" |
| Ober | Dame (article "die": "Leg die Pik-Dame") |
| Eichel-Spiel … Schellen-Spiel | Kreuz-Spiel, Pik-Spiel, Herz-Spiel, Karo-Spiel |

All other terms (Ass, Zehn, König, Trumpf, Grand, Null, Hand …) stay the same.

## Top bar and displays
| Term (German) | Where |
|---|---|
| Reizen · Geber: Richard · Du bist Vorhand | during the bidding |
| Reizen beendet · Du und Richard gegen Ludwig · Gereizt: 22 | between bidding and play |
| Deine Augen: 14 (Skat) | when announcing after taking up the skat |
| Du spielst allein · Du und Ludwig gegen Richard | who plays with whom |
| spielt allein (red) · dein Partner (blue) · dein Gegner (grey) | label next to the name of a computer player |
| Deine Augen: 14 · Gegner: 0 | points when the player is the declarer |
| Ihr beide: 30 · Ludwig: 41 | points when the player plays with the other computer player against the declarer |
| Bisher gereizt: 20 · Bisher hat niemand gereizt | bidding question |
| Reiz-Hilfe: Deine Karten reichen etwa bis 30 | bidding help |
| Reiz-Hilfe: Deine Karten reichen etwa bis 22. Wenn du Hand spielst (ohne den Skat aufzunehmen), reichen sie bis 33. | bidding help when a higher value only holds in a hand game |

## Example sentences of the hint bar
- Du bist dran. Du musst Rot zugeben. Der Rot-Unter ist Trumpf und zählt nicht als Rot.
- Du bist dran. Du spielst aus und darfst jede Karte legen.
- Du hast kein Rot mehr. Du darfst stechen (Trumpf spielen) oder eine andere Karte abwerfen.
- Mit „22 sagen“ bietest du mehr, mit „Passe“ steigst du aus.
- Ausgewählt: Eichel-König und Schellen-Zehn. Zusammen 14 Augen, die zählen am Ende für dich.
- Wähle dein Spiel. Gereizt wurde bis 22.
- Du bekommst den Stich: König 4 + Acht 0 + Ass 11 = 15 Augen.
- Ludwig hat „Grün-Spiel“ angesagt und spielt allein. Du spielst mit Richard zusammen.
- Ludwig ist dran …
- Das Spiel ist vorbei. Klicke auf „Nächstes Spiel“, dann gibt Ludwig die Karten.

## Example tips
- Tipp: Leg das Rot-Ass. Ein Ass bringt oft gleich 11 Augen.
- Tipp: Leg den Grün-König. Richard bekommt den Stich sicher. Gib ihm möglichst viele Augen mit.
- Tipp: Drück die Rot-Zehn und die Eichel-Zehn. Ihre 20 Augen zählen dann sicher für dich.
- Tipp: Sag „Grün-Spiel“ an. Dafür hast du 8 Trümpfe.
- Tipp: Passe. Deine Karten sind für ein eigenes Spiel zu schwach.
- Tipp: Halte 24 mit „Ja, halte ich“. Deine Karten reichen etwa bis 33, aber nur, wenn du Hand spielst. Nimm dann den Skat nicht auf.
