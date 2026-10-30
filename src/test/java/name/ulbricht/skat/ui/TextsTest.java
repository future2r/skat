package name.ulbricht.skat.ui;

import static name.ulbricht.skat.TestCards.card;
import static name.ulbricht.skat.TestCards.cards;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.ai.Advice;
import name.ulbricht.skat.ai.Strength;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.model.PlayerId;
import name.ulbricht.skat.model.Rank;
import name.ulbricht.skat.model.Seat;
import name.ulbricht.skat.model.Suit;

/**
 * Makes sure every text key used in the code exists, so no player ever sees a missing text.
 */
class TextsTest {

    /** A complete key literal; prefixes like {@code "suit." + name} are covered by {@link #allEnumKeysExist}. */
    private static final Pattern KEY = Pattern.compile("get\\(\"([a-zA-Z0-9.]+)\"\\s*[,)]");
    private final ResourceBundle bundle = ResourceBundle.getBundle(Texts.BUNDLE, Locale.GERMAN);

    @Test
    void allLiteralKeysExist() throws IOException {
        List<Path> sources;
        try (Stream<Path> files = Files.walk(Paths.get("src/main/java"))) {
            sources = files.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
        }
        for (Path source : sources) {
            Matcher matcher = KEY.matcher(new String(Files.readAllBytes(source), StandardCharsets.UTF_8));
            while (matcher.find()) {
                assertTrue(bundle.containsKey(matcher.group(1)), "Missing text " + matcher.group(1) + " in " + source);
            }
        }
    }

    /** Catches keys chosen by a condition, e.g. {@code get(won ? "result.won" : "result.lost")}. */
    @Test
    void allKeyLikeLiteralsInTheUiExist() throws IOException {
        Pattern literal = Pattern.compile("\"([a-z][a-zA-Z]*(\\.[a-zA-Z0-9]+)+)\"");
        List<Path> sources;
        try (Stream<Path> files = Files.walk(Paths.get("src/main/java/name/ulbricht/skat/ui"))) {
            sources = files.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
        }
        for (Path source : sources) {
            Matcher matcher = literal.matcher(new String(Files.readAllBytes(source), StandardCharsets.UTF_8));
            while (matcher.find()) {
                String key = matcher.group(1);
                boolean resource = key.endsWith(".css") || key.endsWith(".properties")
                        || key.startsWith("name.ulbricht.skat.");
                boolean systemProperty = key.startsWith("user.");
                assertTrue(resource || systemProperty || bundle.containsKey(key),
                        "Missing text " + key + " in " + source);
            }
        }
    }

    @Test
    void allEnumKeysExist() {
        for (Suit suit : Suit.values()) {
            assertTrue(bundle.containsKey("suit." + suit.name()));
        }
        for (Rank rank : Rank.values()) {
            assertTrue(bundle.containsKey("rank." + rank.name()));
            assertTrue(bundle.containsKey("rank.short." + rank.name()));
        }
        for (PlayerId player : PlayerId.values()) {
            assertTrue(bundle.containsKey("player." + player.name()));
        }
        for (Seat seat : Seat.values()) {
            assertTrue(bundle.containsKey("seat." + seat.name()));
        }
        for (GameType type : GameType.values()) {
            assertTrue(bundle.containsKey("game." + type.name()));
        }
        for (Rank rank : Rank.values()) {
            assertTrue(bundle.containsKey("rank.article." + rank.name()));
        }
        for (Advice.Reason reason : Advice.Reason.values()) {
            assertTrue(bundle.containsKey("tip.reason." + reason.name()), "Missing reason " + reason);
        }
        for (Strength strength : Strength.values()) {
            assertTrue(bundle.containsKey("strength." + strength.name()));
            assertTrue(bundle.containsKey("strength.text." + strength.name()));
        }
        for (RuleTopic topic : RuleTopic.values()) {
            assertTrue(bundle.containsKey("rules.title." + topic.name()));
            assertTrue(bundle.containsKey("rules.text." + topic.name()));
        }
        for (CardDeck deck : CardDeck.values()) {
            assertTrue(bundle.containsKey("deck." + deck.name()));
        }
        assertTrue(bundle.containsKey("calc.matadors.with"));
        assertTrue(bundle.containsKey("calc.matadors.without"));
    }

    /** Every French text replaces an existing one, and all suit, Unter, Ober and game names have one. */
    @Test
    void frenchTextsReplaceExistingOnes() {
        for (String key : bundle.keySet()) {
            if (key.startsWith(Texts.FRENCH_PREFIX)) {
                String replaced = key.substring(Texts.FRENCH_PREFIX.length());
                assertTrue(bundle.containsKey(replaced), key + " replaces no text");
            }
        }
        for (Suit suit : Suit.values()) {
            assertTrue(bundle.containsKey(Texts.FRENCH_PREFIX + "suit." + suit.name()));
            assertTrue(bundle.containsKey(Texts.FRENCH_PREFIX + "game." + GameType.suitGame(suit).name()));
        }
        assertTrue(bundle.containsKey(Texts.FRENCH_PREFIX + "rank." + Rank.UNTER.name()));
        assertTrue(bundle.containsKey(Texts.FRENCH_PREFIX + "rank." + Rank.OBER.name()));
    }

    @Test
    void frenchDeckUsesFrenchNames() {
        Texts texts = new Texts();
        Describe describe = new Describe(texts);
        texts.setFrench(true);
        assertEquals("die Pik-Dame", texts.cardWithArticle(card("lO")));
        assertEquals("Du musst Herz zugeben. Der Herz-Bube ist Trumpf und zählt nicht als Herz.",
                describe.playHint(cards("aU hU lA hA h9"), cards("hK h8"), GameType.LEAVES));
        HandGrouping.CardGroup trump = HandGrouping.group(cards("aU l7"), GameType.LEAVES).get(0);
        assertEquals("Trumpf: Buben und Pik", describe.groupLabel(trump, GameType.LEAVES));
        texts.setFrench(false);
        assertEquals("den Grün-Ober", texts.cardWithArticle(card("lO")));
    }

    @Test
    void everyImageDeckHasAllCardsAndABack() {
        for (CardDeck deck : CardDeck.values()) {
            if (!deck.hasImages()) {
                continue;
            }
            assertTrue(CardView.hasImage(deck, "back"), deck + " back");
            for (Card card : Card.all()) {
                assertTrue(CardView.hasImage(deck, CardView.fileName(card)), deck + " " + card);
            }
        }
    }
}
