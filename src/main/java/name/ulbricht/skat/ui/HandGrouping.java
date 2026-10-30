package name.ulbricht.skat.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.Suit;
import name.ulbricht.skat.rules.CardOrder;

/**
 * Sorts a hand into labelled groups as shown on screen: trump first, then the plain suits in
 * the order acorns, leaves, hearts, bells, each from high to low.
 */
final class HandGrouping {

    /** What a group contains; decides its label. */
    enum Kind { TRUMP, UNTER, SUIT }

    /** One labelled group of cards. */
    static final class CardGroup {

        private final Kind kind;
        private final Suit suit;
        private final List<Card> cards;

        CardGroup(Kind kind, Suit suit, List<Card> cards) {
            this.kind = kind;
            this.suit = suit;
            this.cards = Collections.unmodifiableList(new ArrayList<>(cards));
        }

        Kind kind() {
            return kind;
        }

        /** Returns the suit of a {@link Kind#SUIT} group, or {@code null}. */
        Suit suit() {
            return suit;
        }

        List<Card> cards() {
            return cards;
        }
    }

    private HandGrouping() {
    }

    /**
     * Groups a hand.
     *
     * @param hand the cards
     * @param type the announced game, or {@code null} before the announcement (then the Unter
     *             form their own group as in most games)
     */
    static List<CardGroup> group(List<Card> hand, GameType type) {
        GameType order = type == null ? GameType.GRAND : type;
        List<CardGroup> groups = new ArrayList<>();
        List<Card> trumps = new ArrayList<>();
        for (Card card : hand) {
            if (CardOrder.isTrump(card, order)) {
                trumps.add(card);
            }
        }
        if (!trumps.isEmpty()) {
            trumps.sort(strongestFirst(order));
            Kind kind = type == null ? Kind.UNTER : Kind.TRUMP;
            groups.add(new CardGroup(kind, null, trumps));
        }
        for (Suit suit : Suit.values()) {
            List<Card> cards = new ArrayList<>();
            for (Card card : hand) {
                if (card.suit() == suit && !CardOrder.isTrump(card, order)) {
                    cards.add(card);
                }
            }
            if (!cards.isEmpty()) {
                cards.sort(strongestFirst(order));
                groups.add(new CardGroup(Kind.SUIT, suit, cards));
            }
        }
        return groups;
    }

    private static Comparator<Card> strongestFirst(GameType type) {
        return Comparator.comparingInt((Card c) -> CardOrder.strength(c, type)).reversed();
    }
}
