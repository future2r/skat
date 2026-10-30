package name.ulbricht.skat.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * One of the 32 cards of a Skat deck.
 *
 * <p>Instances are cached and obtained via {@link #of(Suit, Rank)}, so two cards are equal
 * exactly when they are the same instance.
 */
public final class Card {

    private static final Map<Suit, Map<Rank, Card>> CACHE = new EnumMap<>(Suit.class);
    private static final List<Card> ALL;

    static {
        List<Card> all = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            Map<Rank, Card> bySuit = new EnumMap<>(Rank.class);
            for (Rank rank : Rank.values()) {
                Card card = new Card(suit, rank);
                bySuit.put(rank, card);
                all.add(card);
            }
            CACHE.put(suit, bySuit);
        }
        ALL = Collections.unmodifiableList(all);
    }

    private final Suit suit;
    private final Rank rank;

    private Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    /**
     * Returns the card with the given suit and rank.
     *
     * @param suit the suit
     * @param rank the rank
     * @return the cached card instance
     */
    public static Card of(Suit suit, Rank rank) {
        return CACHE.get(suit).get(rank);
    }

    /**
     * Returns all 32 cards, grouped by suit in declaration order.
     *
     * @return an unmodifiable list of all cards
     */
    public static List<Card> all() {
        return ALL;
    }

    /**
     * Returns the suit printed on the card.
     *
     * @return the suit
     */
    public Suit suit() {
        return suit;
    }

    /**
     * Returns the rank of the card.
     *
     * @return the rank
     */
    public Rank rank() {
        return rank;
    }

    /**
     * Returns the card points ("Augen").
     *
     * @return the points (0 to 11)
     */
    public int points() {
        return rank.points();
    }

    /**
     * Tells whether this card is an Unter.
     *
     * @return {@code true} for the four Unter
     */
    public boolean isUnter() {
        return rank == Rank.UNTER;
    }

    /**
     * Sums the card points of the given cards.
     *
     * @param cards the cards to count
     * @return the total points
     */
    public static int totalPoints(Iterable<Card> cards) {
        int sum = 0;
        for (Card card : cards) {
            sum += card.points();
        }
        return sum;
    }

    @Override
    public String toString() {
        return suit + "_" + rank;
    }
}
