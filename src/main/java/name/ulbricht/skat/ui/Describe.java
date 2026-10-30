package name.ulbricht.skat.ui;

import java.util.ArrayList;
import java.util.List;

import name.ulbricht.skat.ai.Advice;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.rules.CardOrder;
import name.ulbricht.skat.rules.GameResult;
import name.ulbricht.skat.rules.GameValue;

/**
 * Builds the German sentences shown to the player: game names, the calculation of a game
 * value, trick sums, group labels and the hints while playing.
 */
final class Describe {

    private final Texts texts;

    Describe(Texts texts) {
        this.texts = texts;
    }

    /**
     * Returns the name of a declared game as players say it, e.g. "Grün-Spiel Hand",
     * "Grand Hand Schneider angesagt", "Null ouvert Hand" or "Grand ouvert" (ouvert suit games and
     * Grand are always hand games with Schwarz announced, so that is not repeated).
     */
    String gameName(Declaration declaration) {
        StringBuilder name = new StringBuilder(texts.game(declaration.type()));
        if (declaration.isOuvert()) {
            name.append(' ').append(texts.get("game.ouvert"));
            if (declaration.type() == GameType.NULL && declaration.isHand()) {
                name.append(' ').append(texts.get("game.hand"));
            }
            return name.toString();
        }
        if (declaration.isHand()) {
            name.append(' ').append(texts.get("game.hand"));
        }
        if (declaration.isSchwarzAnnounced()) {
            name.append(' ').append(texts.get("game.schwarzAnnounced"));
        } else if (declaration.isSchneiderAnnounced()) {
            name.append(' ').append(texts.get("game.schneiderAnnounced"));
        }
        return name.toString();
    }

    /**
     * Returns the calculation of a game value, e.g. "mit 1, Spiel 2 × Grün 11 = 22" or
     * "Null Hand = 35".
     */
    String calculation(GameResult result) {
        Declaration declaration = result.declaration();
        GameType type = declaration.type();
        if (type == GameType.NULL) {
            return texts.get("calc.null", gameName(declaration), result.value());
        }
        int multiplier = GameValue.multiplier(declaration, result.matadors(), result.isSchneider(), result.isSchwarz());
        String levels = levels(declaration, result);
        int reached = multiplier * type.baseValue();
        String base = type.isSuitGame() ? texts.suit(type.trumpSuit()) : texts.game(type);
        String line = texts.get("calc.suit", levels, base, type.baseValue(), reached);
        if (result.isOverbid()) {
            return line + " " + texts.get("calc.overbid", result.value());
        }
        return line;
    }

    /** Returns e.g. "mit 1, Spiel 2, Hand 3". */
    private String levels(Declaration declaration, GameResult result) {
        boolean schneider = result.isSchneider();
        boolean schwarz = result.isSchwarz();
        List<String> parts = new ArrayList<>();
        parts.add(result.isWithMatadors()
                ? texts.get("calc.matadors.with", result.matadors())
                : texts.get("calc.matadors.without", result.matadors()));
        int level = result.matadors() + 1;
        parts.add(texts.get("calc.level.game", level));
        if (declaration.isHand()) {
            parts.add(texts.get("calc.level.hand", ++level));
        }
        if (schneider || schwarz || declaration.isSchneiderAnnounced()) {
            parts.add(texts.get("calc.level.schneider", ++level));
        }
        if (declaration.isSchneiderAnnounced()) {
            parts.add(texts.get("calc.level.announced", ++level));
        }
        if (schwarz || declaration.isSchwarzAnnounced()) {
            parts.add(texts.get("calc.level.schwarz", ++level));
        }
        if (declaration.isSchwarzAnnounced()) {
            parts.add(texts.get("calc.level.announced", ++level));
        }
        if (declaration.isOuvert()) {
            parts.add(texts.get("calc.level.ouvert", ++level));
        }
        return String.join(", ", parts);
    }

    /** Returns the sum of a trick, e.g. "König 4 + Acht 0 + Ass 11 = 15". */
    String trickSum(List<Card> cards) {
        List<String> parts = new ArrayList<>();
        for (Card card : cards) {
            parts.add(texts.rank(card.rank()) + " " + card.points());
        }
        return texts.get("trick.sum", String.join(" + ", parts), Card.totalPoints(cards));
    }

    /**
     * Returns the reason behind a tip, e.g. "Ein Ass bringt oft gleich 11 Augen."; {@code partner}
     * is the name of the human player's partner, used when the reason mentions him.
     */
    String tipReason(Advice advice, String partner) {
        String suit = advice.suit() == null ? "" : texts.suit(advice.suit());
        return texts.get("tip.reason." + advice.reason().name(), advice.number(), suit, partner);
    }

    /** Returns the label above a group of cards in the hand. */
    String groupLabel(HandGrouping.CardGroup group, GameType type) {
        switch (group.kind()) {
            case TRUMP:
                return type.isSuitGame()
                        ? texts.get("group.trump.suit", texts.suit(type.trumpSuit()))
                        : texts.get("group.trump.grand");
            case UNTER:
                return texts.get("group.unter");
            default:
                return texts.suit(group.suit());
        }
    }

    /** Returns the hint for the human player who has to play a card. */
    String playHint(List<Card> hand, List<Card> trick, GameType type) {
        if (trick.isEmpty()) {
            return texts.get("hint.play.lead");
        }
        Card lead = trick.get(0);
        boolean trumpLed = CardOrder.isTrump(lead, type);
        boolean canFollow = false;
        Card unterOfLedSuit = null;
        for (Card card : hand) {
            if (CardOrder.follows(lead, card, type)) {
                canFollow = true;
            }
            if (!trumpLed && type.hasTrump() && card.isUnter() && card.suit() == lead.suit()) {
                unterOfLedSuit = card;
            }
        }
        String suitName = trumpLed ? texts.get("hint.trump") : texts.suit(lead.suit());
        if (canFollow) {
            String hint = texts.get("hint.play.follow", suitName);
            if (unterOfLedSuit != null) {
                hint += " " + texts.get("hint.play.unterIsTrump", texts.card(unterOfLedSuit), suitName);
            }
            return hint;
        }
        if (trumpLed) {
            return texts.get("hint.play.noTrump");
        }
        if (!type.hasTrump()) {
            return texts.get("hint.play.free", suitName);
        }
        return texts.get("hint.play.trumpOrDiscard", suitName);
    }
}
