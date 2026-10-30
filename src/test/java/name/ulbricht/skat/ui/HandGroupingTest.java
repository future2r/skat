package name.ulbricht.skat.ui;

import static name.ulbricht.skat.TestCards.cards;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.Suit;

class HandGroupingTest {

    private static final List<Card> HAND = cards("h9 aU lK bO hA l10 hU lA h7 b8");

    @Test
    void suitGameShowsTrumpThenPlainSuits() {
        List<HandGrouping.CardGroup> groups = HandGrouping.group(HAND, GameType.LEAVES);
        assertEquals(3, groups.size());
        assertEquals(HandGrouping.Kind.TRUMP, groups.get(0).kind());
        assertEquals(cards("aU hU lA l10 lK"), groups.get(0).cards());
        assertEquals(Suit.HEARTS, groups.get(1).suit());
        assertEquals(cards("hA h9 h7"), groups.get(1).cards());
        assertEquals(cards("bO b8"), groups.get(2).cards());
    }

    @Test
    void beforeTheAnnouncementUnterFormTheirOwnGroup() {
        List<HandGrouping.CardGroup> groups = HandGrouping.group(HAND, null);
        assertEquals(HandGrouping.Kind.UNTER, groups.get(0).kind());
        assertEquals(cards("aU hU"), groups.get(0).cards());
        assertEquals(cards("lA l10 lK"), groups.get(1).cards());
    }

    @Test
    void nullHasNoTrumpAndUnterBetweenOberAndTen() {
        List<HandGrouping.CardGroup> groups = HandGrouping.group(cards("h10 hU hO aU"), GameType.NULL);
        assertEquals(2, groups.size());
        assertEquals(cards("aU"), groups.get(0).cards());
        assertEquals(cards("hO hU h10"), groups.get(1).cards());
    }
}
