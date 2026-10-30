package name.ulbricht.skat.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Seat;

/**
 * A collected trick: its cards in playing order, who led and who won it.
 */
public final class CompletedTrick {

    private final List<Card> cards;
    private final Seat leader;
    private final Seat winner;

    CompletedTrick(List<Card> cards, Seat leader, Seat winner) {
        this.cards = Collections.unmodifiableList(new ArrayList<>(cards));
        this.leader = leader;
        this.winner = winner;
    }

    /**
     * Returns the cards in playing order.
     *
     * @return an unmodifiable list of three cards
     */
    public List<Card> cards() {
        return cards;
    }

    /**
     * Returns the seat that led the trick.
     *
     * @return the leader
     */
    public Seat leader() {
        return leader;
    }

    /**
     * Returns the seat that won the trick.
     *
     * @return the winner
     */
    public Seat winner() {
        return winner;
    }

    /**
     * Returns the seat that played the card at a position.
     *
     * @param index the position in playing order
     * @return the seat
     */
    public Seat playerOf(int index) {
        return leader.after(index);
    }

    /**
     * Returns the card points in this trick.
     *
     * @return the points
     */
    public int points() {
        return Card.totalPoints(cards);
    }
}
