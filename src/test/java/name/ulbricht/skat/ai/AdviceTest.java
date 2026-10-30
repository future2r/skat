package name.ulbricht.skat.ai;

import static name.ulbricht.skat.TestCards.card;
import static name.ulbricht.skat.TestCards.cards;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import name.ulbricht.skat.model.Suit;

/**
 * Checks the tips with their reasons, step by step through one game: forehand plays leaves.
 */
class AdviceTest {

    private final SimplePlayer player = new SimplePlayer();

    private static Deal deal() {
        Map<Seat, List<Card>> hands = new EnumMap<>(Seat.class);
        hands.put(Seat.FOREHAND, cards("lU hU lA l10 lK lO l9 l8 aA bK"));
        hands.put(Seat.MIDDLEHAND, cards("aU hA h10 hK a10 aK aO b8 b9 l7"));
        hands.put(Seat.REARHAND, cards("bU bA b10 bO a9 a8 hO h9 h8 h7"));
        return new Deal(hands, cards("b7 a7"));
    }

    private Advice advice(Game game) {
        PlayerView view = new PlayerView(game, game.actingSeat());
        switch (game.phase()) {
            case BIDDING:
                return player.adviseBid(view);
            case SKAT_DECISION:
                return player.adviseSkat(view);
            case DISCARD:
                return player.adviseDiscard(view);
            case DECLARATION:
                return player.adviseDeclaration(view);
            default:
                return player.advisePlay(view);
        }
    }

    /** Forehand wins the bidding with 18, puts away the king and seven of bells and plays leaves. */
    private static Game grueneGame() {
        Game game = new Game(deal());
        game.passBid();
        game.passBid();
        game.acceptBid();
        game.takeSkat();
        game.discard(card("bK"), card("b7"));
        game.declare(Declaration.of(GameType.LEAVES));
        return game;
    }

    @Test
    void weakHandPasses() {
        Advice advice = advice(new Game(deal()));
        assertFalse(advice.accept());
        assertEquals(Advice.Reason.BID_WEAK, advice.reason());
    }

    @Test
    void strongHandSaysWithTheValueItReaches() {
        Game game = new Game(deal());
        game.passBid();
        game.passBid();
        Advice advice = advice(game);
        assertTrue(advice.accept());
        assertEquals(Advice.Reason.BID_REACHES, advice.reason());
        // Eight trumps and a side ace are good for leaves hand, without 1: game 2, hand 3 x 11.
        assertEquals(33, advice.number());
    }

    /** Middlehand says up to 24, forehand is asked to hold it: 9 answers "yes" in a row. */
    private static Game forehandAskedToHold24() {
        Game game = new Game(deal());
        for (int i = 0; i < 9; i++) {
            game.acceptBid();
        }
        assertEquals(Seat.FOREHAND, game.actingSeat());
        assertEquals(24, game.pendingBidValue());
        return game;
    }

    @Test
    void bidAboveTheValueWithSkatNeedsAHandGame() {
        Game game = forehandAskedToHold24();
        List<Card> hand = game.hand(Seat.FOREHAND);
        assertEquals(22, HandEvaluator.maxBid(hand));
        assertEquals(33, HandEvaluator.maxBidWithHand(hand));
        Advice advice = advice(game);
        assertTrue(advice.accept());
        assertEquals(Advice.Reason.BID_REACHES_HAND, advice.reason());
    }

    @Test
    void casualPlayerNeverPlaysHandSoItStopsAtTheValueWithSkat() {
        Game game = forehandAskedToHold24();
        PlayerView view = new PlayerView(game, Seat.FOREHAND);
        for (long seed = 0; seed < 20; seed++) {
            assertFalse(new CasualPlayer(seed).acceptBid(view));
        }
    }

    @Test
    void handDeclarerStopsDrawingTrumpsWhenTheOpponentsHaveNone() {
        // The seven of leaves lies in the skat, which the hand declarer does not know.
        Map<Seat, List<Card>> hands = new EnumMap<>(Seat.class);
        hands.put(Seat.FOREHAND, cards("aU lU lA l10 lK lO l9 l8 aA bK"));
        hands.put(Seat.MIDDLEHAND, cards("hU hA h10 hK a10 aK aO b8 b9 b7"));
        hands.put(Seat.REARHAND, cards("bU bA b10 bO a9 a8 hO h9 h8 h7"));
        Game game = new Game(new Deal(hands, cards("l7 a7")));
        game.passBid();
        game.passBid();
        game.acceptBid();
        game.playHand();
        game.declare(Declaration.hand(GameType.LEAVES));
        for (String trick : new String[] {"aU hU bU", "lU b7 h7"}) {
            for (Card played : cards(trick)) {
                game.play(played);
            }
            game.collectTrick();
        }
        Advice advice = advice(game);
        assertEquals(card("aA"), advice.card());
        assertEquals(Advice.Reason.LEAD_ACE, advice.reason());
    }

    @Test
    void veryStrongHandIsPlayedWithoutTheSkat() {
        Game game = new Game(deal());
        game.passBid();
        game.passBid();
        game.acceptBid();
        Advice advice = advice(game);
        assertFalse(advice.accept());
        assertEquals(Advice.Reason.PLAY_HAND, advice.reason());
        game.playHand();
        assertEquals(Declaration.hand(GameType.LEAVES), advice(game).declaration());
    }

    @Test
    void discardGivesUpASuit() {
        Game game = new Game(deal());
        game.passBid();
        game.passBid();
        game.acceptBid();
        game.takeSkat();
        Advice advice = advice(game);
        assertEquals(cards("bK b7"), advice.cards());
        assertEquals(Advice.Reason.DISCARD_VOID, advice.reason());
        assertEquals(Suit.BELLS, advice.suit());
        assertEquals(4, advice.number());
    }

    @Test
    void declarationNamesTheTrumps() {
        Game game = new Game(deal());
        game.passBid();
        game.passBid();
        game.acceptBid();
        game.takeSkat();
        game.discard(card("bK"), card("b7"));
        Advice advice = advice(game);
        assertEquals(Declaration.of(GameType.LEAVES), advice.declaration());
        assertEquals(Advice.Reason.DECLARE_SUIT, advice.reason());
        assertEquals(8, advice.number());
    }

    @Test
    void declarerDrawsTrumpsFirst() {
        Advice advice = advice(grueneGame());
        assertEquals(card("lU"), advice.card());
        assertEquals(Advice.Reason.DRAW_TRUMPS, advice.reason());
    }

    @Test
    void cheapTrickIsNotWorthAStrongCard() {
        Game game = grueneGame();
        game.play(card("a7"));
        Advice advice = advice(game);
        assertEquals(card("aO"), advice.card());
        assertEquals(Advice.Reason.NOT_WORTH, advice.reason());
    }

    @Test
    void partnerSurelyWinsSoSmear() {
        Game game = grueneGame();
        game.play(card("a7"));
        game.play(card("a10"));
        Advice advice = advice(game);
        // Neun and Acht have no points; of equal cards the weaker one goes.
        assertEquals(card("a8"), advice.card());
        assertEquals(Advice.Reason.SMEAR, advice.reason());
    }

    @Test
    void aceCannotBeBeaten() {
        Game game = grueneGame();
        game.play(card("aA"));
        Advice advice = advice(game);
        assertEquals(card("aO"), advice.card());
        assertEquals(Advice.Reason.CANNOT_WIN, advice.reason());
    }
}
