package name.ulbricht.skat.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.engine.Game;
import name.ulbricht.skat.engine.Phase;
import name.ulbricht.skat.engine.Session;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.PlayerId;
import name.ulbricht.skat.model.Seat;

/**
 * Lets three computer players play many complete games to catch rule or flow errors.
 */
class SimulationTest {

    private static final int GAMES = 2000;

    @Test
    void computerPlayersFinishEveryGameConsistently() {
        Session session = new Session(7, PlayerId.HUMAN);
        ComputerPlayer player = new SimplePlayer();
        int played = 0;
        for (int i = 0; i < GAMES; i++) {
            Game game = session.startGame();
            int actions = 0;
            while (game.phase() != Phase.FINISHED && game.phase() != Phase.PASSED_IN) {
                if (game.phase() == Phase.TRICK_COMPLETE) {
                    game.collectTrick();
                } else {
                    ComputerMove.perform(game, player);
                }
                assertTrue(++actions < 200, "Game does not end");
            }
            if (game.phase() == Phase.FINISHED) {
                played++;
                assertNotNull(game.result());
                if (game.declaration().type() != GameType.NULL || game.tricks().size() == 10) {
                    assertEquals(120, totalPoints(game));
                }
            }
        }
        assertTrue(played > GAMES / 2, "Too few games were played: " + played);
    }

    @Test
    void everyStrengthPlaysLegalGames() {
        for (Strength strength : Strength.values()) {
            Session session = new Session(11, PlayerId.HUMAN);
            ComputerPlayer player = strength.create(3);
            int games = strength == Strength.STRONG ? 6 : 200;
            for (int i = 0; i < games; i++) {
                Game game = session.startGame();
                while (game.phase() != Phase.FINISHED && game.phase() != Phase.PASSED_IN) {
                    if (game.phase() == Phase.TRICK_COMPLETE) {
                        game.collectTrick();
                    } else {
                        ComputerMove.perform(game, player);
                    }
                }
            }
        }
    }

    private static int totalPoints(Game game) {
        int sum = Card.totalPoints(game.skat());
        for (Seat seat : Seat.values()) {
            sum += game.trickPoints(seat);
        }
        return sum;
    }
}
