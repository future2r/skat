package name.ulbricht.skat.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import name.ulbricht.skat.engine.PlayerView;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.Rank;
import name.ulbricht.skat.model.Seat;
import name.ulbricht.skat.model.Suit;
import name.ulbricht.skat.rules.CardOrder;
import name.ulbricht.skat.rules.TrickRules;

/**
 * A straightforward computer player built on common rules of thumb. It never cheats: all
 * decisions use the {@link PlayerView} only.
 *
 * <p>Every decision is first made as an {@link Advice} with its reason, so the same rules give
 * the human player a tip.
 */
public final class SimplePlayer implements ComputerPlayer {

    private static final int VOID_BONUS = 5;
    private static final int KEEP_ACE = -100;
    private static final int TRICK_WORTH_WINNING = 10;

    @Override
    public boolean acceptBid(PlayerView view) {
        return adviseBid(view).accept();
    }

    @Override
    public boolean playHand(PlayerView view) {
        return !adviseSkat(view).accept();
    }

    @Override
    public List<Card> discard(PlayerView view) {
        return adviseDiscard(view).cards();
    }

    @Override
    public Declaration declare(PlayerView view) {
        return adviseDeclaration(view).declaration();
    }

    @Override
    public Card play(PlayerView view) {
        return advisePlay(view).card();
    }

    /**
     * Decides whether to say or hold the value in question.
     *
     * @param view the bidding player's view
     * @return the advice
     */
    public Advice adviseBid(PlayerView view) {
        int value = view.pendingBidValue();
        int max = HandEvaluator.maxBidWithHand(view.hand());
        if (max == 0) {
            return Advice.bid(false, Advice.Reason.BID_WEAK, 0);
        }
        boolean accept = value > 0 && value <= max;
        if (!accept) {
            return Advice.bid(false, Advice.Reason.BID_TOO_HIGH, max);
        }
        // Above the value with the skat, the bid only holds if the game is played hand.
        boolean handNeeded = value > HandEvaluator.maxBid(view.hand());
        return Advice.bid(true, handNeeded ? Advice.Reason.BID_REACHES_HAND : Advice.Reason.BID_REACHES, max);
    }

    /**
     * Decides whether to take up the skat: only very strong hands are played without it.
     *
     * @param view the declarer's view
     * @return the advice
     */
    public Advice adviseSkat(PlayerView view) {
        Declaration hand = HandEvaluator.handGame(view.hand());
        if (hand != null && HandEvaluator.value(view.hand(), hand) >= view.bid()) {
            return Advice.skat(false, Advice.Reason.PLAY_HAND);
        }
        return Advice.skat(true, Advice.Reason.TAKE_SKAT);
    }

    /**
     * Chooses two cards to put away.
     *
     * @param view the declarer's view with twelve cards in hand
     * @return the advice
     */
    public Advice adviseDiscard(PlayerView view) {
        List<Card> cards = view.hand();
        GameType type = HandEvaluator.preferredGame(cards);
        if (type == null) {
            type = HandEvaluator.bestSuitGame(cards);
        }
        List<Card> candidates = new ArrayList<>(cards);
        if (type == GameType.NULL) {
            GameType nullType = type;
            candidates.sort(Comparator.comparingInt((Card c) -> CardOrder.strength(c, nullType)).reversed());
            List<Card> chosen = new ArrayList<>(candidates.subList(0, 2));
            return Advice.discard(chosen, Advice.Reason.DISCARD_NULL, Card.totalPoints(chosen), null);
        }
        GameType game = type;
        candidates.sort(Comparator.comparingInt((Card c) -> discardScore(c, cards, game)).reversed());
        List<Card> chosen = new ArrayList<>(candidates.subList(0, 2));
        Suit given = suitGivenUp(cards, chosen, game);
        Advice.Reason reason = given != null ? Advice.Reason.DISCARD_VOID : Advice.Reason.DISCARD_POINTS;
        return Advice.discard(chosen, reason, Card.totalPoints(chosen), given);
    }

    /** Higher means better to put away: points of plain cards, creating voids, never aces or trumps. */
    private static int discardScore(Card card, List<Card> cards, GameType type) {
        if (CardOrder.isTrump(card, type)) {
            return KEEP_ACE * 2 - CardOrder.strength(card, type);
        }
        if (card.rank() == Rank.ACE) {
            return KEEP_ACE;
        }
        return card.points() + (plainCardsOf(cards, card.suit(), type) == 1 ? VOID_BONUS : 0);
    }

    /** Returns the plain suit that no longer appears in the hand after putting the cards away. */
    private static Suit suitGivenUp(List<Card> cards, List<Card> putAway, GameType type) {
        for (Card card : putAway) {
            if (!CardOrder.isTrump(card, type)
                    && plainCardsOf(cards, card.suit(), type) == plainCardsOf(putAway, card.suit(), type)) {
                return card.suit();
            }
        }
        return null;
    }

    private static int plainCardsOf(List<Card> cards, Suit suit, GameType type) {
        int count = 0;
        for (Card card : cards) {
            if (!CardOrder.isTrump(card, type) && card.suit() == suit) {
                count++;
            }
        }
        return count;
    }

    /**
     * Chooses the game to announce.
     *
     * @param view the declarer's view
     * @return the advice
     */
    public Advice adviseDeclaration(PlayerView view) {
        List<Card> cards = new ArrayList<>(view.hand());
        cards.addAll(view.knownSkat());
        boolean hand = view.isHand();
        int bid = view.bid();
        if (hand) {
            Declaration handGame = HandEvaluator.handGame(cards);
            if (handGame != null && HandEvaluator.value(cards, handGame) >= bid) {
                if (handGame.isOuvert()) {
                    return Advice.declare(handGame, Advice.Reason.DECLARE_NULL_OUVERT, 0);
                }
                return declarationAdvice(cards, handGame.type(), true, false);
            }
        }
        GameType preferred = HandEvaluator.preferredGame(cards);
        if (preferred != null && reaches(cards, preferred, hand, bid)) {
            return declarationAdvice(cards, preferred, hand, false);
        }
        GameType best = null;
        double bestStrength = -1;
        for (GameType type : GameType.values()) {
            if (type == GameType.NULL || !reaches(cards, type, hand, bid)) {
                continue;
            }
            double strength = HandEvaluator.strength(cards, type);
            if (strength > bestStrength) {
                best = type;
                bestStrength = strength;
            }
        }
        if (best == null) {
            return declarationAdvice(cards, HandEvaluator.bestSuitGame(cards), hand, true);
        }
        return declarationAdvice(cards, best, hand, false);
    }

    private static Advice declarationAdvice(List<Card> cards, GameType type, boolean hand, boolean fallback) {
        Declaration declaration = hand ? Declaration.hand(type) : Declaration.of(type);
        if (type == GameType.NULL) {
            return Advice.declare(declaration, Advice.Reason.DECLARE_NULL, 0);
        }
        int trumps = 0;
        for (Card card : cards) {
            if (CardOrder.isTrump(card, type)) {
                trumps++;
            }
        }
        Advice.Reason reason;
        if (fallback) {
            reason = Advice.Reason.DECLARE_FALLBACK;
        } else {
            reason = type == GameType.GRAND ? Advice.Reason.DECLARE_GRAND : Advice.Reason.DECLARE_SUIT;
        }
        return Advice.declare(declaration, reason, trumps);
    }

    private static boolean reaches(List<Card> cards, GameType type, boolean hand, int bid) {
        Declaration declaration = hand ? Declaration.hand(type) : Declaration.of(type);
        return HandEvaluator.value(cards, declaration) >= bid;
    }

    /**
     * Chooses the card to play.
     *
     * @param view the view of the player on turn
     * @return the advice
     */
    public Advice advisePlay(PlayerView view) {
        List<Card> playable = view.playableCards();
        if (playable.size() == 1) {
            return Advice.play(playable.get(0), Advice.Reason.ONLY_CARD);
        }
        GameType type = view.declaration().type();
        List<Card> trick = view.currentTrick();
        if (type == GameType.NULL) {
            return playNull(view, playable, trick, type);
        }
        if (trick.isEmpty()) {
            return lead(view, playable, type);
        }
        return follow(view, playable, trick, type);
    }

    private static Advice lead(PlayerView view, List<Card> playable, GameType type) {
        List<Card> trumps = filter(playable, type, true);
        List<Card> plain = filter(playable, type, false);
        CardMemory memory = CardMemory.of(view);
        if (view.isDeclarer() && !trumps.isEmpty() && memory.opponentMayHoldTrump(view.seat(), view.declarer())) {
            return Advice.play(strongest(trumps, type), Advice.Reason.DRAW_TRUMPS);
        }
        for (Card card : plain) {
            if (card.rank() == Rank.ACE && !canBeTrumped(view, memory, card)) {
                return Advice.play(card, Advice.Reason.LEAD_ACE);
            }
        }
        for (Card card : plain) {
            if (card.rank() == Rank.TEN && memory.isHighest(card) && !canBeTrumped(view, memory, card)) {
                return Advice.play(card, Advice.Reason.LEAD_TEN_HIGH);
            }
        }
        return Advice.play(cheapest(plain.isEmpty() ? trumps : plain, type), Advice.Reason.LEAD_LOW);
    }

    /**
     * Tells whether an opponent of the leading player has shown to have no card of this suit and
     * may still hold a trump, so a high card led there could be lost.
     */
    private static boolean canBeTrumped(PlayerView view, CardMemory memory, Card card) {
        if (memory.unseenTrumps() == 0) {
            return false;
        }
        for (Seat seat : Seat.values()) {
            boolean opponent = view.isDeclarer() ? seat != view.seat() : seat == view.declarer();
            if (opponent && memory.isVoid(seat, card) && !memory.isTrumpVoid(seat)) {
                return true;
            }
        }
        return false;
    }

    private static Advice follow(PlayerView view, List<Card> playable, List<Card> trick, GameType type) {
        return followRule(playable, trick, type, view.trickLeader(), view.seat(), view.declarer());
    }

    /**
     * The rules of thumb for a card into a led trick, without memory; also used by the playouts of
     * the strong player.
     */
    static Advice followRule(List<Card> playable, List<Card> trick, GameType type, Seat leader, Seat player,
            Seat declarer) {
        if (playable.size() == 1) {
            return Advice.play(playable.get(0), Advice.Reason.ONLY_CARD);
        }
        int winnerIndex = TrickRules.winnerIndex(trick, type);
        Card best = trick.get(winnerIndex);
        boolean isDeclarer = player == declarer;
        boolean partnerWinning = !isDeclarer && leader.after(winnerIndex) != declarer;
        boolean lastToPlay = trick.size() == TrickRules.TRICK_SIZE - 1;
        if (partnerWinning) {
            boolean safe = lastToPlay || CardOrder.isTrump(best, type) || best.rank() == Rank.ACE;
            return safe ? Advice.play(mostPoints(playable, type), Advice.Reason.SMEAR)
                    : Advice.play(cheapest(playable, type), Advice.Reason.PARTNER_AHEAD);
        }
        List<Card> winners = new ArrayList<>();
        for (Card card : playable) {
            if (CardOrder.beats(card, best, type)) {
                winners.add(card);
            }
        }
        if (winners.isEmpty()) {
            return Advice.play(cheapest(playable, type), Advice.Reason.CANNOT_WIN);
        }
        boolean worthIt = lastToPlay || isDeclarer || Card.totalPoints(trick) >= TRICK_WORTH_WINNING;
        if (worthIt) {
            return Advice.play(cheapest(winners, type), Advice.Reason.WIN_CHEAPLY);
        }
        return Advice.play(cheapest(playable, type), Advice.Reason.NOT_WORTH);
    }

    private static Advice playNull(PlayerView view, List<Card> playable, List<Card> trick, GameType type) {
        return nullRule(playable, trick, type, view.isDeclarer());
    }

    /** The rules of thumb for a Null game; also used by the playouts of the strong player. */
    static Advice nullRule(List<Card> playable, List<Card> trick, GameType type, boolean isDeclarer) {
        Comparator<Card> byStrength = Comparator.comparingInt(c -> CardOrder.strength(c, type));
        Card lowest = playable.stream().min(byStrength).orElseThrow(IllegalStateException::new);
        if (!isDeclarer) {
            return Advice.play(lowest, Advice.Reason.NULL_OPPONENT_LOW);
        }
        if (trick.isEmpty()) {
            return Advice.play(lowest, Advice.Reason.NULL_LEAD_LOW);
        }
        Card best = trick.get(TrickRules.winnerIndex(trick, type));
        Card safeHighest = null;
        for (Card card : playable) {
            if (!CardOrder.beats(card, best, type)
                    && (safeHighest == null || byStrength.compare(card, safeHighest) > 0)) {
                safeHighest = card;
            }
        }
        if (safeHighest != null) {
            return Advice.play(safeHighest, Advice.Reason.NULL_STAY_BELOW);
        }
        return Advice.play(lowest, Advice.Reason.NULL_CANNOT_STAY_BELOW);
    }

    static List<Card> filter(List<Card> cards, GameType type, boolean trump) {
        List<Card> result = new ArrayList<>();
        for (Card card : cards) {
            if (CardOrder.isTrump(card, type) == trump) {
                result.add(card);
            }
        }
        return result;
    }

    /** The card that costs least: few points, plain before trump, weak before strong. */
    static Card cheapest(List<Card> cards, GameType type) {
        return cards.stream().min(costOrder(type)).orElseThrow(IllegalStateException::new);
    }

    private static Card mostPoints(List<Card> cards, GameType type) {
        return cards.stream().max(Comparator.comparingInt(Card::points)
                .thenComparing(costOrder(type).reversed())).orElseThrow(IllegalStateException::new);
    }

    static Card strongest(List<Card> cards, GameType type) {
        return cards.stream().max(Comparator.comparingInt(c -> CardOrder.strength(c, type)))
                .orElseThrow(IllegalStateException::new);
    }

    private static Comparator<Card> costOrder(GameType type) {
        return Comparator.comparingInt(Card::points)
                .thenComparingInt(c -> CardOrder.isTrump(c, type) ? 1 : 0)
                .thenComparingInt(c -> CardOrder.strength(c, type));
    }
}
