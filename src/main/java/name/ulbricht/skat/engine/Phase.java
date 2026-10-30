package name.ulbricht.skat.engine;

/**
 * The phases of a single game.
 */
public enum Phase {
    /** Players bid; see {@link Bidding}. */
    BIDDING,
    /** The declarer decides between taking up the skat and playing hand. */
    SKAT_DECISION,
    /** The declarer took up the skat and puts two cards away. */
    DISCARD,
    /** The declarer announces the game. */
    DECLARATION,
    /** Cards are played into the current trick. */
    PLAYING,
    /** The current trick is complete and waits to be collected. */
    TRICK_COMPLETE,
    /** The game is over and has a result. */
    FINISHED,
    /** All players passed; the cards are dealt again by the next dealer. */
    PASSED_IN
}
