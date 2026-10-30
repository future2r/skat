package name.ulbricht.skat.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The result of dealing: ten cards for each seat and two cards in the skat.
 */
public final class Deal {

    /** Number of cards each player receives. */
    public static final int HAND_SIZE = 10;
    /** Number of cards in the skat. */
    public static final int SKAT_SIZE = 2;

    private final Map<Seat, List<Card>> hands;
    private final List<Card> skat;

    /**
     * Creates a deal.
     *
     * @param hands the ten cards of each seat
     * @param skat the two skat cards
     * @throws IllegalArgumentException if a hand or the skat has the wrong size
     */
    public Deal(Map<Seat, List<Card>> hands, List<Card> skat) {
        Map<Seat, List<Card>> copy = new EnumMap<>(Seat.class);
        for (Seat seat : Seat.values()) {
            List<Card> hand = hands.get(seat);
            if (hand == null || hand.size() != HAND_SIZE) {
                throw new IllegalArgumentException("Seat " + seat + " needs " + HAND_SIZE + " cards");
            }
            copy.put(seat, Collections.unmodifiableList(new ArrayList<>(hand)));
        }
        if (skat.size() != SKAT_SIZE) {
            throw new IllegalArgumentException("The skat needs " + SKAT_SIZE + " cards");
        }
        this.hands = Collections.unmodifiableMap(copy);
        this.skat = Collections.unmodifiableList(new ArrayList<>(skat));
    }

    /**
     * Returns the cards dealt to a seat.
     *
     * @param seat the seat
     * @return an unmodifiable list of ten cards
     */
    public List<Card> hand(Seat seat) {
        return hands.get(seat);
    }

    /**
     * Returns the two skat cards.
     *
     * @return an unmodifiable list of two cards
     */
    public List<Card> skat() {
        return skat;
    }
}
