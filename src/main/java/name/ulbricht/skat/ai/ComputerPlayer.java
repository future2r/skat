package name.ulbricht.skat.ai;

import java.util.List;

import name.ulbricht.skat.engine.PlayerView;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Declaration;

/**
 * Decisions of a computer player. Every method only sees the player's own {@link PlayerView}.
 */
public interface ComputerPlayer {

    /**
     * Decides during the bidding whether to say or hold the value in question.
     *
     * @param view the player's view
     * @return {@code true} to say or hold, {@code false} to pass
     */
    boolean acceptBid(PlayerView view);

    /**
     * Decides after winning the bidding whether to play without the skat.
     *
     * @param view the declarer's view
     * @return {@code true} to play hand
     */
    boolean playHand(PlayerView view);

    /**
     * Chooses the two cards to put away after taking up the skat.
     *
     * @param view the declarer's view; the hand holds twelve cards
     * @return two different cards from the hand
     */
    List<Card> discard(PlayerView view);

    /**
     * Chooses the game to announce.
     *
     * @param view the declarer's view
     * @return a declaration matching the hand decision
     */
    Declaration declare(PlayerView view);

    /**
     * Chooses the card to play.
     *
     * @param view the player's view; the player is on turn
     * @return one of the playable cards
     */
    Card play(PlayerView view);
}
