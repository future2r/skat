package name.ulbricht.skat.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Deal;
import name.ulbricht.skat.model.Seat;

/**
 * Shuffles and deals the cards as the official rules prescribe: three cards to each player
 * starting with forehand, two into the skat, then four to each, then three to each.
 */
public final class Dealer {

    private static final int[] PACKETS = {3, 4, 3};

    private Dealer() {
    }

    /**
     * Returns a shuffled deck.
     *
     * @param random the source of randomness
     * @return all 32 cards in random order
     */
    public static List<Card> shuffledDeck(Random random) {
        List<Card> deck = new ArrayList<>(Card.all());
        Collections.shuffle(deck, random);
        return deck;
    }

    /**
     * Deals a deck.
     *
     * @param deck all 32 cards in dealing order
     * @return the deal
     * @throws IllegalArgumentException if the deck does not have 32 cards
     */
    public static Deal deal(List<Card> deck) {
        if (deck.size() != Card.all().size()) {
            throw new IllegalArgumentException("A deck has " + Card.all().size() + " cards");
        }
        Map<Seat, List<Card>> hands = new EnumMap<>(Seat.class);
        for (Seat seat : Seat.values()) {
            hands.put(seat, new ArrayList<>());
        }
        List<Card> skat = new ArrayList<>();
        int position = 0;
        for (int round = 0; round < PACKETS.length; round++) {
            for (Seat seat : Seat.values()) {
                for (int i = 0; i < PACKETS[round]; i++) {
                    hands.get(seat).add(deck.get(position++));
                }
            }
            if (round == 0) {
                for (int i = 0; i < Deal.SKAT_SIZE; i++) {
                    skat.add(deck.get(position++));
                }
            }
        }
        return new Deal(hands, skat);
    }
}
