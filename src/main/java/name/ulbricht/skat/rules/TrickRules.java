package name.ulbricht.skat.rules;

import java.util.ArrayList;
import java.util.List;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.GameType;

/**
 * Which cards may be played into a trick and who wins it.
 */
public final class TrickRules {

    /** Number of cards in a complete trick. */
    public static final int TRICK_SIZE = 3;

    private TrickRules() {
    }

    /**
     * Returns the cards of a hand that may legally be played.
     *
     * <p>A player must follow the led group ("zugeben"): trump on a trump lead, otherwise the
     * led suit without its trumps. Only if that is impossible, any card may be played.
     *
     * @param hand the cards in the player's hand
     * @param trick the cards already played into the current trick, in playing order
     * @param type the game type
     * @return the playable cards in hand order; never empty for a non-empty hand
     */
    public static List<Card> playableCards(List<Card> hand, List<Card> trick, GameType type) {
        if (trick.isEmpty()) {
            return new ArrayList<>(hand);
        }
        Card lead = trick.get(0);
        List<Card> following = new ArrayList<>();
        for (Card card : hand) {
            if (CardOrder.follows(lead, card, type)) {
                following.add(card);
            }
        }
        return following.isEmpty() ? new ArrayList<>(hand) : following;
    }

    /**
     * Returns the position of the winning card in a trick.
     *
     * @param trick the cards of the trick in playing order (at least one)
     * @param type the game type
     * @return the index of the winning card
     * @throws IllegalArgumentException if the trick is empty
     */
    public static int winnerIndex(List<Card> trick, GameType type) {
        if (trick.isEmpty()) {
            throw new IllegalArgumentException("Empty trick");
        }
        int winner = 0;
        for (int i = 1; i < trick.size(); i++) {
            if (CardOrder.beats(trick.get(i), trick.get(winner), type)) {
                winner = i;
            }
        }
        return winner;
    }
}
