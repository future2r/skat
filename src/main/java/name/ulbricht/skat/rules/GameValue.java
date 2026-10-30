package name.ulbricht.skat.rules;

import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;

/**
 * Computes game values ("Spielwert").
 *
 * <p>Suit games and Grand: base value times the multiplier, where the multiplier is the number
 * of matadors plus one for the game itself and one for each of Hand, Schneider, Schneider
 * announced, Schwarz, Schwarz announced and Ouvert. Announced Schneider or Schwarz always count,
 * even if they are not reached.
 *
 * <p>Null games have fixed values: Null 23, Null Hand 35, Null ouvert 46, Null ouvert Hand 59.
 */
public final class GameValue {

    /** Value of a plain Null game. */
    public static final int NULL = 23;
    /** Value of Null Hand. */
    public static final int NULL_HAND = 35;
    /** Value of Null ouvert. */
    public static final int NULL_OUVERT = 46;
    /** Value of Null ouvert Hand. */
    public static final int NULL_OUVERT_HAND = 59;

    private GameValue() {
    }

    /**
     * Computes the multiplier ("Spiel x") of a suit game or Grand.
     *
     * @param declaration the declared game
     * @param matadors the number of matadors, with or without
     * @param schneider whether either party ended up Schneider
     * @param schwarz whether either party ended up Schwarz
     * @return the multiplier
     */
    public static int multiplier(Declaration declaration, int matadors, boolean schneider, boolean schwarz) {
        int multiplier = matadors + 1;
        if (declaration.isHand()) {
            multiplier++;
        }
        if (schneider || schwarz || declaration.isSchneiderAnnounced()) {
            multiplier++;
        }
        if (declaration.isSchneiderAnnounced()) {
            multiplier++;
        }
        if (schwarz || declaration.isSchwarzAnnounced()) {
            multiplier++;
        }
        if (declaration.isSchwarzAnnounced()) {
            multiplier++;
        }
        if (declaration.isOuvert()) {
            multiplier++;
        }
        return multiplier;
    }

    /**
     * Computes the value of a game.
     *
     * @param declaration the declared game
     * @param matadors the number of matadors (ignored for Null)
     * @param schneider whether either party ended up Schneider (ignored for Null)
     * @param schwarz whether either party ended up Schwarz (ignored for Null)
     * @return the game value
     */
    public static int of(Declaration declaration, int matadors, boolean schneider, boolean schwarz) {
        if (declaration.type() == GameType.NULL) {
            return nullValue(declaration);
        }
        return declaration.type().baseValue() * multiplier(declaration, matadors, schneider, schwarz);
    }

    /**
     * Computes the value the declarer reaches for sure, i.e. without Schneider or Schwarz.
     * This is what the declaration dialog shows.
     *
     * @param declaration the declared game
     * @param matadors the number of matadors (ignored for Null)
     * @return the guaranteed game value
     */
    public static int guaranteed(Declaration declaration, int matadors) {
        return of(declaration, matadors, false, false);
    }

    /**
     * Returns the fixed value of a Null game.
     *
     * @param declaration a Null declaration
     * @return 23, 35, 46 or 59
     */
    public static int nullValue(Declaration declaration) {
        if (declaration.isOuvert()) {
            return declaration.isHand() ? NULL_OUVERT_HAND : NULL_OUVERT;
        }
        return declaration.isHand() ? NULL_HAND : NULL;
    }

    /**
     * Returns the value of an overbid game: the smallest multiple of the base value that
     * reaches the bid.
     *
     * @param type a suit game or Grand
     * @param bid the bid the declarer won with
     * @return the value used for scoring the lost game
     */
    public static int overbidValue(GameType type, int bid) {
        int base = type.baseValue();
        return (bid + base - 1) / base * base;
    }
}
