package name.ulbricht.skat.ui;

/**
 * The card decks the player can choose in the settings. The name shown is {@code deck.<NAME>}.
 */
enum CardDeck {
    /** The own German-suited deck drawn with shapes ("Klares Blatt"). */
    CLEAR(null, false),
    /** Scans of a German-suited Altenburg deck, the default. */
    ALTENBURG("altenburg", false),
    /** A French-suited deck; the texts then say Kreuz, Pik, Herz, Karo, Bube and Dame. */
    FRENCH("french", true);

    private final String folder;
    private final boolean french;

    CardDeck(String folder, boolean french) {
        this.folder = folder;
        this.french = french;
    }

    /** The folder below {@code name/ulbricht/skat/ui/decks} with the card images, or {@code null} if drawn. */
    String folder() {
        return folder;
    }

    /** Tells whether the cards come from image files. */
    boolean hasImages() {
        return folder != null;
    }

    /** Tells whether the deck has French suits, so the texts use the French names. */
    boolean isFrench() {
        return french;
    }
}
