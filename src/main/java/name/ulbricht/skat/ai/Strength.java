package name.ulbricht.skat.ai;

/**
 * How well the computer players play.
 */
public enum Strength {
    /** Simple rules of thumb and now and then a careless card ("Gemütlich"). */
    CASUAL,
    /** Rules of thumb with remembering the played cards; also gives the tips ("Normal"). */
    NORMAL,
    /** Plays every card out in many possible distributions of the unseen cards ("Stark"). */
    STRONG;

    /**
     * Creates a computer player of this strength.
     *
     * @param seed the seed for the player's random choices
     * @return the player
     */
    public ComputerPlayer create(long seed) {
        switch (this) {
            case CASUAL:
                return new CasualPlayer(seed);
            case STRONG:
                return new StrongPlayer(seed);
            default:
                return new SimplePlayer();
        }
    }
}
