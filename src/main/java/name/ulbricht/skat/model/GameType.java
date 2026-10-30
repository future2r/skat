package name.ulbricht.skat.model;

/**
 * The kinds of game a declarer can announce.
 */
public enum GameType {
    /** Suit game with acorns ("Eichel") as trump suit. */
    ACORNS(Suit.ACORNS, 12),
    /** Suit game with leaves ("Grün") as trump suit. */
    LEAVES(Suit.LEAVES, 11),
    /** Suit game with hearts ("Rot") as trump suit. */
    HEARTS(Suit.HEARTS, 10),
    /** Suit game with bells ("Schellen") as trump suit. */
    BELLS(Suit.BELLS, 9),
    /** Grand: only the four Unter are trump. */
    GRAND(null, 24),
    /** Null: no trump, the declarer must not take any trick. */
    NULL(null, 0);

    private final Suit trumpSuit;
    private final int baseValue;

    GameType(Suit trumpSuit, int baseValue) {
        this.trumpSuit = trumpSuit;
        this.baseValue = baseValue;
    }

    /**
     * Returns the suit game for the given trump suit.
     *
     * @param suit the trump suit
     * @return the matching suit game
     */
    public static GameType suitGame(Suit suit) {
        for (GameType type : values()) {
            if (type.trumpSuit == suit) {
                return type;
            }
        }
        throw new IllegalArgumentException("No suit game for " + suit);
    }

    /**
     * Returns the trump suit of a suit game.
     *
     * @return the trump suit, or {@code null} for Grand and Null
     */
    public Suit trumpSuit() {
        return trumpSuit;
    }

    /**
     * Returns the base value ("Grundwert") used to compute the game value.
     *
     * @return 9 to 12 for suit games, 24 for Grand, 0 for Null (Null has fixed values)
     */
    public int baseValue() {
        return baseValue;
    }

    /**
     * Tells whether this is one of the four suit games.
     *
     * @return {@code true} for acorns, leaves, hearts and bells
     */
    public boolean isSuitGame() {
        return trumpSuit != null;
    }

    /**
     * Tells whether the Unter are trump in this game.
     *
     * @return {@code true} for suit games and Grand
     */
    public boolean hasTrump() {
        return this != NULL;
    }
}
