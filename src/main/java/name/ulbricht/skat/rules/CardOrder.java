package name.ulbricht.skat.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.Rank;
import name.ulbricht.skat.model.Suit;

/**
 * Trump membership and card strength depending on the game type.
 *
 * <ul>
 *   <li>Suit games: the four Unter (acorns, leaves, hearts, bells) followed by ace, ten, king,
 *       Ober, 9, 8, 7 of the trump suit are trump.</li>
 *   <li>Grand: only the four Unter are trump.</li>
 *   <li>Null: there is no trump; each suit ranks ace, king, Ober, Unter, ten, 9, 8, 7.</li>
 * </ul>
 * Plain suits in suit games and Grand rank ace, ten, king, Ober, 9, 8, 7.
 */
public final class CardOrder {

    private static final List<Rank> PLAIN_ORDER = Collections.unmodifiableList(Arrays.asList(
            Rank.ACE, Rank.TEN, Rank.KING, Rank.OBER, Rank.NINE, Rank.EIGHT, Rank.SEVEN));
    private static final List<Rank> NULL_ORDER = Collections.unmodifiableList(Arrays.asList(
            Rank.ACE, Rank.KING, Rank.OBER, Rank.UNTER, Rank.TEN, Rank.NINE, Rank.EIGHT, Rank.SEVEN));
    private static final int UNTER_STRENGTH = 100;

    private CardOrder() {
    }

    /**
     * Tells whether a card is trump in the given game.
     *
     * @param card the card
     * @param type the game type
     * @return {@code true} if the card is trump
     */
    public static boolean isTrump(Card card, GameType type) {
        if (!type.hasTrump()) {
            return false;
        }
        return card.isUnter() || card.suit() == type.trumpSuit();
    }

    /**
     * Tells whether a card belongs to the group that a led card demands: trump if a trump was
     * led, otherwise a non-trump card of the led suit. Playing such a card is "zugeben".
     *
     * @param lead the first card of the trick
     * @param card the card to check
     * @param type the game type
     * @return {@code true} if the card follows the lead
     */
    public static boolean follows(Card lead, Card card, GameType type) {
        if (isTrump(lead, type)) {
            return isTrump(card, type);
        }
        return !isTrump(card, type) && card.suit() == lead.suit();
    }

    /**
     * Tells whether a card beats the card that currently wins a trick.
     *
     * @param challenger the newly played card
     * @param best the card that currently wins the trick
     * @param type the game type
     * @return {@code true} if the challenger takes over the trick
     */
    public static boolean beats(Card challenger, Card best, GameType type) {
        boolean challengerTrump = isTrump(challenger, type);
        boolean bestTrump = isTrump(best, type);
        if (challengerTrump != bestTrump) {
            return challengerTrump;
        }
        if (!challengerTrump && challenger.suit() != best.suit()) {
            return false;
        }
        return strength(challenger, type) > strength(best, type);
    }

    /**
     * Returns all trumps of the game from highest to lowest.
     *
     * @param type the game type
     * @return the trumps in descending order; empty for Null
     */
    public static List<Card> trumpsDescending(GameType type) {
        List<Card> trumps = new ArrayList<>();
        if (!type.hasTrump()) {
            return trumps;
        }
        for (Suit suit : Suit.values()) {
            trumps.add(Card.of(suit, Rank.UNTER));
        }
        if (type.isSuitGame()) {
            for (Rank rank : PLAIN_ORDER) {
                trumps.add(Card.of(type.trumpSuit(), rank));
            }
        }
        return trumps;
    }

    /**
     * Returns the strength of a card within its group (trump or its plain suit); higher is
     * stronger. Only cards of the same group are comparable, but every trump is stronger than
     * every plain card.
     *
     * @param card the card
     * @param type the game type
     * @return the strength
     */
    public static int strength(Card card, GameType type) {
        if (type == GameType.NULL) {
            return NULL_ORDER.size() - NULL_ORDER.indexOf(card.rank());
        }
        if (card.isUnter()) {
            return UNTER_STRENGTH + Suit.values().length - card.suit().ordinal();
        }
        return PLAIN_ORDER.size() - PLAIN_ORDER.indexOf(card.rank());
    }
}
