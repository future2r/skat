package name.ulbricht.skat.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import name.ulbricht.skat.engine.PlayerView;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.Seat;
import name.ulbricht.skat.rules.CardOrder;

/**
 * Deals the cards a player has not seen to the other hands and the skat at random, in a way
 * that fits everything seen so far: hand sizes, open cards, and suits a player could not follow.
 * As the declarer usually holds many trumps, opponents put unseen trumps there more often.
 */
final class Sampler {

    /** One possible distribution of all cards. */
    static final class World {
        private final Map<Seat, List<Card>> hands;
        private final List<Card> skat;

        World(Map<Seat, List<Card>> hands, List<Card> skat) {
            this.hands = hands;
            this.skat = skat;
        }

        List<Card> hand(Seat seat) {
            return hands.get(seat);
        }

        List<Card> skat() {
            return skat;
        }
    }

    private static final int ATTEMPTS = 30;
    private static final int DECLARER_TRUMP_WEIGHT = 3;
    private static final int SKAT_SIZE = 2;

    private final Seat me;
    private final Seat declarer;
    private final GameType type;
    private final CardMemory memory;
    private final Map<Seat, List<Card>> fixed = new EnumMap<>(Seat.class);
    private final Map<Seat, Integer> capacity = new EnumMap<>(Seat.class);
    private final List<Card> knownSkat;
    private final int skatCapacity;

    Sampler(PlayerView view) {
        me = view.seat();
        declarer = view.declarer();
        type = view.declaration().type();
        memory = CardMemory.of(view);
        knownSkat = view.knownSkat();
        skatCapacity = knownSkat.isEmpty() ? SKAT_SIZE : 0;
        List<Card> open = view.openHand();
        for (Seat seat : Seat.values()) {
            List<Card> cards = new ArrayList<>();
            if (seat == me) {
                cards.addAll(view.hand());
            } else if (seat == declarer) {
                cards.addAll(open);
            }
            fixed.put(seat, cards);
            capacity.put(seat, view.handSize(seat) - cards.size());
        }
    }

    /** Returns a random distribution that fits all observations, or {@code null} if none was found. */
    World sample(Random random) {
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            World world = tryOnce(random);
            if (world != null) {
                return world;
            }
        }
        return null;
    }

    private World tryOnce(Random random) {
        Map<Seat, List<Card>> hands = new EnumMap<>(Seat.class);
        Map<Seat, Integer> free = new EnumMap<>(capacity);
        for (Seat seat : Seat.values()) {
            hands.put(seat, new ArrayList<>(fixed.get(seat)));
        }
        List<Card> skat = new ArrayList<>(knownSkat);
        int skatFree = skatCapacity;
        List<Card> cards = new ArrayList<>(memory.unseen());
        Collections.shuffle(cards, random);
        // Cards that fit into few places go first, so they do not end up without a place.
        cards.sort((a, b) -> Integer.compare(places(a), places(b)));
        for (Card card : cards) {
            int[] weights = new int[Seat.values().length + 1];
            int total = 0;
            for (Seat seat : Seat.values()) {
                if (seat != me && free.get(seat) > 0 && !memory.isVoid(seat, card)) {
                    boolean likely = seat == declarer && me != declarer && CardOrder.isTrump(card, type);
                    weights[seat.ordinal()] = free.get(seat) * (likely ? DECLARER_TRUMP_WEIGHT : 1);
                    total += weights[seat.ordinal()];
                }
            }
            weights[weights.length - 1] = skatFree;
            total += skatFree;
            if (total == 0) {
                return null;
            }
            int pick = random.nextInt(total);
            int index = 0;
            while (pick >= weights[index]) {
                pick -= weights[index];
                index++;
            }
            if (index == weights.length - 1) {
                skat.add(card);
                skatFree--;
            } else {
                Seat seat = Seat.values()[index];
                hands.get(seat).add(card);
                free.put(seat, free.get(seat) - 1);
            }
        }
        return new World(hands, skat);
    }

    private int places(Card card) {
        int places = skatCapacity > 0 ? 1 : 0;
        for (Seat seat : Seat.values()) {
            if (seat != me && capacity.get(seat) > 0 && !memory.isVoid(seat, card)) {
                places++;
            }
        }
        return places;
    }
}
