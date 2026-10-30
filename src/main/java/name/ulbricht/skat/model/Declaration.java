package name.ulbricht.skat.model;

/**
 * The game announced by the declarer: game type plus the optional announcements.
 *
 * <p>The constructor enforces the official combinations:
 * <ul>
 *   <li>Schneider and Schwarz can only be announced in hand games, never in Null.</li>
 *   <li>Announcing Schwarz includes announcing Schneider.</li>
 *   <li>Ouvert suit and Grand games are always hand games with Schwarz announced.</li>
 * </ul>
 */
public final class Declaration {

    private final GameType type;
    private final boolean hand;
    private final boolean schneiderAnnounced;
    private final boolean schwarzAnnounced;
    private final boolean ouvert;

    /**
     * Creates a declaration and validates the combination of announcements.
     *
     * @param type the game type
     * @param hand whether the game is played without taking up the skat
     * @param schneiderAnnounced whether Schneider is announced
     * @param schwarzAnnounced whether Schwarz is announced
     * @param ouvert whether the declarer plays with open cards
     * @throws IllegalArgumentException if the combination is not allowed
     */
    public Declaration(GameType type, boolean hand, boolean schneiderAnnounced, boolean schwarzAnnounced,
            boolean ouvert) {
        if (type == null) {
            throw new IllegalArgumentException("Game type is required");
        }
        if (type == GameType.NULL && (schneiderAnnounced || schwarzAnnounced)) {
            throw new IllegalArgumentException("Null games have no Schneider or Schwarz announcements");
        }
        if ((schneiderAnnounced || schwarzAnnounced) && !hand) {
            throw new IllegalArgumentException("Announcements require a hand game");
        }
        if (schwarzAnnounced && !schneiderAnnounced) {
            throw new IllegalArgumentException("Announcing Schwarz includes announcing Schneider");
        }
        if (ouvert && type != GameType.NULL && !schwarzAnnounced) {
            throw new IllegalArgumentException("Ouvert suit and Grand games require Schwarz announced");
        }
        this.type = type;
        this.hand = hand;
        this.schneiderAnnounced = schneiderAnnounced;
        this.schwarzAnnounced = schwarzAnnounced;
        this.ouvert = ouvert;
    }

    /**
     * Creates a plain declaration after taking up the skat.
     *
     * @param type the game type
     * @return a declaration without any announcement
     */
    public static Declaration of(GameType type) {
        return new Declaration(type, false, false, false, false);
    }

    /**
     * Creates a hand declaration without further announcements.
     *
     * @param type the game type
     * @return a hand declaration
     */
    public static Declaration hand(GameType type) {
        return new Declaration(type, true, false, false, false);
    }

    /**
     * Returns the game type.
     *
     * @return the game type
     */
    public GameType type() {
        return type;
    }

    /**
     * Tells whether the game is played without taking up the skat.
     *
     * @return {@code true} for hand games
     */
    public boolean isHand() {
        return hand;
    }

    /**
     * Tells whether Schneider was announced.
     *
     * @return {@code true} if announced
     */
    public boolean isSchneiderAnnounced() {
        return schneiderAnnounced;
    }

    /**
     * Tells whether Schwarz was announced.
     *
     * @return {@code true} if announced
     */
    public boolean isSchwarzAnnounced() {
        return schwarzAnnounced;
    }

    /**
     * Tells whether the declarer plays with open cards.
     *
     * @return {@code true} for ouvert games
     */
    public boolean isOuvert() {
        return ouvert;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Declaration)) {
            return false;
        }
        Declaration that = (Declaration) other;
        return type == that.type && hand == that.hand && schneiderAnnounced == that.schneiderAnnounced
                && schwarzAnnounced == that.schwarzAnnounced && ouvert == that.ouvert;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + Boolean.hashCode(hand);
        result = 31 * result + Boolean.hashCode(schneiderAnnounced);
        result = 31 * result + Boolean.hashCode(schwarzAnnounced);
        return 31 * result + Boolean.hashCode(ouvert);
    }

    @Override
    public String toString() {
        return type + (hand ? " hand" : "") + (schneiderAnnounced ? " schneider" : "")
                + (schwarzAnnounced ? " schwarz" : "") + (ouvert ? " ouvert" : "");
    }
}
