package name.ulbricht.skat.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Deal;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.Seat;
import name.ulbricht.skat.rules.GameResult;
import name.ulbricht.skat.rules.GameValue;
import name.ulbricht.skat.rules.TrickRules;

/**
 * One game of Skat from the deal to the result, as a passive state machine.
 *
 * <p>The game never decides anything itself: every step is an action of the seat returned by
 * {@link #actingSeat()}. Invalid actions are rejected with an exception, so callers (UI and
 * computer players) can rely on the state always being consistent.
 */
public final class Game {

    private final Map<Seat, List<Card>> hands = new EnumMap<>(Seat.class);
    private final Map<Seat, List<Card>> wonCards = new EnumMap<>(Seat.class);
    private final List<Card> skat;
    private final Bidding bidding = new Bidding();
    private final List<Card> currentTrick = new ArrayList<>();
    private final List<CompletedTrick> tricks = new ArrayList<>();

    private Phase phase = Phase.BIDDING;
    private Seat declarer;
    private int bid;
    private boolean hand;
    private Declaration declaration;
    private List<Card> declarerCards;
    private Seat trickLeader = Seat.FOREHAND;
    private Seat toPlay = Seat.FOREHAND;
    private GameResult result;

    /**
     * Starts a game with the given deal.
     *
     * @param deal the dealt cards
     */
    public Game(Deal deal) {
        for (Seat seat : Seat.values()) {
            hands.put(seat, new ArrayList<>(deal.hand(seat)));
            wonCards.put(seat, new ArrayList<>());
        }
        skat = new ArrayList<>(deal.skat());
    }

    // ----------------------------------------------------------------------------------------
    // Queries

    /**
     * Returns the current phase.
     *
     * @return the phase
     */
    public Phase phase() {
        return phase;
    }

    /**
     * Returns the seat whose action the game is waiting for.
     *
     * @return the acting seat, or {@code null} if a trick waits to be collected or the game is over
     */
    public Seat actingSeat() {
        switch (phase) {
            case BIDDING:
                return bidding.toAct();
            case SKAT_DECISION:
            case DISCARD:
            case DECLARATION:
                return declarer;
            case PLAYING:
                return toPlay;
            default:
                return null;
        }
    }

    /**
     * Tells whether the acting bidder is asked to say a new value rather than to hold one.
     *
     * @return {@code true} if a new value is to be said
     */
    public boolean isAskingToSay() {
        return bidding.isAskingToSay();
    }

    /**
     * Returns the value the acting bidder answers about.
     *
     * @return the value to say or hold, or -1 if no higher value exists
     */
    public int pendingBidValue() {
        return bidding.pendingValue();
    }

    /**
     * Returns the highest value said so far during the bidding.
     *
     * @return the current bid, or 0 if nothing was said yet
     */
    public int currentBid() {
        return bidding.currentBid();
    }

    /**
     * Returns the cards a seat currently holds.
     *
     * @param seat the seat
     * @return an unmodifiable view of the hand
     */
    public List<Card> hand(Seat seat) {
        return Collections.unmodifiableList(hands.get(seat));
    }

    /**
     * Returns the skat: the dealt skat before it is taken up, the discarded cards afterwards.
     *
     * @return an unmodifiable view of the skat (empty while the declarer holds twelve cards)
     */
    public List<Card> skat() {
        return Collections.unmodifiableList(skat);
    }

    /**
     * Returns the declarer.
     *
     * @return the declarer's seat, or {@code null} before the bidding is over
     */
    public Seat declarer() {
        return declarer;
    }

    /**
     * Returns the winning bid.
     *
     * @return the bid, or 0 before the bidding is over
     */
    public int bid() {
        return bid;
    }

    /**
     * Tells whether the declarer plays without taking up the skat.
     *
     * @return {@code true} for hand games
     */
    public boolean isHand() {
        return hand;
    }

    /**
     * Returns the announced game.
     *
     * @return the declaration, or {@code null} before it is announced
     */
    public Declaration declaration() {
        return declaration;
    }

    /**
     * Returns the cards played into the current trick.
     *
     * @return an unmodifiable view in playing order
     */
    public List<Card> currentTrick() {
        return Collections.unmodifiableList(currentTrick);
    }

    /**
     * Returns the seat that led the current trick.
     *
     * @return the leader
     */
    public Seat trickLeader() {
        return trickLeader;
    }

    /**
     * Returns all collected tricks.
     *
     * @return an unmodifiable view in playing order
     */
    public List<CompletedTrick> tricks() {
        return Collections.unmodifiableList(tricks);
    }

    /**
     * Returns the winner of the complete current trick.
     *
     * @return the seat that takes the trick
     * @throws IllegalStateException if the trick is not complete
     */
    public Seat currentTrickWinner() {
        require(Phase.TRICK_COMPLETE);
        return seatAt(TrickRules.winnerIndex(currentTrick, declaration.type()));
    }

    /**
     * Returns the cards the seat on turn may play.
     *
     * @return the playable cards
     * @throws IllegalStateException if no card is to be played
     */
    public List<Card> playableCards() {
        require(Phase.PLAYING);
        return TrickRules.playableCards(hands.get(toPlay), currentTrick, declaration.type());
    }

    /**
     * Returns the card points a seat has won in tricks so far, without the skat.
     *
     * @param seat the seat
     * @return the points
     */
    public int trickPoints(Seat seat) {
        return Card.totalPoints(wonCards.get(seat));
    }

    /**
     * Returns the result of the finished game.
     *
     * @return the result, or {@code null} while the game is running
     */
    public GameResult result() {
        return result;
    }

    // ----------------------------------------------------------------------------------------
    // Bidding

    /**
     * The acting player says the pending value or holds the current bid.
     */
    public void acceptBid() {
        require(Phase.BIDDING);
        bidding.accept();
        afterBidding();
    }

    /**
     * The acting player passes.
     */
    public void passBid() {
        require(Phase.BIDDING);
        bidding.pass();
        afterBidding();
    }

    private void afterBidding() {
        if (!bidding.isDone()) {
            return;
        }
        if (bidding.isPassedIn()) {
            phase = Phase.PASSED_IN;
            return;
        }
        declarer = bidding.winner();
        bid = bidding.currentBid();
        phase = Phase.SKAT_DECISION;
    }

    // ----------------------------------------------------------------------------------------
    // Skat and declaration

    /**
     * The declarer plays without taking up the skat.
     */
    public void playHand() {
        require(Phase.SKAT_DECISION);
        hand = true;
        declarerCards = new ArrayList<>(hands.get(declarer));
        declarerCards.addAll(skat);
        phase = Phase.DECLARATION;
    }

    /**
     * The declarer takes up the skat and then holds twelve cards.
     */
    public void takeSkat() {
        require(Phase.SKAT_DECISION);
        hands.get(declarer).addAll(skat);
        skat.clear();
        phase = Phase.DISCARD;
    }

    /**
     * The declarer puts two cards away ("drücken").
     *
     * @param first the first card to put away
     * @param second the second card to put away
     * @throws IllegalArgumentException if the cards are equal or not in the declarer's hand
     */
    public void discard(Card first, Card second) {
        require(Phase.DISCARD);
        List<Card> declarerHand = hands.get(declarer);
        if (first == second || !declarerHand.contains(first) || !declarerHand.contains(second)) {
            throw new IllegalArgumentException("Two different cards of the declarer are required");
        }
        declarerCards = new ArrayList<>(declarerHand);
        declarerHand.remove(first);
        declarerHand.remove(second);
        skat.add(first);
        skat.add(second);
        phase = Phase.DECLARATION;
    }

    /**
     * Tells whether a declaration is allowed now: hand games must be declared as hand games and
     * vice versa, and a Null game must reach the bid because its value is fixed.
     *
     * @param candidate the declaration to check
     * @return {@code true} if {@link #declare(Declaration)} would accept it
     */
    public boolean canDeclare(Declaration candidate) {
        if (phase != Phase.DECLARATION || candidate.isHand() != hand) {
            return false;
        }
        return candidate.type() != GameType.NULL || GameValue.nullValue(candidate) >= bid;
    }

    /**
     * The declarer announces the game; forehand leads the first trick.
     *
     * @param chosen the announced game
     * @throws IllegalArgumentException if the declaration is not allowed
     */
    public void declare(Declaration chosen) {
        require(Phase.DECLARATION);
        if (!canDeclare(chosen)) {
            throw new IllegalArgumentException("Declaration not allowed: " + chosen);
        }
        declaration = chosen;
        phase = Phase.PLAYING;
    }

    // ----------------------------------------------------------------------------------------
    // Playing

    /**
     * The seat on turn plays a card.
     *
     * @param card the card to play
     * @throws IllegalArgumentException if the card may not be played
     */
    public void play(Card card) {
        require(Phase.PLAYING);
        if (!playableCards().contains(card)) {
            throw new IllegalArgumentException("Card may not be played: " + card);
        }
        hands.get(toPlay).remove(card);
        currentTrick.add(card);
        if (currentTrick.size() == TrickRules.TRICK_SIZE) {
            phase = Phase.TRICK_COMPLETE;
        } else {
            toPlay = toPlay.next();
        }
    }

    /**
     * Collects the complete trick; its winner leads the next one. Ends the game after the last
     * trick, or as soon as the declarer takes a trick in a Null game.
     */
    public void collectTrick() {
        require(Phase.TRICK_COMPLETE);
        Seat winner = currentTrickWinner();
        tricks.add(new CompletedTrick(currentTrick, trickLeader, winner));
        wonCards.get(winner).addAll(currentTrick);
        currentTrick.clear();
        boolean nullLost = declaration.type() == GameType.NULL && winner == declarer;
        if (nullLost || hands.get(winner).isEmpty()) {
            finish();
            return;
        }
        trickLeader = winner;
        toPlay = winner;
        phase = Phase.PLAYING;
    }

    /**
     * Tells whether a seat may take back its card: only as long as it is the last card in the
     * uncollected current trick, i.e. nobody has played after it.
     *
     * @param seat the seat
     * @return {@code true} if the seat played the most recent card of the current trick
     */
    public boolean canTakeBack(Seat seat) {
        boolean trickOpen = phase == Phase.PLAYING || phase == Phase.TRICK_COMPLETE;
        return trickOpen && !currentTrick.isEmpty() && seatAt(currentTrick.size() - 1) == seat;
    }

    /**
     * Takes back the seat's card from the current trick. The card returns to the hand and the
     * seat is on turn again.
     *
     * @param seat the seat taking back its card
     * @throws IllegalStateException if the seat did not play the last card of the current trick
     */
    public void takeBack(Seat seat) {
        if (!canTakeBack(seat)) {
            throw new IllegalStateException("Nothing to take back for " + seat);
        }
        hands.get(seat).add(currentTrick.remove(currentTrick.size() - 1));
        toPlay = seat;
        phase = Phase.PLAYING;
    }

    private Seat seatAt(int index) {
        return trickLeader.after(index);
    }

    private void finish() {
        int declarerPoints = trickPoints(declarer) + Card.totalPoints(skat);
        int declarerTricks = 0;
        for (CompletedTrick trick : tricks) {
            if (trick.winner() == declarer) {
                declarerTricks++;
            }
        }
        result = GameResult.evaluate(declaration, bid, declarerCards, declarerPoints, declarerTricks);
        phase = Phase.FINISHED;
    }

    private void require(Phase expected) {
        if (phase != expected) {
            throw new IllegalStateException("Expected phase " + expected + " but was " + phase);
        }
    }
}
