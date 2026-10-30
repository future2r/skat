package name.ulbricht.skat;

import java.util.ArrayList;
import java.util.List;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Rank;
import name.ulbricht.skat.model.Suit;

/**
 * Short card notation for tests: suit letter (a = acorns, l = leaves, h = hearts, b = bells)
 * followed by the rank as printed on the cards (A, 10, K, O, U, 9, 8, 7), e.g. {@code "aU"} for
 * the Unter of acorns or {@code "h10"} for the ten of hearts.
 */
public final class TestCards {

    private TestCards() {
    }

    public static Card card(String code) {
        return Card.of(suit(code.charAt(0)), rank(code.substring(1)));
    }

    public static List<Card> cards(String codes) {
        List<Card> result = new ArrayList<>();
        for (String code : codes.trim().split("\\s+")) {
            result.add(card(code));
        }
        return result;
    }

    private static Suit suit(char letter) {
        switch (letter) {
            case 'a':
                return Suit.ACORNS;
            case 'l':
                return Suit.LEAVES;
            case 'h':
                return Suit.HEARTS;
            case 'b':
                return Suit.BELLS;
            default:
                throw new IllegalArgumentException("Unknown suit " + letter);
        }
    }

    private static Rank rank(String code) {
        switch (code) {
            case "A":
                return Rank.ACE;
            case "10":
                return Rank.TEN;
            case "K":
                return Rank.KING;
            case "O":
                return Rank.OBER;
            case "U":
                return Rank.UNTER;
            case "9":
                return Rank.NINE;
            case "8":
                return Rank.EIGHT;
            case "7":
                return Rank.SEVEN;
            default:
                throw new IllegalArgumentException("Unknown rank " + code);
        }
    }
}
