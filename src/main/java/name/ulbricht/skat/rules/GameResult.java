package name.ulbricht.skat.rules;

import java.util.Collection;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;

/**
 * The outcome of a finished game from the declarer's point of view.
 *
 * <p>Suit games and Grand are won with at least 61 card points, at least 90 if Schneider was
 * announced, and all tricks if Schwarz was announced. Schneider means a party has at most
 * 30 points, Schwarz means a party has no trick. A Null game is won if the declarer takes no
 * trick. A game whose value does not reach the bid is overbid and lost; its value is then the
 * smallest multiple of its base value that reaches the bid.
 */
public final class GameResult {

    /** Points needed to win a suit game or Grand. */
    public static final int WINNING_POINTS = 61;
    /** Points at which the opponents are Schneider. */
    public static final int SCHNEIDER_POINTS = 90;
    /** At most this many points means being Schneider. */
    public static final int SCHNEIDER_LIMIT = 30;
    /** Number of tricks in a game. */
    public static final int TRICKS = 10;

    private final Declaration declaration;
    private final boolean won;
    private final boolean overbid;
    private final boolean schneider;
    private final boolean schwarz;
    private final int declarerPoints;
    private final int matadors;
    private final boolean withMatadors;
    private final int value;

    private GameResult(Declaration declaration, boolean won, boolean overbid, boolean schneider, boolean schwarz,
            int declarerPoints, Matadors matadors, int value) {
        this.declaration = declaration;
        this.won = won;
        this.overbid = overbid;
        this.schneider = schneider;
        this.schwarz = schwarz;
        this.declarerPoints = declarerPoints;
        this.matadors = matadors == null ? 0 : matadors.count();
        this.withMatadors = matadors == null || matadors.isWith();
        this.value = value;
    }

    /**
     * Evaluates a finished game.
     *
     * @param declaration the declared game
     * @param bid the bid the declarer won with
     * @param declarerCards the declarer's twelve cards before play (hand plus skat), used for matadors
     * @param declarerPoints the declarer's card points including the skat
     * @param declarerTricks the number of tricks the declarer took
     * @return the result
     */
    public static GameResult evaluate(Declaration declaration, int bid, Collection<Card> declarerCards,
            int declarerPoints, int declarerTricks) {
        if (declaration.type() == GameType.NULL) {
            return evaluateNull(declaration, bid, declarerPoints, declarerTricks);
        }
        boolean schneider = declarerPoints >= SCHNEIDER_POINTS || declarerPoints <= SCHNEIDER_LIMIT;
        boolean schwarz = declarerTricks == TRICKS || declarerTricks == 0;
        boolean won = declarerPoints >= WINNING_POINTS;
        if (declaration.isSchneiderAnnounced()) {
            won = won && declarerPoints >= SCHNEIDER_POINTS;
        }
        if (declaration.isSchwarzAnnounced()) {
            won = won && declarerTricks == TRICKS;
        }
        Matadors matadors = Matadors.of(declarerCards, declaration.type());
        int value = GameValue.of(declaration, matadors.count(), schneider, schwarz);
        boolean overbid = value < bid;
        if (overbid) {
            won = false;
            value = GameValue.overbidValue(declaration.type(), bid);
        }
        return new GameResult(declaration, won, overbid, schneider, schwarz, declarerPoints, matadors, value);
    }

    private static GameResult evaluateNull(Declaration declaration, int bid, int declarerPoints, int declarerTricks) {
        int value = GameValue.nullValue(declaration);
        boolean overbid = value < bid;
        boolean won = declarerTricks == 0 && !overbid;
        return new GameResult(declaration, won, overbid, false, false, declarerPoints, null, value);
    }

    /**
     * Returns the declared game.
     *
     * @return the declaration
     */
    public Declaration declaration() {
        return declaration;
    }

    /**
     * Tells whether the declarer won.
     *
     * @return {@code true} if won
     */
    public boolean isWon() {
        return won;
    }

    /**
     * Tells whether the game value did not reach the bid.
     *
     * @return {@code true} if overbid
     */
    public boolean isOverbid() {
        return overbid;
    }

    /**
     * Tells whether one party ended up Schneider.
     *
     * @return {@code true} if Schneider was reached by either side
     */
    public boolean isSchneider() {
        return schneider;
    }

    /**
     * Tells whether one party took no trick.
     *
     * @return {@code true} if Schwarz was reached by either side
     */
    public boolean isSchwarz() {
        return schwarz;
    }

    /**
     * Returns the declarer's card points including the skat.
     *
     * @return the points (0 to 120)
     */
    public int declarerPoints() {
        return declarerPoints;
    }

    /**
     * Returns the number of matadors used for the value.
     *
     * @return the matadors; 0 for Null games
     */
    public int matadors() {
        return matadors;
    }

    /**
     * Tells whether the declarer played "mit" (holding the top trump) or "ohne".
     *
     * @return {@code true} for "mit"; also {@code true} for Null games
     */
    public boolean isWithMatadors() {
        return withMatadors;
    }

    /**
     * Returns the game value.
     *
     * @return the value, always positive
     */
    public int value() {
        return value;
    }
}
