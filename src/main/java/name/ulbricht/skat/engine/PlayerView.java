package name.ulbricht.skat.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.Seat;

/**
 * What one player is allowed to know about a running game: the own hand, everything played
 * openly, and the skat only for a declarer who took it up.
 *
 * <p>Computer players decide based on this view only, so they cannot look into other hands.
 */
public final class PlayerView {

    private final Game game;
    private final Seat seat;

    /**
     * Creates the view of a seat.
     *
     * @param game the running game
     * @param seat the seat whose knowledge is represented
     */
    public PlayerView(Game game, Seat seat) {
        this.game = game;
        this.seat = seat;
    }

    /**
     * Returns the seat of this player.
     *
     * @return the seat
     */
    public Seat seat() {
        return seat;
    }

    /**
     * Returns the current phase.
     *
     * @return the phase
     */
    public Phase phase() {
        return game.phase();
    }

    /**
     * Returns the player's own cards.
     *
     * @return a copy of the hand
     */
    public List<Card> hand() {
        return new ArrayList<>(game.hand(seat));
    }

    /**
     * Returns the skat cards if this player may know them: the declarer after taking up the
     * skat (the put-away cards after discarding).
     *
     * @return the known skat cards; empty otherwise
     */
    public List<Card> knownSkat() {
        if (seat == game.declarer() && !game.isHand()) {
            return new ArrayList<>(game.skat());
        }
        return Collections.emptyList();
    }

    /**
     * Returns the declarer.
     *
     * @return the declarer's seat, or {@code null} during the bidding
     */
    public Seat declarer() {
        return game.declarer();
    }

    /**
     * Tells whether this player is the declarer.
     *
     * @return {@code true} for the declarer
     */
    public boolean isDeclarer() {
        return seat == game.declarer();
    }

    /**
     * Returns the announced game.
     *
     * @return the declaration, or {@code null} before it is announced
     */
    public Declaration declaration() {
        return game.declaration();
    }

    /**
     * Returns the winning bid.
     *
     * @return the bid, or 0 during the bidding
     */
    public int bid() {
        return game.bid();
    }

    /**
     * Tells whether the declarer plays hand.
     *
     * @return {@code true} for hand games
     */
    public boolean isHand() {
        return game.isHand();
    }

    /**
     * Tells whether the player is asked to say a new value rather than to hold one.
     *
     * @return {@code true} if a new value is to be said
     */
    public boolean isAskingToSay() {
        return game.isAskingToSay();
    }

    /**
     * Returns the value in question during the bidding.
     *
     * @return the value to say or hold
     */
    public int pendingBidValue() {
        return game.pendingBidValue();
    }

    /**
     * Returns the cards played into the current trick.
     *
     * @return a copy in playing order
     */
    public List<Card> currentTrick() {
        return new ArrayList<>(game.currentTrick());
    }

    /**
     * Returns the seat that led the current trick.
     *
     * @return the leader
     */
    public Seat trickLeader() {
        return game.trickLeader();
    }

    /**
     * Returns all collected tricks.
     *
     * @return the tricks in playing order
     */
    public List<CompletedTrick> tricks() {
        return game.tricks();
    }

    /**
     * Returns all cards that were played openly so far, including the current trick.
     *
     * @return the played cards
     */
    public List<Card> playedCards() {
        List<Card> played = new ArrayList<>();
        for (CompletedTrick trick : game.tricks()) {
            played.addAll(trick.cards());
        }
        played.addAll(game.currentTrick());
        return played;
    }

    /**
     * Returns how many cards a seat still holds; everybody at the table can count that.
     *
     * @param other any seat
     * @return the number of cards in that hand
     */
    public int handSize(Seat other) {
        return game.hand(other).size();
    }

    /**
     * Returns the declarer's cards if they lie open on the table (Ouvert) and this player is an
     * opponent.
     *
     * @return the open cards; empty otherwise
     */
    public List<Card> openHand() {
        Declaration declaration = game.declaration();
        boolean playing = game.phase() == Phase.PLAYING || game.phase() == Phase.TRICK_COMPLETE;
        if (declaration != null && declaration.isOuvert() && playing && seat != game.declarer()) {
            return new ArrayList<>(game.hand(game.declarer()));
        }
        return Collections.emptyList();
    }

    /**
     * Returns the cards this player may play now.
     *
     * @return the playable cards
     */
    public List<Card> playableCards() {
        return game.playableCards();
    }
}
