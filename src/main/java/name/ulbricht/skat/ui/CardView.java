package name.ulbricht.skat.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Rank;
import name.ulbricht.skat.model.Suit;

/**
 * A card of the chosen {@link CardDeck}. The own "Klares Blatt" is drawn with shapes: large corner
 * indices top left and bottom right, suit symbols for number cards and the ace, double-headed
 * simple figures for king, Ober and Unter, designed in a 200 x 300 box and scaled to the requested
 * width. The other decks come from image files in {@code decks/<folder>/<suit>-<rank>.png}.
 */
final class CardView extends Pane {

    /** Visual state of a card in the hand. */
    enum State { NORMAL, PLAYABLE, DIMMED, SELECTED, TIP }

    private static final double DESIGN_WIDTH = 200;
    private static final double DESIGN_HEIGHT = 300;
    private static final double RATIO = DESIGN_HEIGHT / DESIGN_WIDTH;
    private static final double[][] PIPS_7 = {{44, 50}, {122, 50}, {44, 128}, {122, 128}, {44, 206}, {122, 206},
        {83, 89}};
    private static final double[][] PIPS_8 = {{44, 50}, {122, 50}, {44, 128}, {122, 128}, {44, 206}, {122, 206},
        {83, 89}, {83, 167}};
    private static final double[][] PIPS_9 = {{44, 46}, {122, 46}, {44, 100}, {122, 100}, {44, 154}, {122, 154},
        {44, 208}, {122, 208}, {83, 127}};
    private static final double[][] PIPS_10 = {{44, 46}, {122, 46}, {44, 100}, {122, 100}, {44, 154}, {122, 154},
        {44, 208}, {122, 208}, {83, 73}, {83, 181}};

    /**
     * Images are decoded at this multiple of the shown width, because the whole table is scaled up
     * on larger screens.
     */
    private static final double SHARPNESS = 2;
    /** Decoded card images by deck, name and size; only the current deck is kept. */
    private static final Map<String, Image> IMAGES = new HashMap<>();

    private final Card card;
    private final double width;
    private final Rectangle frame;

    /** Creates the face of a card of the given deck. */
    CardView(Card card, double width, Texts texts, CardDeck deck) {
        this.card = card;
        this.width = width;
        double height = width * RATIO;
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);

        Node design;
        if (deck.hasImages()) {
            design = imageView(deck, fileName(card), width);
        } else {
            Group drawn = new Group(face(card, texts));
            drawn.getTransforms().add(new Scale(width / DESIGN_WIDTH, width / DESIGN_WIDTH, 0, 0));
            design = drawn;
        }
        frame = new Rectangle(width, height);
        frame.setArcWidth(width / 7);
        frame.setArcHeight(width / 7);
        frame.setFill(Color.TRANSPARENT);
        frame.setStrokeWidth(Math.max(3, width / 17));
        frame.setStroke(Color.TRANSPARENT);
        frame.setMouseTransparent(true);
        getChildren().addAll(design, frame);
        setEffect(shadow(width));
    }

    /** Creates the back of a card of the given deck. */
    static Pane back(double width, CardDeck deck) {
        double height = width * RATIO;
        if (deck.hasImages()) {
            Pane pane = new Pane(imageView(deck, "back", width));
            pane.setPrefSize(width, height);
            pane.setMinSize(width, height);
            pane.setMaxSize(width, height);
            pane.setEffect(shadow(width));
            return pane;
        }
        // Dark red with fine diagonal stripes and a cream border, like a classic card back.
        Rectangle back = new Rectangle(width, height);
        back.setArcWidth(width / 6);
        back.setArcHeight(width / 6);
        double stripe = Math.max(3, width / 11);
        back.setFill(new LinearGradient(0, 0, stripe, stripe, false, CycleMethod.REPEAT,
                new Stop(0, Color.web("#7a1f2b")), new Stop(0.5, Color.web("#7a1f2b")),
                new Stop(0.5, Color.web("#93303c")), new Stop(1, Color.web("#93303c"))));
        back.setStroke(Color.web("#f3ead8"));
        back.setStrokeWidth(Math.max(2, width / 15));
        Pane pane = new Pane(back);
        pane.setPrefSize(width, height);
        pane.setMinSize(width, height);
        pane.setMaxSize(width, height);
        pane.setEffect(shadow(width));
        return pane;
    }

    Card card() {
        return card;
    }

    /** A soft shadow below a card, growing with its size, as if it lay on the felt. */
    private static DropShadow shadow(double width) {
        return new DropShadow(Math.max(4, width / 9), 0, width / 30, Color.rgb(0, 0, 0, 0.5));
    }

    /** The image file of a card without extension, e.g. "hearts-ober". */
    static String fileName(Card card) {
        return card.suit().name().toLowerCase(Locale.ROOT) + "-" + card.rank().name().toLowerCase(Locale.ROOT);
    }

    /** Tells whether the image file of a card or of the back ("back") exists in a deck. */
    static boolean hasImage(CardDeck deck, String name) {
        return CardView.class.getResource(imagePath(deck, name)) != null;
    }

    private static String imagePath(CardDeck deck, String name) {
        return "decks/" + deck.folder() + "/" + name + ".png";
    }

    private static ImageView imageView(CardDeck deck, String name, double width) {
        ImageView view = new ImageView(image(deck, name, width));
        view.setFitWidth(width);
        view.setFitHeight(width * RATIO);
        view.setSmooth(true);
        return view;
    }

    private static Image image(CardDeck deck, String name, double width) {
        int decodedWidth = (int) Math.ceil(width * SHARPNESS);
        String key = deck.name() + "/" + name + "@" + decodedWidth;
        Image cached = IMAGES.get(key);
        if (cached != null) {
            return cached;
        }
        // Images of another deck are no longer needed once the player has switched.
        IMAGES.keySet().removeIf(other -> !other.startsWith(deck.name() + "/"));
        URL url = CardView.class.getResource(imagePath(deck, name));
        if (url == null) {
            throw new IllegalStateException("Missing card image " + imagePath(deck, name));
        }
        Image image = new Image(url.toExternalForm(), decodedWidth, decodedWidth * RATIO, false, true);
        IMAGES.put(key, image);
        return image;
    }

    /**
     * Shows the card as normal, playable (raised, yellow frame), dimmed, selected (raised, blue frame)
     * or as the tip (raised highest, glowing green frame).
     */
    void setState(State state) {
        double unit = width / 86;
        switch (state) {
            case TIP:
                setTranslateY(-24 * unit);
                frame.setStroke(Color.web("#1fa83c"));
                setEffect(new DropShadow(18 * unit, Color.web("#7dff8f")));
                break;
            case PLAYABLE:
                setTranslateY(-18 * unit);
                frame.setStroke(Color.web("#ffc400"));
                setEffect(shadow(width));
                break;
            case SELECTED:
                setTranslateY(-26 * unit);
                frame.setStroke(Color.web("#2f7de1"));
                setEffect(shadow(width));
                break;
            case DIMMED:
                setTranslateY(0);
                frame.setStroke(Color.TRANSPARENT);
                setEffect(new ColorAdjust(0, -0.4, -0.45, 0));
                break;
            default:
                setTranslateY(0);
                frame.setStroke(Color.TRANSPARENT);
                setEffect(shadow(width));
                break;
        }
    }

    private static List<Node> face(Card card, Texts texts) {
        Suit suit = card.suit();
        Color color = SuitSymbol.textColor(suit);
        List<Node> nodes = new ArrayList<>();
        Rectangle background = new Rectangle(1, 1, DESIGN_WIDTH - 2, DESIGN_HEIGHT - 2);
        background.setArcWidth(28);
        background.setArcHeight(28);
        background.setFill(Color.WHITE);
        background.setStroke(Color.web("#999999"));
        background.setStrokeWidth(2);
        nodes.add(background);

        Rank rank = card.rank();
        if (rank == Rank.ACE) {
            nodes.add(SuitSymbol.create(suit, 55, 50, 90));
            nodes.add(SuitSymbol.create(suit, 55, 160, 90));
        } else if (rank == Rank.KING || rank == Rank.OBER || rank == Rank.UNTER) {
            nodes.add(courtPanel(card, color));
        } else {
            for (double[] pip : pips(rank)) {
                nodes.add(SuitSymbol.create(suit, pip[0], pip[1], 34));
            }
        }

        String label = texts.rankShort(rank);
        nodes.add(cornerIndex(label, suit, color));
        Group bottom = cornerIndex(label, suit, color);
        bottom.getTransforms().add(new Rotate(180, DESIGN_WIDTH / 2, DESIGN_HEIGHT / 2));
        nodes.add(bottom);
        return nodes;
    }

    private static Group cornerIndex(String label, Suit suit, Color color) {
        Text text = new Text(label);
        text.setFont(Font.font("Arial", FontWeight.BOLD, label.length() > 1 ? 31 : 38));
        text.setFill(color);
        text.setX(23 - text.getLayoutBounds().getWidth() / 2);
        text.setY(44);
        return new Group(text, SuitSymbol.create(suit, 9, 50, 28));
    }

    private static double[][] pips(Rank rank) {
        switch (rank) {
            case SEVEN:
                return PIPS_7;
            case EIGHT:
                return PIPS_8;
            case NINE:
                return PIPS_9;
            default:
                return PIPS_10;
        }
    }

    private static Group courtPanel(Card card, Color color) {
        Rectangle panel = new Rectangle(44, 34, 112, 232);
        panel.setArcWidth(12);
        panel.setArcHeight(12);
        panel.setFill(Color.web("#faf6ec"));
        panel.setStroke(color);
        panel.setStrokeWidth(2.5);

        Group upper = figure(card);
        Group lower = figure(card);
        lower.getTransforms().add(new Rotate(180, DESIGN_WIDTH / 2, DESIGN_HEIGHT / 2));
        Group figures = new Group(upper, lower);
        Rectangle clip = new Rectangle(46, 36, 108, 228);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        figures.setClip(clip);

        Line divider = new Line(44, 150, 156, 150);
        divider.setStroke(color);
        divider.setStrokeWidth(2);
        return new Group(panel, figures, divider);
    }

    /** One half of a court card: bust with suit-coloured clothes, headgear by rank, and the suit symbol. */
    private static Group figure(Card card) {
        Color ink = Color.web("#222222");
        SVGPath body = svg("M58 150 Q58 108 100 104 Q142 108 142 150 Z", SuitSymbol.bodyColor(card.suit()), ink, 2);
        SVGPath collar = svg("M88 105 L100 124 L112 105 Z", Color.WHITE, ink, 1.5);
        Circle head = new Circle(100, 86, 17, Color.web("#f2d0a9"));
        head.setStroke(ink);
        head.setStrokeWidth(2);
        Circle leftEye = new Circle(94, 84, 2, ink);
        Circle rightEye = new Circle(106, 84, 2, ink);
        SVGPath mouth = svg("M94 93 q6 4 12 0", Color.TRANSPARENT, ink, 1.5);
        double pipY = card.rank() == Rank.UNTER ? 104 : 44;
        return new Group(body, collar, head, leftEye, rightEye, mouth, headgear(card.rank()),
                SuitSymbol.create(card.suit(), 44, pipY, 30));
    }

    private static Node headgear(Rank rank) {
        if (rank == Rank.KING) {
            return svg("M80 70 L80 52 L90 62 L100 48 L110 62 L120 52 L120 70 Z", Color.web("#f0b323"),
                    Color.web("#8a5a00"), 2);
        }
        Color dark = Color.web("#333333");
        if (rank == Rank.OBER) {
            Rectangle crown = new Rectangle(84, 50, 32, 16);
            crown.setArcWidth(6);
            crown.setArcHeight(6);
            crown.setFill(dark);
            Rectangle brim = new Rectangle(76, 64, 48, 5);
            brim.setFill(dark);
            SVGPath feather = svg("M114 52 q16 -14 22 4", Color.TRANSPARENT, Color.web("#c8102e"), 4);
            return new Group(crown, brim, feather);
        }
        Ellipse cap = new Ellipse(100, 66, 24, 8);
        cap.setFill(dark);
        Circle bobble = new Circle(100, 60, 5, Color.web("#c8102e"));
        return new Group(cap, bobble);
    }

    private static SVGPath svg(String content, Color fill, Color stroke, double strokeWidth) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.setFill(fill);
        path.setStroke(stroke);
        path.setStrokeWidth(strokeWidth);
        return path;
    }
}
