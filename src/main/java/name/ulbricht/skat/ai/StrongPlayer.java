package name.ulbricht.skat.ai;

import java.util.List;
import java.util.Random;

import name.ulbricht.skat.engine.PlayerView;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Declaration;

/**
 * The strong computer player ("Stark"): bids and announces like {@link SimplePlayer}, but for
 * every card it could play it deals the unseen cards many times at random, plays each game to the
 * end and picks the card with the best average outcome. It never looks into other hands.
 */
final class StrongPlayer implements ComputerPlayer {

    private static final int SAMPLES = 200;
    /** At most this long per card, so the player never keeps the table waiting. */
    private static final long TIME_BUDGET_NANOS = 500_000_000L;

    private final SimplePlayer rules = new SimplePlayer();
    private final Random random;

    StrongPlayer(long seed) {
        random = new Random(seed);
    }

    @Override
    public boolean acceptBid(PlayerView view) {
        return rules.acceptBid(view);
    }

    @Override
    public boolean playHand(PlayerView view) {
        return rules.playHand(view);
    }

    @Override
    public List<Card> discard(PlayerView view) {
        return rules.discard(view);
    }

    @Override
    public Declaration declare(PlayerView view) {
        return rules.declare(view);
    }

    @Override
    public Card play(PlayerView view) {
        List<Card> playable = view.playableCards();
        Card fallback = rules.play(view);
        if (playable.size() == 1) {
            return fallback;
        }
        Sampler sampler = new Sampler(view);
        double[] totals = new double[playable.size()];
        long deadline = System.nanoTime() + TIME_BUDGET_NANOS;
        int samples = 0;
        while (samples < SAMPLES && System.nanoTime() < deadline) {
            Sampler.World world = sampler.sample(random);
            if (world == null) {
                break;
            }
            for (int i = 0; i < playable.size(); i++) {
                totals[i] += new Playout(view, world).run(playable.get(i));
            }
            samples++;
        }
        if (samples == 0) {
            return fallback;
        }
        // On a tie the rules of thumb decide.
        int best = playable.indexOf(fallback);
        for (int i = 0; i < playable.size(); i++) {
            if (totals[i] > totals[best] + 1e-9) {
                best = i;
            }
        }
        return playable.get(best);
    }
}
