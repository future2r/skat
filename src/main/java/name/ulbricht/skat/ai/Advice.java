package name.ulbricht.skat.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.Suit;

/**
 * A decision suggested by a computer player together with the rule of thumb behind it. The
 * computer players act on it, and the "Tipp" button shows it to the human player.
 */
public final class Advice {

    /** The rule of thumb that led to a decision. */
    public enum Reason {
        /** Accept the bid: the hand reaches at least {@link #number()}. */
        BID_REACHES,
        /** Accept the bid, which the hand only reaches ({@link #number()}) when played without the skat. */
        BID_REACHES_HAND,
        /** Pass: the hand only reaches {@link #number()}. */
        BID_TOO_HIGH,
        /** Pass: the hand is too weak for any game. */
        BID_WEAK,
        /** Take up the skat: two more cards make the game safer. */
        TAKE_SKAT,
        /** Play hand: the cards are strong enough without the skat. */
        PLAY_HAND,
        /** Put away cards so that no card of {@link #suit()} is left. */
        DISCARD_VOID,
        /** Put away cards whose {@link #number()} points then count safely. */
        DISCARD_POINTS,
        /** Put away the highest cards for a Null game. */
        DISCARD_NULL,
        /** A suit game with {@link #number()} trumps. */
        DECLARE_SUIT,
        /** Grand with {@link #number()} Unter. */
        DECLARE_GRAND,
        /** Null with low cards. */
        DECLARE_NULL,
        /** Null ouvert: no card can take a trick, whatever the others hold. */
        DECLARE_NULL_OUVERT,
        /** No game safely reaches the bid; the suit game with the most trumps ({@link #number()}). */
        DECLARE_FALLBACK,
        /** Only one card may be played. */
        ONLY_CARD,
        /** The declarer draws the opponents' trumps. */
        DRAW_TRUMPS,
        /** Lead an ace for 11 points. */
        LEAD_ACE,
        /** Lead a ten whose ace has already been played. */
        LEAD_TEN_HIGH,
        /** Lead a card that costs little. */
        LEAD_LOW,
        /** The partner surely wins the trick: give many points. */
        SMEAR,
        /** The partner is ahead, but the trick is not safe yet: give little. */
        PARTNER_AHEAD,
        /** Win the trick with the cheapest card that does it. */
        WIN_CHEAPLY,
        /** The trick is worth little: keep the strong cards. */
        NOT_WORTH,
        /** The trick cannot be won: give as few points as possible. */
        CANNOT_WIN,
        /** Null declarer leads: the lowest card. */
        NULL_LEAD_LOW,
        /** Null opponent: play low so that the declarer may have to go over. */
        NULL_OPPONENT_LOW,
        /** Null declarer: get rid of the highest card that still stays below the trick. */
        NULL_STAY_BELOW,
        /** Null declarer cannot stay below: play the lowest card. */
        NULL_CANNOT_STAY_BELOW
    }

    private final Reason reason;
    private final boolean accept;
    private final List<Card> cards;
    private final Declaration declaration;
    private final int number;
    private final Suit suit;

    private Advice(Reason reason, boolean accept, List<Card> cards, Declaration declaration, int number, Suit suit) {
        this.reason = reason;
        this.accept = accept;
        this.cards = Collections.unmodifiableList(new ArrayList<>(cards));
        this.declaration = declaration;
        this.number = number;
        this.suit = suit;
    }

    /**
     * Advice for the bidding.
     *
     * @param accept whether to say or hold the value in question
     * @param reason why
     * @param maxBid the value the hand reaches
     * @return the advice
     */
    public static Advice bid(boolean accept, Reason reason, int maxBid) {
        return new Advice(reason, accept, Collections.emptyList(), null, maxBid, null);
    }

    /**
     * Advice whether to take up the skat.
     *
     * @param take {@code true} to take up the skat, {@code false} to play hand
     * @param reason why
     * @return the advice
     */
    public static Advice skat(boolean take, Reason reason) {
        return new Advice(reason, take, Collections.emptyList(), null, 0, null);
    }

    /**
     * Advice which two cards to put away.
     *
     * @param cards the two cards
     * @param reason why
     * @param points the points of the two cards
     * @param suit the suit that is given up completely, or {@code null}
     * @return the advice
     */
    public static Advice discard(List<Card> cards, Reason reason, int points, Suit suit) {
        return new Advice(reason, true, cards, null, points, suit);
    }

    /**
     * Advice which game to announce.
     *
     * @param declaration the game
     * @param reason why
     * @param count the number of trumps or Unter the reason refers to
     * @return the advice
     */
    public static Advice declare(Declaration declaration, Reason reason, int count) {
        return new Advice(reason, true, Collections.emptyList(), declaration, count, null);
    }

    /**
     * Advice which card to play.
     *
     * @param card the card
     * @param reason why
     * @return the advice
     */
    public static Advice play(Card card, Reason reason) {
        return new Advice(reason, true, Collections.singletonList(card), null, 0, null);
    }

    /**
     * Returns the rule of thumb behind the decision.
     *
     * @return the reason
     */
    public Reason reason() {
        return reason;
    }

    /**
     * Tells whether to accept: say or hold a bid, or take up the skat.
     *
     * @return {@code true} to accept
     */
    public boolean accept() {
        return accept;
    }

    /**
     * Returns the suggested cards: two to put away, or one to play.
     *
     * @return the cards; empty for other decisions
     */
    public List<Card> cards() {
        return cards;
    }

    /**
     * Returns the suggested card to play.
     *
     * @return the card
     * @throws IllegalStateException if this is no advice for playing a card
     */
    public Card card() {
        if (cards.size() != 1) {
            throw new IllegalStateException("No single card advised");
        }
        return cards.get(0);
    }

    /**
     * Returns the suggested game.
     *
     * @return the declaration, or {@code null} for other decisions
     */
    public Declaration declaration() {
        return declaration;
    }

    /**
     * Returns the number the reason refers to: a bid value, points, or a count of trumps or Unter.
     *
     * @return the number, 0 if the reason has none
     */
    public int number() {
        return number;
    }

    /**
     * Returns the suit the reason refers to.
     *
     * @return the suit, or {@code null}
     */
    public Suit suit() {
        return suit;
    }
}
