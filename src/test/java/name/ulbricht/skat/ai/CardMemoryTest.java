package name.ulbricht.skat.ai;

import static name.ulbricht.skat.TestCards.card;
import static name.ulbricht.skat.TestCards.cards;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.engine.Game;
import name.ulbricht.skat.engine.PlayerView;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Deal;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.Seat;

class CardMemoryTest {

    /** Forehand plays leaves; acorns are led, then hearts, which forehand cannot follow. */
    private static Game playedTwoTricks() {
        Map<Seat, List<Card>> hands = new EnumMap<>(Seat.class);
        hands.put(Seat.FOREHAND, cards("lU hU lA l10 lK lO l9 l8 aA bK"));
        hands.put(Seat.MIDDLEHAND, cards("aU hA h10 hK a10 aK aO b8 b9 l7"));
        hands.put(Seat.REARHAND, cards("bU bA b10 bO a9 a8 hO h9 h8 h7"));
        Game game = new Game(new Deal(hands, cards("b7 a7")));
        game.passBid();
        game.passBid();
        game.acceptBid();
        game.takeSkat();
        game.discard(card("bK"), card("b7"));
        game.declare(Declaration.of(GameType.LEAVES));
        // The second trick is led by Middlehand (winner of the first), then Rearhand, then Forehand.
        for (String trick : new String[] {"a7 aK a8", "hA h7 l8"}) {
            for (Card played : cards(trick)) {
                game.play(played);
            }
            game.collectTrick();
        }
        return game;
    }

    @Test
    void remembersVoidsAndHighCards() {
        CardMemory memory = CardMemory.of(new PlayerView(playedTwoTricks(), Seat.MIDDLEHAND));
        assertTrue(memory.isVoid(Seat.FOREHAND, card("hK")));
        assertFalse(memory.isVoid(Seat.REARHAND, card("hK")));
        assertFalse(memory.isVoid(Seat.FOREHAND, card("lA")));
        // The ace of hearts is gone, so the own ten is the highest heart; the ace of acorns is still out.
        assertTrue(memory.isHighest(card("h10")));
        assertFalse(memory.isHighest(card("a10")));
        // Eleven trumps: Middlehand holds two, one was played.
        assertEquals(8, memory.unseenTrumps());
    }

    @Test
    void safeNullNeedsALowerCardForEveryCard() {
        assertTrue(HandEvaluator.isSafeNull(cards("a7 a9 aU l7 l8 h7 b7 b9 bU bK")));
        assertFalse(HandEvaluator.isSafeNull(cards("a8 a9 aU l7 l8 h7 b7 b9 bU bK")));
        assertEquals(new Declaration(GameType.NULL, true, false, false, true),
                HandEvaluator.handGame(cards("a7 a9 aU l7 l8 h7 b7 b9 bU bK")));
        assertNull(HandEvaluator.handGame(cards("a8 a9 aU l7 l8 h7 b7 b9 bU bK")));
    }
}
