package name.ulbricht.skat.model;

import static name.ulbricht.skat.TestCards.card;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

class ModelTest {

    @Test
    void deckHas32DistinctCardsWorth120Points() {
        assertEquals(32, Card.all().size());
        assertEquals(32, new HashSet<>(Card.all()).size());
        assertEquals(120, Card.totalPoints(Card.all()));
    }

    @Test
    void seatsFollowClockwise() {
        assertEquals(Seat.FOREHAND, Seat.FOREHAND.after(0));
        assertEquals(Seat.MIDDLEHAND, Seat.FOREHAND.next());
        assertEquals(Seat.FOREHAND, Seat.MIDDLEHAND.after(2));
        assertEquals(Seat.FOREHAND, Seat.REARHAND.next());
    }

    @Test
    void cardsAreCached() {
        assertSame(Card.of(Suit.HEARTS, Rank.ACE), card("hA"));
    }

    @Test
    void seatsRotateClockwise() {
        assertEquals(Seat.MIDDLEHAND, Seat.FOREHAND.next());
        assertEquals(Seat.FOREHAND, Seat.REARHAND.next());
    }

    @Test
    void playerLeftOfDealerIsForehandAndDealerIsRearhand() {
        assertEquals(PlayerId.LEFT, PlayerId.HUMAN.onSeatWhenDealing(Seat.FOREHAND));
        assertEquals(PlayerId.RIGHT, PlayerId.HUMAN.onSeatWhenDealing(Seat.MIDDLEHAND));
        assertEquals(PlayerId.HUMAN, PlayerId.HUMAN.onSeatWhenDealing(Seat.REARHAND));
        assertEquals(PlayerId.HUMAN, PlayerId.RIGHT.onSeatWhenDealing(Seat.FOREHAND));
    }

    @Test
    void suitGameLookup() {
        assertEquals(GameType.LEAVES, GameType.suitGame(Suit.LEAVES));
        assertEquals(11, GameType.LEAVES.baseValue());
        assertTrue(GameType.GRAND.hasTrump());
    }

    @Test
    void validDeclarations() {
        new Declaration(GameType.NULL, false, false, false, true);
        new Declaration(GameType.GRAND, true, true, true, true);
        new Declaration(GameType.ACORNS, true, true, false, false);
    }

    @Test
    void invalidDeclarationsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Declaration(GameType.LEAVES, false, true, false, false));
        assertThrows(IllegalArgumentException.class,
                () -> new Declaration(GameType.LEAVES, true, false, true, false));
        assertThrows(IllegalArgumentException.class,
                () -> new Declaration(GameType.GRAND, true, false, false, true));
        assertThrows(IllegalArgumentException.class,
                () -> new Declaration(GameType.NULL, true, true, false, false));
    }
}
