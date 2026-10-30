package name.ulbricht.skat.model;

/**
 * The four suits of the German-suited deck, declared from highest to lowest.
 *
 * <p>The order matters for Skat: it ranks the Unter among each other (the Unter of acorns is the
 * highest trump) and the suit games by their base value.
 */
public enum Suit {
    /** Acorns, base value 12. */
    ACORNS(12),
    /** Leaves, base value 11. */
    LEAVES(11),
    /** Hearts, base value 10. */
    HEARTS(10),
    /** Bells, base value 9. */
    BELLS(9);

    private final int baseValue;

    Suit(int baseValue) {
        this.baseValue = baseValue;
    }

    /**
     * Returns the base value of a suit game with this suit as trump.
     *
     * @return the base value (9 to 12)
     */
    public int baseValue() {
        return baseValue;
    }
}
