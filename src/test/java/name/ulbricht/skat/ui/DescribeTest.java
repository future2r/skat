package name.ulbricht.skat.ui;

import static name.ulbricht.skat.TestCards.card;
import static name.ulbricht.skat.TestCards.cards;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.ai.Advice;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.Suit;
import name.ulbricht.skat.rules.GameResult;

class DescribeTest {

    private final Texts texts = new Texts();
    private final Describe describe = new Describe(texts);

    @Test
    void calculationOfASuitGame() {
        GameResult result = GameResult.evaluate(Declaration.of(GameType.LEAVES), 22,
                cards("aU hU lA l10 lK hA h9 h7 bO b8 aK b10"), 78, 6);
        assertEquals("mit 1, Spiel 2 × Grün 11 = 22", describe.calculation(result));
    }

    @Test
    void calculationWithoutAndHandAndSchneider() {
        GameResult result = GameResult.evaluate(Declaration.hand(GameType.GRAND), 18,
                cards("aA a10 lA l10 lK hA h9 h7 bO b8 aK b10"), 95, 8);
        assertEquals("ohne 4, Spiel 5, Hand 6, Schneider 7 × Grand 24 = 168", describe.calculation(result));
    }

    @Test
    void calculationOfNull() {
        GameResult result = GameResult.evaluate(Declaration.hand(GameType.NULL), 18, cards("a7"), 0, 0);
        assertEquals("Null Hand = 35", describe.calculation(result));
    }

    @Test
    void tipReasonsNameSuitPartnerAndPoints() {
        assertEquals("Dann hast du kein Schellen mehr und kannst später stechen.",
                describe.tipReason(Advice.discard(cards("bK b7"), Advice.Reason.DISCARD_VOID, 4, Suit.BELLS), ""));
        assertEquals("Richard bekommt den Stich sicher. Gib ihm möglichst viele Augen mit.",
                describe.tipReason(Advice.play(card("a9"), Advice.Reason.SMEAR), "Richard"));
        assertEquals("Diese beiden Karten brauchst du am wenigsten.",
                describe.tipReason(Advice.discard(cards("b7 a7"), Advice.Reason.DISCARD_POINTS, 0, null), ""));
        assertEquals("Ihre 14 Augen zählen dann sicher für dich.",
                describe.tipReason(Advice.discard(cards("bK b10"), Advice.Reason.DISCARD_POINTS, 14, null), ""));
    }

    @Test
    void cardWithArticle() {
        assertEquals("das Rot-Ass", texts.cardWithArticle(card("hA")));
        assertEquals("den Grün-König", texts.cardWithArticle(card("lK")));
        assertEquals("die Schellen-Zehn", texts.cardWithArticle(card("b10")));
    }

    @Test
    void trickSum() {
        assertEquals("König 4 + Acht 0 + Ass 11 = 15 Augen", describe.trickSum(cards("hK h8 hA")));
    }

    @Test
    void hintNamesTheUnterThatCountsAsTrump() {
        assertEquals("Du musst Rot zugeben. Der Rot-Unter ist Trumpf und zählt nicht als Rot.",
                describe.playHint(cards("aU hU lA hA h9"), cards("hK h8"), GameType.LEAVES));
    }

    @Test
    void hintWithoutTheLedSuit() {
        assertEquals("Du hast kein Rot mehr. Du darfst stechen (Trumpf spielen) oder eine andere Karte abwerfen.",
                describe.playHint(cards("aU lA b9"), cards("hK"), GameType.LEAVES));
        assertEquals("Du hast keinen Trumpf mehr und darfst jede Karte legen.",
                describe.playHint(cards("hA b9"), cards("lK"), GameType.LEAVES));
    }

    @Test
    void groupLabelsAndGameNames() {
        assertEquals("Grün-Spiel Hand", describe.gameName(Declaration.hand(GameType.LEAVES)));
        assertEquals("Grand Hand Schneider angesagt",
                describe.gameName(new Declaration(GameType.GRAND, true, true, false, false)));
        assertEquals("Grand ouvert", describe.gameName(new Declaration(GameType.GRAND, true, true, true, true)));
        assertEquals("Null ouvert Hand", describe.gameName(new Declaration(GameType.NULL, true, false, false, true)));
        HandGrouping.CardGroup trump = HandGrouping.group(cards("aU l7"), GameType.LEAVES).get(0);
        assertEquals("Trumpf: Unter und Grün", describe.groupLabel(trump, GameType.LEAVES));
    }
}
