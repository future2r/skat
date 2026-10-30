package name.ulbricht.skat.ui;

import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;

/**
 * The additional announcements chosen under "Weitere Ansagen": Schneider, Schwarz and Ouvert.
 *
 * <p>Schwarz includes Schneider; Ouvert in a suit game or Grand includes Schneider and Schwarz.
 * Schneider and Schwarz can only be announced in hand games; Null ouvert is always possible.
 */
final class Announcements {

    private boolean schneider;
    private boolean schwarz;
    private boolean ouvert;

    boolean isSchneider() {
        return schneider;
    }

    boolean isSchwarz() {
        return schwarz;
    }

    boolean isOuvert() {
        return ouvert;
    }

    boolean isAny() {
        return schneider || schwarz || ouvert;
    }

    /** Switches Schneider; switching it off also switches off Schwarz, which includes it. */
    void toggleSchneider() {
        schneider = !schneider;
        if (!schneider) {
            schwarz = false;
        }
    }

    /** Switches Schwarz; switching it on also switches on Schneider. */
    void toggleSchwarz() {
        schwarz = !schwarz;
        if (schwarz) {
            schneider = true;
        }
    }

    void toggleOuvert() {
        ouvert = !ouvert;
    }

    void reset() {
        schneider = false;
        schwarz = false;
        ouvert = false;
    }

    /**
     * Returns the declaration of a game type with the chosen announcements.
     *
     * @param type the game type
     * @param hand whether the declarer plays hand
     * @return the declaration, or {@code null} if the announcements are not allowed for this game
     */
    Declaration declaration(GameType type, boolean hand) {
        if (type == GameType.NULL) {
            return new Declaration(type, hand, false, false, ouvert);
        }
        if (!hand && isAny()) {
            return null;
        }
        return new Declaration(type, hand, schneider || schwarz || ouvert, schwarz || ouvert, ouvert);
    }
}
