package name.ulbricht.skat.ai;

import java.util.EnumMap;
import java.util.Map;

import name.ulbricht.skat.engine.Game;
import name.ulbricht.skat.engine.Phase;
import name.ulbricht.skat.engine.Session;
import name.ulbricht.skat.model.PlayerId;
import name.ulbricht.skat.model.Seat;

/**
 * Compares two strengths on the same deals: bidding and announcing are always "Normal"; then the
 * declarer plays with one strength and the opponents with the other. Prints how often the
 * declarer wins. Run manually with the test class path; not part of the unit tests.
 */
public final class StrengthDuel {

    private StrengthDuel() {
    }

    public static void main(String[] args) {
        int games = args.length > 0 ? Integer.parseInt(args[0]) : 200;
        Strength[][] pairings = {
            {Strength.NORMAL, Strength.NORMAL},
            {Strength.STRONG, Strength.NORMAL},
            {Strength.NORMAL, Strength.STRONG},
            {Strength.CASUAL, Strength.NORMAL},
            {Strength.NORMAL, Strength.CASUAL},
        };
        for (Strength[] pairing : pairings) {
            long start = System.nanoTime();
            int played = 0;
            int won = 0;
            Session session = new Session(42, PlayerId.HUMAN);
            ComputerPlayer bidder = new SimplePlayer();
            ComputerPlayer declarerPlayer = pairing[0].create(1);
            ComputerPlayer opponentPlayer = pairing[1].create(2);
            for (int i = 0; i < games; i++) {
                Game game = session.startGame();
                Map<Seat, ComputerPlayer> players = new EnumMap<>(Seat.class);
                while (game.phase() != Phase.FINISHED && game.phase() != Phase.PASSED_IN) {
                    if (game.phase() == Phase.TRICK_COMPLETE) {
                        game.collectTrick();
                    } else if (game.phase() == Phase.PLAYING) {
                        if (players.isEmpty()) {
                            for (Seat seat : Seat.values()) {
                                players.put(seat, seat == game.declarer() ? declarerPlayer : opponentPlayer);
                            }
                        }
                        ComputerMove.perform(game, players.get(game.actingSeat()));
                    } else {
                        ComputerMove.perform(game, bidder);
                    }
                }
                if (game.phase() == Phase.FINISHED) {
                    played++;
                    won += game.result().isWon() ? 1 : 0;
                }
            }
            long millis = (System.nanoTime() - start) / 1_000_000;
            System.out.printf("Declarer %-6s vs opponents %-6s: declarer won %d of %d (%.1f %%), %d ms%n",
                    pairing[0], pairing[1], won, played, 100.0 * won / played, millis);
        }
    }
}
