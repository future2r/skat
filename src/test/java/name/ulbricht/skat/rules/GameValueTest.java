package name.ulbricht.skat.rules;

import static name.ulbricht.skat.TestCards.cards;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;

class GameValueTest {

    @Test
    void matadorsWith() {
        Matadors matadors = Matadors.of(cards("aU hU lA l10 lK hA h9 h7 bO b8 aK b10"), GameType.LEAVES);
        assertTrue(matadors.isWith());
        assertEquals(1, matadors.count());
    }

    @Test
    void matadorsWithout() {
        Matadors matadors = Matadors.of(cards("hU lA l10 lK hA h9 h7 bO b8 aK b10 a9"), GameType.LEAVES);
        assertFalse(matadors.isWith());
        assertEquals(2, matadors.count());
    }

    @Test
    void matadorsInGrandCountOnlyUnter() {
        assertEquals(4, Matadors.of(cards("aU lU hU bU lA l10 lK hA h9 h7 bO b8"), GameType.GRAND).count());
        Matadors without = Matadors.of(cards("aA a10 lA l10 lK hA h9 h7 bO b8 aK b10"), GameType.GRAND);
        assertFalse(without.isWith());
        assertEquals(4, without.count());
    }

    @Test
    void matadorsWithAllElevenTrumps() {
        assertEquals(11, Matadors.of(cards("aU lU hU bU lA l10 lK lO l9 l8 l7 hA"), GameType.LEAVES).count());
    }

    @Test
    void nullHasNoMatadors() {
        assertThrows(IllegalArgumentException.class, () -> Matadors.of(cards("aU"), GameType.NULL));
    }

    @Test
    void suitGameWithOne() {
        assertEquals(22, GameValue.guaranteed(Declaration.of(GameType.LEAVES), 1));
    }

    @Test
    void grandOuvertWithFourIsTheHighestGame() {
        Declaration grandOuvert = new Declaration(GameType.GRAND, true, true, true, true);
        assertEquals(264, GameValue.guaranteed(grandOuvert, 4));
    }

    @Test
    void announcedSchneiderCountsTwice() {
        Declaration declaration = new Declaration(GameType.ACORNS, true, true, false, false);
        assertEquals(72, GameValue.guaranteed(declaration, 2));
    }

    @Test
    void schwarzIncludesSchneider() {
        assertEquals(44, GameValue.of(Declaration.of(GameType.LEAVES), 1, false, true));
    }

    @Test
    void nullValues() {
        assertEquals(23, GameValue.guaranteed(Declaration.of(GameType.NULL), 0));
        assertEquals(35, GameValue.guaranteed(Declaration.hand(GameType.NULL), 0));
        assertEquals(46, GameValue.guaranteed(new Declaration(GameType.NULL, false, false, false, true), 0));
        assertEquals(59, GameValue.guaranteed(new Declaration(GameType.NULL, true, false, false, true), 0));
    }

    @Test
    void overbidValueIsNextMultipleOfBase() {
        assertEquals(44, GameValue.overbidValue(GameType.LEAVES, 44));
        assertEquals(44, GameValue.overbidValue(GameType.LEAVES, 40));
        assertEquals(72, GameValue.overbidValue(GameType.GRAND, 50));
    }

    @Test
    void bidValuesStartWithTheKnownSequence() {
        assertEquals(Arrays.asList(18, 20, 22, 23, 24, 27, 30, 33, 35, 36, 40, 44, 45, 46, 48, 50, 54, 55,
                59, 60), BidValues.all().subList(0, 20));
        assertEquals(264, (int) BidValues.all().get(BidValues.all().size() - 1));
        assertEquals(18, BidValues.next(0));
        assertEquals(20, BidValues.next(18));
        assertEquals(-1, BidValues.next(264));
    }
}
