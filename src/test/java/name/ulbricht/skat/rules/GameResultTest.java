package name.ulbricht.skat.rules;

import static name.ulbricht.skat.TestCards.cards;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;

class GameResultTest {

    /** Leaves game "with 1": has the Unter of acorns but not the Unter of leaves. */
    private static final List<Card> WITH_ONE = cards("aU hU lA l10 lK hA h9 h7 bO b8 aK b10");
    private static final Declaration LEAVES = Declaration.of(GameType.LEAVES);

    @Test
    void wonWithSixtyOne() {
        GameResult result = GameResult.evaluate(LEAVES, 22, WITH_ONE, 61, 5);
        assertTrue(result.isWon());
        assertEquals(22, result.value());
    }

    @Test
    void lostWithSixty() {
        GameResult result = GameResult.evaluate(LEAVES, 22, WITH_ONE, 60, 5);
        assertFalse(result.isWon());
        assertEquals(22, result.value());
    }

    @Test
    void schneiderRaisesTheValue() {
        GameResult result = GameResult.evaluate(LEAVES, 22, WITH_ONE, 92, 8);
        assertTrue(result.isWon());
        assertTrue(result.isSchneider());
        assertEquals(33, result.value());
    }

    @Test
    void declarerBeingSchneiderAlsoRaisesTheValue() {
        GameResult result = GameResult.evaluate(LEAVES, 22, WITH_ONE, 25, 2);
        assertFalse(result.isWon());
        assertEquals(33, result.value());
    }

    @Test
    void schwarzCountsSchneiderAndSchwarz() {
        GameResult result = GameResult.evaluate(LEAVES, 22, WITH_ONE, 120, 10);
        assertTrue(result.isSchwarz());
        assertEquals(44, result.value());
    }

    @Test
    void handGameOverbidBecauseSkatChangedTheMatadors() {
        // Bid 44 expecting "without 2, hand" (4 x 11); the skat brought the Unter of leaves: only "without 1".
        List<Card> declarerCards = cards("lU lA l10 lK lO l9 hA h10 bA b10 hK bK");
        GameResult result = GameResult.evaluate(Declaration.hand(GameType.LEAVES), 44, declarerCards, 80, 7);
        assertTrue(result.isOverbid());
        assertFalse(result.isWon());
        assertEquals(44, result.value());
    }

    @Test
    void announcedSchneiderMissed() {
        Declaration declaration = new Declaration(GameType.LEAVES, true, true, false, false);
        GameResult result = GameResult.evaluate(declaration, 55, WITH_ONE, 85, 7);
        assertFalse(result.isWon());
        assertEquals(55, result.value());
    }

    @Test
    void nullWonWithoutTrick() {
        GameResult result = GameResult.evaluate(Declaration.of(GameType.NULL), 23, WITH_ONE, 0, 0);
        assertTrue(result.isWon());
        assertEquals(23, result.value());
    }

    @Test
    void nullLostWithOneTrick() {
        GameResult result = GameResult.evaluate(Declaration.of(GameType.NULL), 23, WITH_ONE, 4, 1);
        assertFalse(result.isWon());
        assertEquals(23, result.value());
    }

    @Test
    void nullBelowBidIsOverbid() {
        GameResult result = GameResult.evaluate(Declaration.of(GameType.NULL), 24, WITH_ONE, 0, 0);
        assertTrue(result.isOverbid());
        assertFalse(result.isWon());
    }
}
