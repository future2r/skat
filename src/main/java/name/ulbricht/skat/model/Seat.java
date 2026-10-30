package name.ulbricht.skat.model;

/**
 * The three positions at the table for one game, in bidding and playing order.
 *
 * <p>The dealer is always the rear hand ("Hinterhand"). Seats rotate between games, see
 * {@link PlayerId}.
 */
public enum Seat {
    /** Forehand ("Vorhand"): receives the first cards and leads the first trick. */
    FOREHAND,
    /** Middlehand ("Mittelhand"): bids first. */
    MIDDLEHAND,
    /** Rearhand ("Hinterhand"): the dealer. */
    REARHAND;

    /**
     * Returns the seat that plays after this one.
     *
     * @return the next seat in clockwise order
     */
    public Seat next() {
        return after(1);
    }

    /**
     * Returns the seat that plays {@code steps} cards after this one, e.g. the player of the
     * card at position {@code steps} of a trick led by this seat.
     *
     * @param steps how many seats to go on, at least 0
     * @return the seat in clockwise order
     */
    public Seat after(int steps) {
        return values()[(ordinal() + steps) % values().length];
    }
}
