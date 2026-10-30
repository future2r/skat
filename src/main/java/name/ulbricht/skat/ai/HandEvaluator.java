package name.ulbricht.skat.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.Rank;
import name.ulbricht.skat.model.Suit;
import name.ulbricht.skat.rules.CardOrder;
import name.ulbricht.skat.rules.GameValue;
import name.ulbricht.skat.rules.Matadors;

/**
 * Rates a hand with simple, well-known rules of thumb: count trumps and side aces for suit
 * games, Unter and aces for Grand, and look for low cards in every suit for Null.
 */
public final class HandEvaluator {

    /** Strength a suit game needs with ten cards before seeing the skat. */
    private static final double SUIT_BID_THRESHOLD = 6.5;
    /** Strength a Grand needs before seeing the skat. */
    private static final double GRAND_BID_THRESHOLD = 5.0;
    private static final int GRAND_MIN_UNTER = 2;
    private static final double TEN_WITH_ACE = 0.5;
    private static final double TOP_UNTER_BONUS = 0.5;
    /** Extra for the ace and the ten of the trump suit. */
    private static final double FULL_TRUMP_BONUS = 0.25;
    /** Strength a suit game needs to be played without the skat. */
    private static final double SUIT_HAND_THRESHOLD = 9.0;
    /** Strength a Grand needs to be played without the skat. */
    private static final double GRAND_HAND_THRESHOLD = 7.0;
    private static final int GRAND_HAND_MIN_UNTER = 3;

    private HandEvaluator() {
    }

    /**
     * Rates a suit game or Grand: trumps (the ace and ten of the trump suit a little more, as they
     * win tricks and bring many points themselves), side aces, protected side tens and the top Unter.
     *
     * @param cards the cards to rate
     * @param type a suit game or Grand
     * @return the strength; about 7 is a solid game
     */
    public static double strength(Collection<Card> cards, GameType type) {
        double strength = 0;
        Set<Suit> sideAces = EnumSet.noneOf(Suit.class);
        for (Card card : cards) {
            if (CardOrder.isTrump(card, type)) {
                strength += 1;
                boolean fullTrump = card.rank() == Rank.ACE || card.rank() == Rank.TEN;
                if (fullTrump && !card.isUnter()) {
                    strength += FULL_TRUMP_BONUS;
                }
            } else if (card.rank() == Rank.ACE) {
                strength += 1;
                sideAces.add(card.suit());
            }
        }
        for (Card card : cards) {
            if (!CardOrder.isTrump(card, type) && card.rank() == Rank.TEN && sideAces.contains(card.suit())) {
                strength += TEN_WITH_ACE;
            }
        }
        if (cards.contains(Card.of(Suit.ACORNS, Rank.UNTER))) {
            strength += TOP_UNTER_BONUS;
        }
        return strength;
    }

    /**
     * Returns the suit game with the most trumps, preferring the higher base value on ties.
     *
     * @param cards the cards to rate
     * @return the best suit game
     */
    public static GameType bestSuitGame(Collection<Card> cards) {
        GameType best = GameType.ACORNS;
        double bestStrength = -1;
        for (Suit suit : Suit.values()) {
            GameType type = GameType.suitGame(suit);
            double strength = strength(cards, type);
            if (strength > bestStrength) {
                best = type;
                bestStrength = strength;
            }
        }
        return best;
    }

    /**
     * Tells whether a Null game looks safe: no ace or king, and every held suit contains its
     * seven or eight.
     *
     * @param cards the cards to rate
     * @return {@code true} if Null is a reasonable game
     */
    public static boolean isNullCandidate(Collection<Card> cards) {
        Set<Suit> held = EnumSet.noneOf(Suit.class);
        Set<Suit> covered = EnumSet.noneOf(Suit.class);
        for (Card card : cards) {
            if (card.rank() == Rank.ACE || card.rank() == Rank.KING) {
                return false;
            }
            held.add(card.suit());
            if (card.rank() == Rank.SEVEN || card.rank() == Rank.EIGHT) {
                covered.add(card.suit());
            }
        }
        return covered.containsAll(held);
    }

    /**
     * Tells whether a hand cannot take a single trick in a Null game, whatever the others hold:
     * in every suit the k-th lowest card is at most the (2k-1)-th lowest of the suit, so there is
     * always a lower card of the own to answer with.
     *
     * @param cards the cards to rate
     * @return {@code true} if Null ouvert is safe
     */
    public static boolean isSafeNull(Collection<Card> cards) {
        for (Suit suit : Suit.values()) {
            List<Integer> ranks = new ArrayList<>();
            for (Card card : cards) {
                if (card.suit() == suit) {
                    ranks.add(CardOrder.strength(card, GameType.NULL) - 1);
                }
            }
            Collections.sort(ranks);
            for (int i = 0; i < ranks.size(); i++) {
                if (ranks.get(i) > 2 * i) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Chooses a game strong enough to play without the skat: Null ouvert if no trick can be
     * taken, a very strong suit game or Grand otherwise.
     *
     * @param cards the ten cards before the skat
     * @return the hand game, or {@code null} if the skat should be taken
     */
    public static Declaration handGame(Collection<Card> cards) {
        if (isSafeNull(cards)) {
            return new Declaration(GameType.NULL, true, false, false, true);
        }
        int unter = 0;
        for (Card card : cards) {
            if (card.isUnter()) {
                unter++;
            }
        }
        if (unter >= GRAND_HAND_MIN_UNTER && strength(cards, GameType.GRAND) >= GRAND_HAND_THRESHOLD) {
            return Declaration.hand(GameType.GRAND);
        }
        GameType suit = bestSuitGame(cards);
        if (strength(cards, suit) >= SUIT_HAND_THRESHOLD) {
            return Declaration.hand(suit);
        }
        return null;
    }

    /**
     * Chooses the game a player would aim for with these cards.
     *
     * @param cards ten or twelve cards
     * @return the preferred game type, or {@code null} if no game is strong enough
     */
    public static GameType preferredGame(Collection<Card> cards) {
        int unter = 0;
        for (Card card : cards) {
            if (card.isUnter()) {
                unter++;
            }
        }
        if (unter >= GRAND_MIN_UNTER && strength(cards, GameType.GRAND) >= GRAND_BID_THRESHOLD) {
            return GameType.GRAND;
        }
        GameType suit = bestSuitGame(cards);
        if (strength(cards, suit) >= SUIT_BID_THRESHOLD) {
            return suit;
        }
        if (isNullCandidate(cards)) {
            return GameType.NULL;
        }
        return null;
    }

    /**
     * Returns the highest value a player should bid when taking up the skat.
     *
     * @param cards the ten cards before the skat
     * @return the maximum bid, or 0 to pass at once
     */
    public static int maxBid(Collection<Card> cards) {
        GameType type = preferredGame(cards);
        return type == null ? 0 : value(cards, Declaration.of(type));
    }

    /**
     * Returns the highest value a player should bid who will play hand when the cards are strong
     * enough for it ({@link #handGame}); that value only holds without taking up the skat.
     *
     * @param cards the ten cards before the skat
     * @return the maximum bid including a hand game, or 0 to pass at once
     */
    public static int maxBidWithHand(Collection<Card> cards) {
        int max = maxBid(cards);
        Declaration hand = handGame(cards);
        return hand == null ? max : Math.max(max, value(cards, hand));
    }

    /**
     * Returns the guaranteed value of a declaration with the given cards.
     *
     * @param cards the declarer's cards (matadors are counted from them)
     * @param declaration the game
     * @return the value without Schneider or Schwarz
     */
    public static int value(Collection<Card> cards, Declaration declaration) {
        if (declaration.type() == GameType.NULL) {
            return GameValue.nullValue(declaration);
        }
        return GameValue.guaranteed(declaration, Matadors.of(cards, declaration.type()).count());
    }
}
