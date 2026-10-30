package name.ulbricht.skat.ui;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.PlayerId;
import name.ulbricht.skat.model.Rank;
import name.ulbricht.skat.model.Seat;
import name.ulbricht.skat.model.Suit;

/**
 * Access to the German UI texts in {@code name/ulbricht/skat/i18n/messages.properties}. With the French deck,
 * a text under {@code french.<key>} replaces the one under {@code <key>} where the card names differ.
 */
final class Texts {

    static final String BUNDLE = "name.ulbricht.skat.i18n.messages";

    /** Texts for the French-suited deck are stored under this prefix and replace the normal ones. */
    static final String FRENCH_PREFIX = "french.";

    private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, Locale.GERMAN);
    private boolean french;

    /** Switches to the French card names (Kreuz, Pik, Herz, Karo, Bube, Dame) or back. */
    void setFrench(boolean french) {
        this.french = french;
    }

    /** Returns the text for a key, with {@code {0}}-style arguments filled in. */
    String get(String key, Object... args) {
        String chosen = french && bundle.containsKey(FRENCH_PREFIX + key) ? FRENCH_PREFIX + key : key;
        String pattern = bundle.getString(chosen);
        if (args.length == 0) {
            return pattern;
        }
        MessageFormat format = new MessageFormat(pattern, Locale.GERMAN);
        return format.format(args);
    }

    String suit(Suit suit) {
        return get("suit." + suit.name());
    }

    String rank(Rank rank) {
        return get("rank." + rank.name());
    }

    String rankShort(Rank rank) {
        return get("rank.short." + rank.name());
    }

    /** Returns the full card name, e.g. "Rot-Ass". */
    String card(Card card) {
        return get("card.name", suit(card.suit()), rank(card.rank()));
    }

    /** Returns the card name with the article for "Leg ...", e.g. "das Rot-Ass" or "den Grün-König". */
    String cardWithArticle(Card card) {
        return get("rank.article." + card.rank().name()) + " " + card(card);
    }

    String player(PlayerId player) {
        return get("player." + player.name());
    }

    String seat(Seat seat) {
        return get("seat." + seat.name());
    }

    /** Returns the game name, e.g. "Grün-Spiel" or "Grand". */
    String game(GameType type) {
        return get("game." + type.name());
    }
}
