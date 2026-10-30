package name.ulbricht.skat.ai;

import java.util.List;

import name.ulbricht.skat.engine.Game;
import name.ulbricht.skat.engine.PlayerView;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Seat;

/**
 * Performs the next action of a computer player in a game.
 */
public final class ComputerMove {

    private ComputerMove() {
    }

    /**
     * Lets the acting seat's computer player make one decision and applies it to the game.
     *
     * @param game the running game; a seat must be acting
     * @param player the computer player of the acting seat
     * @throws IllegalStateException if no seat is acting
     */
    public static void perform(Game game, ComputerPlayer player) {
        Seat seat = game.actingSeat();
        if (seat == null) {
            throw new IllegalStateException("No seat is acting in phase " + game.phase());
        }
        PlayerView view = new PlayerView(game, seat);
        switch (game.phase()) {
            case BIDDING:
                if (player.acceptBid(view)) {
                    game.acceptBid();
                } else {
                    game.passBid();
                }
                break;
            case SKAT_DECISION:
                if (player.playHand(view)) {
                    game.playHand();
                } else {
                    game.takeSkat();
                }
                break;
            case DISCARD:
                List<Card> discard = player.discard(view);
                game.discard(discard.get(0), discard.get(1));
                break;
            case DECLARATION:
                game.declare(player.declare(view));
                break;
            case PLAYING:
                game.play(player.play(view));
                break;
            default:
                throw new IllegalStateException("Unexpected phase " + game.phase());
        }
    }
}
