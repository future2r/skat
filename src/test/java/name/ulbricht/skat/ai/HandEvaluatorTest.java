package name.ulbricht.skat.ai;

import static name.ulbricht.skat.TestCards.cards;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.GameType;

class HandEvaluatorTest {

    /** Six trumps including the trump ace and ten, but no side ace: a game of leaves worth 22. */
    @Test
    void sixTrumpsWithAceAndTenAreEnoughForASuitGame() {
        List<Card> hand = cards("lU bU aK aO lA l10 lO l9 hO b7");
        assertEquals(6.5, HandEvaluator.strength(hand, GameType.LEAVES), 1e-9);
        assertEquals(GameType.LEAVES, HandEvaluator.preferredGame(hand));
        // Without the Unter of acorns: without 1, game 2 x 11.
        assertEquals(22, HandEvaluator.maxBid(hand));
    }

    @Test
    void sixPlainTrumpsAreNotEnough() {
        List<Card> hand = cards("lU bU aK aO lK l8 lO l9 hO b7");
        assertEquals(6.0, HandEvaluator.strength(hand, GameType.LEAVES), 1e-9);
        assertEquals(0, HandEvaluator.maxBid(hand));
    }
}
