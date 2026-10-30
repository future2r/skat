package name.ulbricht.skat.engine;

import static name.ulbricht.skat.TestCards.card;
import static name.ulbricht.skat.TestCards.cards;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Deal;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.Seat;

class GameTest {

    /** Forehand holds all leaves, the Unter of acorns and the ace of hearts: a strong leaves game. */
    private static Deal deal() {
        Map<Seat, List<Card>> hands = new EnumMap<>(Seat.class);
        hands.put(Seat.FOREHAND, cards("aU lU lA l10 lK lO l9 l8 l7 hA"));
        hands.put(Seat.MIDDLEHAND, cards("hU bU aA a10 aK aO a9 a8 a7 h10"));
        hands.put(Seat.REARHAND, cards("bA b10 bK bO b9 b8 b7 hK hO h9"));
        return new Deal(hands, cards("h8 h7"));
    }

    /** Middlehand passes, rearhand passes, forehand plays for 18 and takes the skat. */
    private static Game gameWithForehandDeclaring(GameType type) {
        Game game = new Game(deal());
        game.passBid();
        game.passBid();
        game.acceptBid();
        assertEquals(Phase.SKAT_DECISION, game.phase());
        game.takeSkat();
        assertEquals(12, game.hand(Seat.FOREHAND).size());
        game.discard(card("h8"), card("h7"));
        game.declare(Declaration.of(type));
        return game;
    }

    @Test
    void biddingLeadsToSkatDecision() {
        Game game = new Game(deal());
        assertEquals(Seat.MIDDLEHAND, game.actingSeat());
        game.passBid();
        game.passBid();
        game.acceptBid();
        assertEquals(Seat.FOREHAND, game.declarer());
        assertEquals(18, game.bid());
    }

    @Test
    void allPassedIsPassedIn() {
        Game game = new Game(deal());
        game.passBid();
        game.passBid();
        game.passBid();
        assertEquals(Phase.PASSED_IN, game.phase());
    }

    @Test
    void declarationMustMatchHandDecision() {
        Game game = new Game(deal());
        game.passBid();
        game.passBid();
        game.acceptBid();
        game.playHand();
        assertFalse(game.canDeclare(Declaration.of(GameType.LEAVES)));
        assertTrue(game.canDeclare(Declaration.hand(GameType.LEAVES)));
    }

    @Test
    void forehandLeadsAndPlayerMustZugeben() {
        Game game = gameWithForehandDeclaring(GameType.LEAVES);
        assertEquals(Seat.FOREHAND, game.actingSeat());
        game.play(card("hA"));
        assertEquals(cards("h10"), game.playableCards());
        assertThrows(IllegalArgumentException.class, () -> game.play(card("aA")));
    }

    @Test
    void trickWaitsForCollectionAndWinnerLeads() {
        Game game = gameWithForehandDeclaring(GameType.LEAVES);
        game.play(card("hA"));
        game.play(card("h10"));
        game.play(card("h9"));
        assertEquals(Phase.TRICK_COMPLETE, game.phase());
        assertEquals(Seat.FOREHAND, game.currentTrickWinner());
        game.collectTrick();
        assertEquals(Seat.FOREHAND, game.actingSeat());
        assertEquals(21, game.trickPoints(Seat.FOREHAND));
    }

    @Test
    void takeBackOnlyWhileNobodyPlayedAfterwards() {
        Game game = gameWithForehandDeclaring(GameType.LEAVES);
        game.play(card("hA"));
        assertTrue(game.canTakeBack(Seat.FOREHAND));
        game.play(card("h10"));
        assertFalse(game.canTakeBack(Seat.FOREHAND));
        assertThrows(IllegalStateException.class, () -> game.takeBack(Seat.FOREHAND));
        game.play(card("h9"));
        assertFalse(game.canTakeBack(Seat.MIDDLEHAND));
        assertTrue(game.canTakeBack(Seat.REARHAND));
        game.takeBack(Seat.REARHAND);
        assertEquals(Phase.PLAYING, game.phase());
        assertEquals(cards("hA h10"), game.currentTrick());
        assertEquals(Seat.REARHAND, game.actingSeat());
        assertTrue(game.hand(Seat.REARHAND).contains(card("h9")));
    }

    @Test
    void nullEndsAsSoonAsDeclarerTakesATrick() {
        Game game = gameWithForehandDeclaring(GameType.NULL);
        game.play(card("hA"));
        game.play(card("h10"));
        game.play(card("h9"));
        game.collectTrick();
        assertEquals(Phase.FINISHED, game.phase());
        assertFalse(game.result().isWon());
        assertEquals(23, game.result().value());
    }

    @Test
    void nullBelowBidCannotBeDeclared() {
        Game game = new Game(deal());
        game.acceptBid();
        for (int i = 0; i < 4; i++) {
            game.acceptBid();
            game.acceptBid();
        }
        game.passBid();
        game.passBid();
        assertTrue(game.bid() > 23);
        game.takeSkat();
        List<Card> hand = new ArrayList<>(game.hand(game.declarer()));
        game.discard(hand.get(0), hand.get(1));
        assertFalse(game.canDeclare(Declaration.of(GameType.NULL)));
    }
}
