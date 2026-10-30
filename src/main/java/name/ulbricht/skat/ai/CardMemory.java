package name.ulbricht.skat.ai;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import name.ulbricht.skat.engine.CompletedTrick;
import name.ulbricht.skat.engine.PlayerView;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.Seat;
import name.ulbricht.skat.model.Suit;
import name.ulbricht.skat.rules.CardOrder;

/**
 * What a careful player remembers during the play: which cards are still unseen, and who could
 * not follow a suit or trump. Built from a {@link PlayerView} only.
 */
final class CardMemory {

    private final GameType type;
    private final List<Card> unseen = new ArrayList<>();
    private final Map<Seat, Set<Suit>> suitVoids = new EnumMap<>(Seat.class);
    private final Set<Seat> trumpVoids = EnumSet.noneOf(Seat.class);

    private CardMemory(GameType type) {
        this.type = type;
        for (Seat seat : Seat.values()) {
            suitVoids.put(seat, EnumSet.noneOf(Suit.class));
        }
    }

    /** Remembers everything the player of the view has seen so far. */
    static CardMemory of(PlayerView view) {
        CardMemory memory = new CardMemory(view.declaration().type());
        Set<Card> seen = new HashSet<>(view.hand());
        seen.addAll(view.playedCards());
        seen.addAll(view.knownSkat());
        seen.addAll(view.openHand());
        for (Card card : Card.all()) {
            if (!seen.contains(card)) {
                memory.unseen.add(card);
            }
        }
        for (CompletedTrick trick : view.tricks()) {
            memory.noteVoids(trick.cards(), trick.leader());
        }
        memory.noteVoids(view.currentTrick(), view.trickLeader());
        return memory;
    }

    private void noteVoids(List<Card> trick, Seat leader) {
        if (trick.isEmpty()) {
            return;
        }
        Card lead = trick.get(0);
        Seat seat = leader;
        for (int i = 1; i < trick.size(); i++) {
            seat = seat.next();
            if (!CardOrder.follows(lead, trick.get(i), type)) {
                if (CardOrder.isTrump(lead, type)) {
                    trumpVoids.add(seat);
                } else {
                    suitVoids.get(seat).add(lead.suit());
                }
            }
        }
    }

    /** The cards this player has not seen: in the other hands or in an unknown skat. */
    List<Card> unseen() {
        return unseen;
    }

    /** Tells whether a seat has shown that it holds no card of the group of {@code card}. */
    boolean isVoid(Seat seat, Card card) {
        if (CardOrder.isTrump(card, type)) {
            return trumpVoids.contains(seat);
        }
        return suitVoids.get(seat).contains(card.suit());
    }

    /** Tells whether a seat has shown that it holds no trump. */
    boolean isTrumpVoid(Seat seat) {
        return trumpVoids.contains(seat);
    }

    /**
     * Tells whether an opponent of {@code player} may still hold a trump: some trump is unseen and
     * not every opponent has shown to have none. In a hand game unseen trumps may also lie in the
     * skat, so only the opponents' voids settle it.
     */
    boolean opponentMayHoldTrump(Seat player, Seat declarer) {
        if (unseenTrumps() == 0) {
            return false;
        }
        for (Seat seat : Seat.values()) {
            boolean opponent = player == declarer ? seat != player : seat == declarer;
            if (opponent && !trumpVoids.contains(seat)) {
                return true;
            }
        }
        return false;
    }

    /** Counts the unseen trumps. */
    int unseenTrumps() {
        int count = 0;
        for (Card card : unseen) {
            if (CardOrder.isTrump(card, type)) {
                count++;
            }
        }
        return count;
    }

    /** Counts the unseen cards of the group (trump or plain suit) of {@code card}. */
    int unseenOfGroup(Card card) {
        int count = 0;
        for (Card other : unseen) {
            if (CardOrder.follows(card, other, type)) {
                count++;
            }
        }
        return count;
    }

    /** Tells whether no unseen card of the same group beats {@code card}. */
    boolean isHighest(Card card) {
        for (Card other : unseen) {
            if (CardOrder.follows(card, other, type) && CardOrder.beats(other, card, type)) {
                return false;
            }
        }
        return true;
    }
}
