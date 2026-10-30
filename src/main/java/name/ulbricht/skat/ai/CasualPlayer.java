package name.ulbricht.skat.ai;

import java.util.List;
import java.util.Random;

import name.ulbricht.skat.engine.PlayerView;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Declaration;

/**
 * The relaxed computer player ("Gemütlich"): follows the same rules of thumb as
 * {@link SimplePlayer}, but never plays hand, sometimes stops bidding early and now and then
 * lays a careless card.
 */
final class CasualPlayer implements ComputerPlayer {

    /** How often a careless card is played when there is a choice. */
    private static final double CARELESS = 0.3;
    /** How often the bidding stops before the cards' limit. */
    private static final double SHY = 0.25;

    private final SimplePlayer rules = new SimplePlayer();
    private final Random random;

    CasualPlayer(long seed) {
        random = new Random(seed);
    }

    /** Bids only up to the value with the skat, as this player never plays hand. */
    @Override
    public boolean acceptBid(PlayerView view) {
        int value = view.pendingBidValue();
        return value > 0 && value <= HandEvaluator.maxBid(view.hand()) && random.nextDouble() >= SHY;
    }

    @Override
    public boolean playHand(PlayerView view) {
        return false;
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
        if (playable.size() > 1 && random.nextDouble() < CARELESS) {
            return playable.get(random.nextInt(playable.size()));
        }
        return rules.play(view);
    }
}
