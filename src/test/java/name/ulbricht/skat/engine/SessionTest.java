package name.ulbricht.skat.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.ai.ComputerMove;
import name.ulbricht.skat.ai.SimplePlayer;
import name.ulbricht.skat.model.PlayerId;
import name.ulbricht.skat.model.Seat;

/**
 * Checks the seating and the playing order around the table: play goes clockwise, i.e. from the
 * human player (bottom) to Ludwig (left) to Richard (right), starting with forehand, the player to
 * the left of the dealer.
 */
class SessionTest {

    @Test
    void forehandSitsLeftOfTheDealerAndTheDealIsPassedClockwise() {
        Session session = new Session(1, PlayerId.RIGHT);
        session.startGame();
        assertEquals(PlayerId.HUMAN, session.playerAt(Seat.FOREHAND));
        assertEquals(PlayerId.LEFT, session.playerAt(Seat.MIDDLEHAND));
        assertEquals(PlayerId.RIGHT, session.playerAt(Seat.REARHAND));
        passIn(session.game());
        session.startGame();
        assertEquals(PlayerId.HUMAN, session.dealer());
        assertEquals(PlayerId.LEFT, session.playerAt(Seat.FOREHAND));
    }

    @Test
    void abandonedGameLetsTheNextPlayerDeal() {
        Session session = new Session(1, PlayerId.RIGHT);
        session.startGame();
        assertTrue(session.isGameRunning());
        assertThrows(IllegalStateException.class, session::startGame);
        session.abandonGame();
        assertFalse(session.isGameRunning());
        session.startGame();
        assertEquals(PlayerId.HUMAN, session.dealer());
        assertTrue(session.isGameRunning());
    }

    @Test
    void everyTrickIsPlayedClockwiseStartingWithItsLeader() {
        Session session = new Session(5, PlayerId.RIGHT);
        SimplePlayer player = new SimplePlayer();
        for (int games = 0; games < 50; games++) {
            Game game = session.startGame();
            List<PlayerId> order = new ArrayList<>();
            while (game.phase() != Phase.FINISHED && game.phase() != Phase.PASSED_IN) {
                if (game.phase() == Phase.TRICK_COMPLETE) {
                    assertClockwise(order);
                    order.clear();
                    game.collectTrick();
                } else {
                    if (game.phase() == Phase.PLAYING) {
                        order.add(session.playerAt(game.actingSeat()));
                    }
                    ComputerMove.perform(game, player);
                }
            }
        }
    }

    private static void assertClockwise(List<PlayerId> order) {
        assertEquals(3, order.size());
        assertEquals(order.get(0).next(), order.get(1));
        assertEquals(order.get(1).next(), order.get(2));
    }

    private static void passIn(Game game) {
        game.passBid();
        game.passBid();
        game.passBid();
    }
}
