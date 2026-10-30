package name.ulbricht.skat.ai;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import name.ulbricht.skat.engine.CompletedTrick;
import name.ulbricht.skat.engine.PlayerView;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.Rank;
import name.ulbricht.skat.model.Seat;
import name.ulbricht.skat.rules.CardOrder;
import name.ulbricht.skat.rules.GameResult;
import name.ulbricht.skat.rules.TrickRules;

/**
 * Plays a game to its end in one possible distribution of the cards, with quick rules of thumb
 * for every player, and rates the outcome for one player.
 */
final class Playout {

    /** Weight of the card points next to winning or losing (1). */
    private static final double POINTS_WEIGHT = 1.0 / 240;
    private static final double SCHNEIDER_BONUS = 0.25;

    private final GameType type;
    private final Declaration declaration;
    private final Seat declarer;
    private final Map<Seat, List<Card>> hands = new EnumMap<>(Seat.class);
    private final List<Card> trick;
    private final int skatPoints;
    private Seat leader;
    private Seat toPlay;
    private int declarerPoints;
    private int declarerTricks;
    private int opponentTricks;
    private boolean over;

    Playout(PlayerView view, Sampler.World world) {
        declaration = view.declaration();
        type = declaration.type();
        declarer = view.declarer();
        for (Seat seat : Seat.values()) {
            hands.put(seat, new ArrayList<>(world.hand(seat)));
        }
        trick = new ArrayList<>(view.currentTrick());
        leader = view.trickLeader();
        toPlay = view.seat();
        skatPoints = Card.totalPoints(world.skat());
        for (CompletedTrick done : view.tricks()) {
            if (done.winner() == declarer) {
                declarerPoints += done.points();
                declarerTricks++;
            } else {
                opponentTricks++;
            }
        }
    }

    /**
     * Plays {@code first} for the player on turn and then the rest of the game.
     *
     * @return how good the outcome is for that player; higher is better
     */
    double run(Card first) {
        Seat me = toPlay;
        play(first);
        while (!over) {
            play(choose(toPlay));
        }
        return me == declarer ? declarerValue() : -declarerValue();
    }

    private void play(Card card) {
        hands.get(toPlay).remove(card);
        trick.add(card);
        if (trick.size() < TrickRules.TRICK_SIZE) {
            toPlay = toPlay.next();
            return;
        }
        Seat winner = leader.after(TrickRules.winnerIndex(trick, type));
        if (winner == declarer) {
            declarerPoints += Card.totalPoints(trick);
            declarerTricks++;
        } else {
            opponentTricks++;
        }
        trick.clear();
        leader = winner;
        toPlay = winner;
        over = hands.get(winner).isEmpty() || (type == GameType.NULL && winner == declarer);
    }

    private double declarerValue() {
        if (type == GameType.NULL) {
            return declarerTricks == 0 ? 1 : 0;
        }
        int points = declarerPoints + skatPoints;
        boolean won = points >= GameResult.WINNING_POINTS;
        if (declaration.isSchneiderAnnounced()) {
            won = won && points >= GameResult.SCHNEIDER_POINTS;
        }
        if (declaration.isSchwarzAnnounced()) {
            won = won && opponentTricks == 0;
        }
        double value = (won ? 1 : 0) + points * POINTS_WEIGHT;
        if (points >= GameResult.SCHNEIDER_POINTS) {
            value += SCHNEIDER_BONUS;
        } else if (points <= GameResult.SCHNEIDER_LIMIT) {
            value -= SCHNEIDER_BONUS;
        }
        return value;
    }

    /**
     * Quick rules of thumb; every player sees only the own cards and the table. Leads are simpler
     * than in {@link SimplePlayer} (no memory); following and Null use its rules.
     */
    private Card choose(Seat seat) {
        List<Card> playable = TrickRules.playableCards(hands.get(seat), trick, type);
        if (playable.size() == 1) {
            return playable.get(0);
        }
        if (type == GameType.NULL) {
            return SimplePlayer.nullRule(playable, trick, type, seat == declarer).card();
        }
        if (!trick.isEmpty()) {
            return SimplePlayer.followRule(playable, trick, type, leader, seat, declarer).card();
        }
        List<Card> trumps = SimplePlayer.filter(playable, type, true);
        if (seat == declarer && !trumps.isEmpty()) {
            return SimplePlayer.strongest(trumps, type);
        }
        for (Card card : playable) {
            if (card.rank() == Rank.ACE && !CardOrder.isTrump(card, type)) {
                return card;
            }
        }
        return SimplePlayer.cheapest(playable, type);
    }
}
