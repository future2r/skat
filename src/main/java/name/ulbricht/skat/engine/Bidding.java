package name.ulbricht.skat.engine;

import name.ulbricht.skat.model.Seat;
import name.ulbricht.skat.rules.BidValues;

/**
 * The bidding ("Reizen") as a state machine.
 *
 * <ol>
 *   <li>Middlehand says values to forehand, who holds or passes.</li>
 *   <li>Rearhand says values to the survivor, starting above the current bid.</li>
 *   <li>If nobody said anything, forehand may play for 18 or pass; then the game is passed in.</li>
 * </ol>
 * The player to act is either asked to say the next value or to hold the current one; in both
 * cases the answer is {@link #accept()} or {@link #pass()}.
 */
public final class Bidding {

    private enum Stage { MIDDLE_TO_FORE, REAR_TO_SURVIVOR, FOREHAND_ALONE, DONE }

    private Stage stage = Stage.MIDDLE_TO_FORE;
    private Seat sayer = Seat.MIDDLEHAND;
    private Seat listener = Seat.FOREHAND;
    private boolean sayerToAct = true;
    private int bid;
    private Seat winner;

    /**
     * Returns the seat that has to answer next.
     *
     * @return the acting seat
     * @throws IllegalStateException if the bidding is over
     */
    public Seat toAct() {
        requireOpen();
        return sayerToAct ? sayer : listener;
    }

    /**
     * Tells whether the acting player is asked to say a new value (rather than to hold one).
     *
     * @return {@code true} if a new value is to be said
     */
    public boolean isAskingToSay() {
        return sayerToAct;
    }

    /**
     * Returns the value the acting player answers about: the next value when saying, the current
     * bid when holding.
     *
     * @return the value in question, or -1 if no higher value exists
     */
    public int pendingValue() {
        return sayerToAct ? BidValues.next(bid) : bid;
    }

    /**
     * Returns the highest value said so far.
     *
     * @return the current bid, or 0 if nothing was said yet
     */
    public int currentBid() {
        return bid;
    }

    /**
     * Tells whether the bidding is over.
     *
     * @return {@code true} once a declarer is found or all passed
     */
    public boolean isDone() {
        return stage == Stage.DONE;
    }

    /**
     * Tells whether all players passed.
     *
     * @return {@code true} if the game is passed in
     */
    public boolean isPassedIn() {
        return stage == Stage.DONE && winner == null;
    }

    /**
     * Returns the winner of the bidding.
     *
     * @return the declarer's seat, or {@code null} if the bidding is open or passed in
     */
    public Seat winner() {
        return winner;
    }

    /**
     * The acting player says the pending value or holds the current bid.
     *
     * @throws IllegalStateException if the bidding is over or no higher value exists
     */
    public void accept() {
        requireOpen();
        if (!sayerToAct) {
            sayerToAct = true;
            return;
        }
        int next = BidValues.next(bid);
        if (next < 0) {
            throw new IllegalStateException("No higher bid exists");
        }
        bid = next;
        if (stage == Stage.FOREHAND_ALONE) {
            finish(sayer);
        } else {
            sayerToAct = false;
        }
    }

    /**
     * The acting player passes.
     *
     * @throws IllegalStateException if the bidding is over
     */
    public void pass() {
        requireOpen();
        Seat survivor = sayerToAct ? listener : sayer;
        switch (stage) {
            case MIDDLE_TO_FORE:
                stage = Stage.REAR_TO_SURVIVOR;
                sayer = Seat.REARHAND;
                listener = survivor;
                sayerToAct = true;
                break;
            case REAR_TO_SURVIVOR:
                if (bid == 0) {
                    stage = Stage.FOREHAND_ALONE;
                    sayer = Seat.FOREHAND;
                    listener = null;
                    sayerToAct = true;
                } else {
                    finish(survivor);
                }
                break;
            case FOREHAND_ALONE:
                finish(null);
                break;
            default:
                throw new IllegalStateException("Unexpected stage " + stage);
        }
    }

    private void finish(Seat declarer) {
        winner = declarer;
        stage = Stage.DONE;
    }

    private void requireOpen() {
        if (stage == Stage.DONE) {
            throw new IllegalStateException("The bidding is over");
        }
    }
}
