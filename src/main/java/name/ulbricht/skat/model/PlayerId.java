package name.ulbricht.skat.model;

/**
 * The three participants, in clockwise order around the table.
 *
 * <p>Display names are UI texts and live in the message bundle.
 */
public enum PlayerId {
    /** The human player at the bottom of the screen. */
    HUMAN,
    /** The computer player on the left (Ludwig). */
    LEFT,
    /** The computer player on the right (Richard). */
    RIGHT;

    /**
     * Returns the player to the left, i.e. the next one in clockwise order.
     *
     * @return the next player
     */
    public PlayerId next() {
        return values()[(ordinal() + 1) % values().length];
    }

    /**
     * Returns the player who sits on the given seat when this player deals.
     *
     * @param seat the seat to look up
     * @return the player on that seat
     */
    public PlayerId onSeatWhenDealing(Seat seat) {
        PlayerId forehand = next();
        PlayerId result = forehand;
        for (int i = 0; i < seat.ordinal(); i++) {
            result = result.next();
        }
        return result;
    }
}
