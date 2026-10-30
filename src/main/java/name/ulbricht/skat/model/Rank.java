package name.ulbricht.skat.model;

/**
 * The eight ranks of a Skat deck with their card points ("Augen").
 *
 * <p>{@code UNTER} and {@code OBER} keep their German names because English has no common
 * equivalent for these German-suited court cards. The declaration order is not the trick-taking
 * order; that depends on the game type and is defined in {@code name.ulbricht.skat.rules.CardOrder}.
 */
public enum Rank {
    /** Seven, 0 points. */
    SEVEN(0),
    /** Eight, 0 points. */
    EIGHT(0),
    /** Nine, 0 points. */
    NINE(0),
    /** Unter, 2 points; always trump except in Null games. */
    UNTER(2),
    /** Ober, 3 points. */
    OBER(3),
    /** King, 4 points. */
    KING(4),
    /** Ten, 10 points. */
    TEN(10),
    /** Ace ("Ass"), 11 points. */
    ACE(11);

    private final int points;

    Rank(int points) {
        this.points = points;
    }

    /**
     * Returns the card points ("Augen") of this rank.
     *
     * @return the points (0 to 11)
     */
    public int points() {
        return points;
    }
}
