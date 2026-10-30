package name.ulbricht.skat.rules;

import java.util.Collection;
import java.util.List;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.GameType;

/**
 * The matadors ("Spitzen") of a declarer: the unbroken run of top trumps the declarer holds
 * ("mit") or lacks ("ohne"), counted over hand and skat.
 */
public final class Matadors {

    private final boolean with;
    private final int count;

    private Matadors(boolean with, int count) {
        this.with = with;
        this.count = count;
    }

    /**
     * Counts the matadors.
     *
     * @param declarerCards the declarer's twelve cards (hand plus skat)
     * @param type a suit game or Grand
     * @return the matadors
     * @throws IllegalArgumentException for Null games, which have no matadors
     */
    public static Matadors of(Collection<Card> declarerCards, GameType type) {
        if (!type.hasTrump()) {
            throw new IllegalArgumentException("Null games have no matadors");
        }
        List<Card> trumps = CardOrder.trumpsDescending(type);
        boolean with = declarerCards.contains(trumps.get(0));
        int count = 0;
        for (Card trump : trumps) {
            if (declarerCards.contains(trump) != with) {
                break;
            }
            count++;
        }
        return new Matadors(with, count);
    }

    /**
     * Tells whether the declarer holds the highest trump.
     *
     * @return {@code true} for "mit", {@code false} for "ohne"
     */
    public boolean isWith() {
        return with;
    }

    /**
     * Returns the length of the run of held or missing top trumps.
     *
     * @return the number of matadors (at least 1)
     */
    public int count() {
        return count;
    }

    @Override
    public String toString() {
        return (with ? "with " : "without ") + count;
    }
}
