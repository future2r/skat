package name.ulbricht.skat.rules;

import static name.ulbricht.skat.TestCards.card;
import static name.ulbricht.skat.TestCards.cards;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.GameType;

class TrickRulesTest {

    private static final List<Card> HAND = cards("aU hU lA l10 lK hA h9 h7 bO b8");

    @Test
    void trumpMembership() {
        assertTrue(CardOrder.isTrump(card("hU"), GameType.LEAVES));
        assertTrue(CardOrder.isTrump(card("l7"), GameType.LEAVES));
        assertFalse(CardOrder.isTrump(card("hA"), GameType.LEAVES));
        assertTrue(CardOrder.isTrump(card("bU"), GameType.GRAND));
        assertFalse(CardOrder.isTrump(card("lA"), GameType.GRAND));
        assertFalse(CardOrder.isTrump(card("aU"), GameType.NULL));
    }

    @Test
    void trumpsDescending() {
        List<Card> trumps = CardOrder.trumpsDescending(GameType.LEAVES);
        assertEquals(cards("aU lU hU bU lA l10 lK lO l9 l8 l7"), trumps);
        assertEquals(cards("aU lU hU bU"), CardOrder.trumpsDescending(GameType.GRAND));
        assertTrue(CardOrder.trumpsDescending(GameType.NULL).isEmpty());
    }

    @Test
    void mustZugebenLedSuitButUnterIsTrump() {
        assertEquals(cards("hA h9 h7"), TrickRules.playableCards(HAND, cards("hK h8"), GameType.LEAVES));
    }

    @Test
    void trumpLeadRequiresTrumpIncludingUnter() {
        assertEquals(cards("aU hU lA l10 lK"), TrickRules.playableCards(HAND, cards("lO"), GameType.LEAVES));
    }

    @Test
    void unterLeadInGrandRequiresUnter() {
        assertEquals(cards("aU hU"), TrickRules.playableCards(HAND, cards("bU"), GameType.GRAND));
    }

    @Test
    void unterIsPlainCardInNull() {
        assertEquals(cards("hU hA h9 h7"), TrickRules.playableCards(HAND, cards("hK"), GameType.NULL));
    }

    @Test
    void anyCardWhenSuitIsMissing() {
        List<Card> hand = cards("aU lA bO b8");
        assertEquals(hand, TrickRules.playableCards(hand, cards("hK"), GameType.LEAVES));
    }

    @Test
    void anyCardWhenLeading() {
        assertEquals(HAND, TrickRules.playableCards(HAND, Collections.emptyList(), GameType.LEAVES));
    }

    @Test
    void highestCardOfLedSuitWins() {
        assertEquals(2, TrickRules.winnerIndex(cards("hK h8 hA"), GameType.LEAVES));
        assertEquals(1, TrickRules.winnerIndex(cards("h10 hA h7"), GameType.GRAND));
    }

    @Test
    void offSuitCardDoesNotWin() {
        assertEquals(0, TrickRules.winnerIndex(cards("h9 bA aA"), GameType.LEAVES));
    }

    @Test
    void trumpBeatsPlainSuit() {
        assertEquals(2, TrickRules.winnerIndex(cards("hA h10 l7"), GameType.LEAVES));
        assertEquals(1, TrickRules.winnerIndex(cards("lA bU l10"), GameType.GRAND));
    }

    @Test
    void unterRankByColourAndAboveTrumpAce() {
        assertEquals(1, TrickRules.winnerIndex(cards("bU aU lU"), GameType.LEAVES));
        assertEquals(1, TrickRules.winnerIndex(cards("lA bU"), GameType.LEAVES));
    }

    @Test
    void nullOrderPutsUnterBetweenOberAndTen() {
        assertEquals(1, TrickRules.winnerIndex(cards("h10 hU h9"), GameType.NULL));
        assertEquals(0, TrickRules.winnerIndex(cards("hO hU h10"), GameType.NULL));
    }
}
