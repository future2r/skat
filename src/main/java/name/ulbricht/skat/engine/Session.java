package name.ulbricht.skat.engine;

import java.util.Random;

import name.ulbricht.skat.model.PlayerId;
import name.ulbricht.skat.model.Seat;
import name.ulbricht.skat.rules.Dealer;

/**
 * A sitting at the table: a sequence of games with rotating dealer.
 */
public final class Session {

    private final Random random;
    private PlayerId dealer;
    private Game game;
    private boolean abandoned;

    /**
     * Creates a session.
     *
     * @param seed the seed for shuffling
     * @param firstDealer the player who deals the first game
     */
    public Session(long seed, PlayerId firstDealer) {
        this.random = new Random(seed);
        this.dealer = firstDealer;
    }

    /**
     * Deals a new game. From the second game on, the deal passes to the left.
     *
     * @return the new game
     * @throws IllegalStateException if the current game is still running
     */
    public Game startGame() {
        if (game != null) {
            if (!isOver(game) && !abandoned) {
                throw new IllegalStateException("The current game is not finished");
            }
            dealer = dealer.next();
        }
        game = new Game(Dealer.deal(Dealer.shuffledDeck(random)));
        abandoned = false;
        return game;
    }

    /**
     * Gives up the running game, so that {@link #startGame()} may deal anew.
     */
    public void abandonGame() {
        abandoned = true;
    }

    /**
     * Tells whether the current game is still being played (not finished, passed in or abandoned).
     *
     * @return {@code true} while the game runs
     */
    public boolean isGameRunning() {
        return game != null && !isOver(game) && !abandoned;
    }

    /**
     * Returns the current game.
     *
     * @return the game, or {@code null} before the first deal
     */
    public Game game() {
        return game;
    }

    /**
     * Returns the dealer of the current game.
     *
     * @return the dealer
     */
    public PlayerId dealer() {
        return dealer;
    }

    /**
     * Returns the player sitting on a seat in the current game.
     *
     * @param seat the seat
     * @return the player
     */
    public PlayerId playerAt(Seat seat) {
        return dealer.onSeatWhenDealing(seat);
    }

    /**
     * Returns the seat of a player in the current game.
     *
     * @param player the player
     * @return the seat
     */
    public Seat seatOf(PlayerId player) {
        for (Seat seat : Seat.values()) {
            if (playerAt(seat) == player) {
                return seat;
            }
        }
        throw new IllegalStateException("Player without seat: " + player);
    }

    private static boolean isOver(Game candidate) {
        return candidate.phase() == Phase.FINISHED || candidate.phase() == Phase.PASSED_IN;
    }
}
