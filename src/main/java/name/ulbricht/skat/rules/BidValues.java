package name.ulbricht.skat.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import name.ulbricht.skat.model.GameType;

/**
 * The legal bids ("Reizwerte"): every possible game value from 18 upwards.
 */
public final class BidValues {

    /** The lowest bid. */
    public static final int FIRST = 18;

    /** Highest multiplier of a suit game: 11 matadors plus 7 levels. */
    private static final int MAX_SUIT_MULTIPLIER = 18;
    /** Highest multiplier of a Grand: 4 matadors plus 7 levels. */
    private static final int MAX_GRAND_MULTIPLIER = 11;
    private static final int MIN_MULTIPLIER = 2;

    private static final List<Integer> VALUES = computeValues();

    private BidValues() {
    }

    private static List<Integer> computeValues() {
        TreeSet<Integer> values = new TreeSet<>();
        for (GameType type : GameType.values()) {
            if (type.isSuitGame()) {
                addMultiples(values, type.baseValue(), MAX_SUIT_MULTIPLIER);
            }
        }
        addMultiples(values, GameType.GRAND.baseValue(), MAX_GRAND_MULTIPLIER);
        values.add(GameValue.NULL);
        values.add(GameValue.NULL_HAND);
        values.add(GameValue.NULL_OUVERT);
        values.add(GameValue.NULL_OUVERT_HAND);
        return new ArrayList<>(values.tailSet(FIRST));
    }

    private static void addMultiples(TreeSet<Integer> values, int base, int maxMultiplier) {
        for (int multiplier = MIN_MULTIPLIER; multiplier <= maxMultiplier; multiplier++) {
            values.add(base * multiplier);
        }
    }

    /**
     * Returns all legal bids in ascending order.
     *
     * @return an unmodifiable list starting with 18
     */
    public static List<Integer> all() {
        return Collections.unmodifiableList(VALUES);
    }

    /**
     * Returns the next legal bid above a value.
     *
     * @param current the current bid, or 0 if nothing was bid yet
     * @return the next higher bid, or -1 if there is none
     */
    public static int next(int current) {
        for (int value : VALUES) {
            if (value > current) {
                return value;
            }
        }
        return -1;
    }
}
