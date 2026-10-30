package name.ulbricht.skat.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Deal;
import name.ulbricht.skat.model.Seat;

class DealerTest {

    @Test
    void dealsTenCardsEachAndTwoIntoTheSkat() {
        Deal deal = Dealer.deal(Dealer.shuffledDeck(new Random(42)));
        Set<Card> seen = new HashSet<>(deal.skat());
        for (Seat seat : Seat.values()) {
            assertEquals(10, deal.hand(seat).size());
            seen.addAll(deal.hand(seat));
        }
        assertEquals(32, seen.size());
    }

    @Test
    void followsTheOfficialPacketOrder() {
        List<Card> deck = new ArrayList<>(Card.all());
        Deal deal = Dealer.deal(deck);
        assertEquals(deck.subList(0, 3), deal.hand(Seat.FOREHAND).subList(0, 3));
        assertEquals(deck.subList(3, 6), deal.hand(Seat.MIDDLEHAND).subList(0, 3));
        assertEquals(deck.subList(9, 11), deal.skat());
        assertEquals(deck.subList(11, 15), deal.hand(Seat.FOREHAND).subList(3, 7));
        assertEquals(deck.subList(29, 32), deal.hand(Seat.REARHAND).subList(7, 10));
    }

    @Test
    void rejectsIncompleteDeck() {
        assertThrows(IllegalArgumentException.class, () -> Dealer.deal(Card.all().subList(0, 31)));
    }
}
