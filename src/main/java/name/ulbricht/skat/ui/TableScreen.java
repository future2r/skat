package name.ulbricht.skat.ui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

/**
 * The table as laid out in the mockup {@code docs/main-window-draft.html}, designed 738 pixels high and
 * scaled to the window height. The middle part (trick, skat, panels and the own hand) is always
 * 1024 pixels wide; in a wider window the top bar and the hint bar grow and the computer players
 * move out to the edges, up to a 16:9 window. A narrower window shows the 1024 pixels in full.
 *
 * <p>This class only arranges nodes; what to show is decided by {@link GameController}.
 */
final class TableScreen {

    /** Width of the middle part, and the narrowest the table gets. */
    static final double WIDTH = 1024;
    /** The widest the table gets: a 16:9 window. */
    static final double MAX_WIDTH = 1312;
    static final double HEIGHT = 738;

    /** Card width in the hand and in the trick. */
    static final double HAND_CARD = 86;
    /** Card width while the declarer holds twelve cards. */
    static final double SMALL_HAND_CARD = 70;
    static final double TRICK_CARD = 100;

    private static final double TOP_BAR_HEIGHT = 46;
    /** Room kept free in the top bar, as text may come out a little wider on screen than measured. */
    private static final double TOP_BAR_RESERVE = 12;
    private static final double HINT_HEIGHT = 110;
    private static final double HAND_BOTTOM = HEIGHT - 120;
    private static final double OPPONENT_TOP = 62;
    private static final double OPPONENT_WIDTH = 330;
    private static final double SIDE_MARGIN = 24;
    private static final double TRICK_LABEL_TOP = 146;
    private static final double TRICK_LEFT_X = 352;
    private static final double TRICK_RIGHT_X = 572;
    private static final double TRICK_OWN_X = 462;
    private static final double TRICK_OWN_Y = 242;
    private static final double PANEL_TOP = 90;
    private static final double PANEL_HEIGHT = 350;
    private static final double LABEL_HEIGHT = 24;
    /** Where a card leaves a player's area: the middle of the card fans and of the own hand. */
    private static final double FAN_CENTER_Y = 159;
    private static final double LEFT_FAN_CENTER_X = 102;
    private static final double OWN_HAND_CENTER_Y = 560;
    private static final Duration ARRIVAL = Duration.millis(550);
    private static final Duration COLLECT = Duration.millis(600);

    /** The three places around the trick, one per player. */
    enum Spot { LEFT, RIGHT, OWN }

    /** Card width of a computer player's open cards in Ouvert games. */
    static final double OPEN_CARD = 50;
    private static final double OPEN_CARD_OVERLAP = 25;
    /** Size of a portrait inside the 54 pixel avatar ring. */
    private static final double PORTRAIT = 46;

    private final Map<Spot, Node> trickCards = new EnumMap<>(Spot.class);
    private final Map<Spot, HBox> fans = new EnumMap<>(Spot.class);

    private final StackPane viewport = new StackPane();
    private final Pane design = new Pane();
    private final HBox topBar = new HBox();
    private final VBox leftOpponent = new VBox(10);
    private final VBox rightOpponent = new VBox(10);
    /** The middle part of fixed width, centered on the table. */
    private final Pane middle = new Pane();
    private final Pane center = new Pane();
    private final StackPane panelLayer = new StackPane();
    private final HBox hand = new HBox(22);
    private final VBox hintBar = new VBox(6);
    private final StackPane overlayLayer = new StackPane();
    private final Rectangle clip = new Rectangle(WIDTH, HEIGHT);
    private double width = WIDTH;
    /** The deck whose backs are shown for the hidden cards. */
    private CardDeck deck = CardDeck.ALTENBURG;
    /** Draws the top bar again, so a changed width can show more or fewer items. */
    private Runnable topBarContent = () -> { };

    TableScreen() {
        design.getStyleClass().add("table");

        topBar.getStyleClass().add("top-bar");

        leftOpponent.relocate(SIDE_MARGIN, OPPONENT_TOP);
        rightOpponent.setPrefWidth(OPPONENT_WIDTH);
        rightOpponent.setAlignment(Pos.TOP_RIGHT);

        center.setPrefSize(WIDTH, HEIGHT);
        center.setPickOnBounds(false);

        panelLayer.setPrefSize(WIDTH, PANEL_HEIGHT);
        panelLayer.relocate(0, PANEL_TOP);
        panelLayer.setPickOnBounds(false);

        double handHeight = 230;
        hand.setAlignment(Pos.BOTTOM_CENTER);
        hand.setPrefSize(WIDTH, handHeight);
        hand.relocate(0, HAND_BOTTOM - handHeight);
        hand.setPickOnBounds(false);

        hintBar.getStyleClass().add("hint-bar");
        hintBar.relocate(0, HEIGHT - HINT_HEIGHT);

        overlayLayer.getStyleClass().add("shield");
        overlayLayer.setVisible(false);

        middle.setPrefSize(WIDTH, HEIGHT);
        middle.setPickOnBounds(false);
        middle.getChildren().addAll(center, hand, panelLayer);
        design.getChildren().addAll(topBar, leftOpponent, rightOpponent, middle, hintBar, overlayLayer);
        design.setClip(clip);
        setWidth(WIDTH);

        Scale scale = new Scale(1, 1, 0, 0);
        design.getTransforms().add(scale);
        Group scaled = new Group(design);
        viewport.getStyleClass().add("viewport");
        viewport.getChildren().add(scaled);
        viewport.widthProperty().addListener((obs, old, value) -> rescale(scale));
        viewport.heightProperty().addListener((obs, old, value) -> rescale(scale));
    }

    Parent node() {
        return viewport;
    }

    /** While blocked, the table takes no mouse clicks, e.g. while a trick slides to its winner. */
    void setInputBlocked(boolean blocked) {
        design.setMouseTransparent(blocked);
    }

    /** Sets the deck whose backs are shown from the next drawing on. */
    void setDeck(CardDeck deck) {
        this.deck = deck;
    }

    /** Scales the table to the window height and lets it grow in width as far as the window allows. */
    private void rescale(Scale scale) {
        double factor = Math.min(viewport.getWidth() / WIDTH, viewport.getHeight() / HEIGHT);
        if (factor > 0) {
            scale.setX(factor);
            scale.setY(factor);
            setWidth(Math.min(MAX_WIDTH, Math.max(WIDTH, viewport.getWidth() / factor)));
        }
    }

    private void setWidth(double newWidth) {
        width = newWidth;
        design.setPrefSize(width, HEIGHT);
        design.setMinSize(width, HEIGHT);
        design.setMaxSize(width, HEIGHT);
        clip.setWidth(width);
        topBar.setPrefSize(width, TOP_BAR_HEIGHT);
        rightOpponent.relocate(width - SIDE_MARGIN - OPPONENT_WIDTH, OPPONENT_TOP);
        middle.relocate(middleOffset(), 0);
        hintBar.setPrefSize(width, HINT_HEIGHT);
        overlayLayer.setPrefSize(width, HEIGHT);
        topBarContent.run();
    }

    /** Where the middle part starts on the table. */
    private double middleOffset() {
        return Math.round((width - WIDTH) / 2);
    }

    /**
     * Shows a window-like panel in front of the darkened table, which blocks clicks on the table,
     * or removes it when {@code content} is {@code null}.
     */
    void setOverlay(Node content, Pos position) {
        if (content == null) {
            overlayLayer.getChildren().clear();
            overlayLayer.setVisible(false);
            return;
        }
        StackPane.setAlignment(content, position);
        StackPane.setMargin(content, position == Pos.TOP_RIGHT ? new Insets(TOP_BAR_HEIGHT + 6, 12, 0, 0) : null);
        overlayLayer.getChildren().setAll(content);
        overlayLayer.setVisible(true);
    }

    /**
     * Shows the top bar: a bold title, further items, an optional points badge and buttons on the right.
     * The items are ordered by importance; if the bar is too narrow, the last ones are left out
     * instead of cutting every text short.
     */
    void setTopBar(String title, List<String> items, String points, Node... buttons) {
        topBarContent = () -> fillTopBar(title, items, points, buttons);
        topBarContent.run();
    }

    private void fillTopBar(String title, List<String> items, String points, Node... buttons) {
        topBar.getChildren().clear();
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title");
        titleLabel.setMinWidth(Region.USE_PREF_SIZE);
        topBar.getChildren().add(titleLabel);
        List<Label> itemLabels = new ArrayList<>();
        for (String item : items) {
            Label label = new Label(item);
            label.setMinWidth(Region.USE_PREF_SIZE);
            itemLabels.add(label);
            topBar.getChildren().add(label);
        }
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().add(spacer);
        if (points != null) {
            Label pill = new Label(points);
            pill.getStyleClass().add("pill");
            pill.setMinWidth(Region.USE_PREF_SIZE);
            topBar.getChildren().add(pill);
        }
        for (Node button : buttons) {
            if (button instanceof Region) {
                ((Region) button).setMinWidth(Region.USE_PREF_SIZE);
            }
        }
        topBar.getChildren().addAll(buttons);
        topBar.applyCss();
        while (!itemLabels.isEmpty() && contentWidth(topBar) > topBar.getPrefWidth() - TOP_BAR_RESERVE) {
            topBar.getChildren().remove(itemLabels.remove(itemLabels.size() - 1));
        }
    }

    /** The width the children of a row need, without stretching. */
    private static double contentWidth(HBox row) {
        double width = row.snappedLeftInset() + row.snappedRightInset();
        for (Node child : row.getChildren()) {
            width += child.prefWidth(-1);
        }
        return width + row.getSpacing() * Math.max(0, row.getChildren().size() - 1);
    }

    /** Which side a player is on, shown as a coloured label next to the name. */
    enum Team { NONE, DECLARER, PARTNER, OPPONENT }

    /**
     * Shows a computer player: portrait (Ludwig on the left, Richard on the right), name with team
     * label, role, an optional speech bubble and the backs of the cards. The player on turn gets a
     * yellow ring around the portrait and a yellow name.
     */
    void setOpponent(boolean left, String name, String role, int cards, String bubble, boolean active,
            Team team, String teamText) {
        VBox box = left ? leftOpponent : rightOpponent;
        box.getChildren().clear();

        StackPane avatar = new StackPane(Portrait.of(left, PORTRAIT));
        avatar.getStyleClass().add("avatar");
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("opp-name");
        if (active) {
            avatar.getStyleClass().add("active");
            nameLabel.getStyleClass().add("active");
        }
        Label roleLabel = new Label(role);
        roleLabel.getStyleClass().add("opp-role");
        HBox nameRow = new HBox(8, nameLabel);
        nameRow.setAlignment(left ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        if (team != Team.NONE) {
            Label badge = new Label(teamText);
            badge.getStyleClass().addAll("team-badge", team.name().toLowerCase(Locale.ROOT));
            nameRow.getChildren().add(left ? 1 : 0, badge);
        }
        VBox text = new VBox(2, nameRow, roleLabel);
        text.setAlignment(left ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);

        HBox who = new HBox(12);
        who.setAlignment(left ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        if (left) {
            who.getChildren().addAll(avatar, text);
        } else {
            who.getChildren().addAll(text, avatar);
        }
        if (bubble != null) {
            Label bubbleLabel = new Label(bubble);
            bubbleLabel.getStyleClass().add("bubble");
            who.getChildren().add(left ? who.getChildren().size() : 0, bubbleLabel);
        }

        HBox fan = new HBox(-30);
        fan.setAlignment(left ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        for (int i = 0; i < cards; i++) {
            fan.getChildren().add(CardView.back(44, deck));
        }
        fans.put(left ? Spot.LEFT : Spot.RIGHT, fan);
        box.getChildren().addAll(who, fan);
    }

    /** Shows a computer player's cards face up instead of their backs (Ouvert games). */
    void showOpenCards(boolean left, List<? extends Node> cards) {
        HBox fan = fans.get(left ? Spot.LEFT : Spot.RIGHT);
        fan.setSpacing(-OPEN_CARD_OVERLAP);
        fan.getChildren().setAll(cards);
    }

    /** Removes everything from the middle of the table. */
    void clearCenter() {
        center.getChildren().clear();
        trickCards.clear();
    }

    /**
     * Places one player's part of the trick: the label (name, with the playing order once a card
     * lies there) and the card. The own place shows a dashed slot while it is empty; the slot
     * turns yellow when it is the player's turn.
     */
    void setTrickSpot(Spot spot, String label, Node card, String emptyText, boolean active) {
        Label title = new Label(label);
        title.getStyleClass().add("table-label");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        Node content = card;
        if (card != null) {
            trickCards.put(spot, card);
        } else if (spot == Spot.OWN) {
            StackPane slot = slot(emptyText);
            if (active) {
                slot.getStyleClass().add("active");
            }
            content = slot;
        }
        VBox box = new VBox(4);
        box.setPrefWidth(TRICK_CARD);
        box.setAlignment(Pos.TOP_CENTER);
        if (spot == Spot.OWN) {
            box.getChildren().add(content);
            box.getChildren().add(title);
            box.relocate(TRICK_OWN_X, TRICK_OWN_Y);
        } else {
            box.getChildren().add(title);
            if (content != null) {
                box.getChildren().add(content);
            }
            box.relocate(spot == Spot.LEFT ? TRICK_LEFT_X : TRICK_RIGHT_X, TRICK_LABEL_TOP);
        }
        center.getChildren().add(box);
    }

    /** Lets the card of a spot fly in from the player who played it. */
    void animateArrival(Spot spot) {
        Node card = trickCards.get(spot);
        if (card == null) {
            return;
        }
        Point2D from = playerCenter(spot);
        Point2D to = trickCenter(spot);
        TranslateTransition move = new TranslateTransition(ARRIVAL, card);
        move.setFromX(from.getX() - to.getX());
        move.setFromY(from.getY() - to.getY());
        move.setToX(0);
        move.setToY(0);
        move.setInterpolator(Interpolator.EASE_OUT);
        ScaleTransition grow = new ScaleTransition(ARRIVAL, card);
        grow.setFromX(0.6);
        grow.setFromY(0.6);
        grow.setToX(1);
        grow.setToY(1);
        new ParallelTransition(move, grow).play();
    }

    /** Lets all trick cards slide to the winner and fade out, then runs {@code done}. */
    void animateCollect(Spot winner, Runnable done) {
        ParallelTransition all = new ParallelTransition();
        Point2D target = playerCenter(winner);
        for (Map.Entry<Spot, Node> entry : trickCards.entrySet()) {
            Node card = entry.getValue();
            Point2D from = trickCenter(entry.getKey());
            TranslateTransition move = new TranslateTransition(COLLECT, card);
            move.setToX(target.getX() - from.getX());
            move.setToY(target.getY() - from.getY());
            move.setInterpolator(Interpolator.EASE_IN);
            ScaleTransition shrink = new ScaleTransition(COLLECT, card);
            shrink.setToX(0.5);
            shrink.setToY(0.5);
            FadeTransition fade = new FadeTransition(COLLECT, card);
            fade.setToValue(0.2);
            all.getChildren().addAll(move, shrink, fade);
        }
        all.setOnFinished(event -> done.run());
        all.play();
    }

    /** Where a player's cards are, in the coordinates of the middle part like the trick. */
    private Point2D playerCenter(Spot spot) {
        switch (spot) {
            case LEFT:
                return new Point2D(LEFT_FAN_CENTER_X - middleOffset(), FAN_CENTER_Y);
            case RIGHT:
                return new Point2D(width - LEFT_FAN_CENTER_X - middleOffset(), FAN_CENTER_Y);
            default:
                return new Point2D(WIDTH / 2, OWN_HAND_CENTER_Y);
        }
    }

    private static Point2D trickCenter(Spot spot) {
        double halfWidth = TRICK_CARD / 2;
        double halfHeight = TRICK_CARD * 1.5 / 2;
        switch (spot) {
            case LEFT:
                return new Point2D(TRICK_LEFT_X + halfWidth, TRICK_LABEL_TOP + LABEL_HEIGHT + halfHeight);
            case RIGHT:
                return new Point2D(TRICK_RIGHT_X + halfWidth, TRICK_LABEL_TOP + LABEL_HEIGHT + halfHeight);
            default:
                return new Point2D(TRICK_OWN_X + halfWidth, TRICK_OWN_Y + halfHeight);
        }
    }

    /** Shows the two covered skat cards at the top of the table. */
    void setSkatBacks(String label) {
        Label title = new Label(label);
        title.getStyleClass().add("table-label");
        HBox cards = new HBox(8, CardView.back(54, deck), CardView.back(54, deck));
        VBox skat = new VBox(4, title, cards);
        skat.setAlignment(Pos.CENTER);
        skat.relocate(WIDTH / 2 - 58, OPPONENT_TOP);
        center.getChildren().add(skat);
    }

    /** Shows the two skat places while the declarer chooses cards to put away. */
    void setDiscardSlots(String label, Node first, Node second) {
        Label title = new Label(label);
        title.getStyleClass().add("table-label");
        HBox slots = new HBox(40, first != null ? first : slot("1"), second != null ? second : slot("2"));
        VBox box = new VBox(6, title, slots);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(2 * TRICK_CARD + 40);
        box.relocate((WIDTH - box.getPrefWidth()) / 2, TRICK_LABEL_TOP);
        center.getChildren().add(box);
    }

    /** Shows a panel in the middle of the table, or none. */
    void setPanel(Node panel) {
        panelLayer.getChildren().clear();
        if (panel != null) {
            panelLayer.getChildren().add(panel);
        }
    }

    /** Shows the player's cards as labelled groups. */
    void setHand(List<String> labels, List<List<CardView>> groups) {
        hand.getChildren().clear();
        for (int i = 0; i < groups.size(); i++) {
            Label label = new Label(labels.get(i));
            label.getStyleClass().add("group-label");
            HBox cards = new HBox(6);
            cards.getChildren().addAll(groups.get(i));
            VBox group = new VBox(26, label, cards);
            group.setAlignment(Pos.BOTTOM_CENTER);
            hand.getChildren().add(group);
        }
    }

    /** Shows the hint sentence and the buttons that make sense right now. */
    void setHint(String text, List<Button> buttons) {
        hintBar.getChildren().clear();
        Label icon = new Label("i");
        icon.getStyleClass().add("hint-icon");
        Label sentence = new Label(text);
        sentence.getStyleClass().add("hint-text");
        sentence.setWrapText(true);
        sentence.setMinHeight(Region.USE_PREF_SIZE);
        HBox.setHgrow(sentence, Priority.ALWAYS);
        sentence.setMaxWidth(Double.MAX_VALUE);
        HBox line = new HBox(10, icon, sentence);
        line.setAlignment(Pos.CENTER_LEFT);
        HBox row = new HBox(10);
        row.getChildren().addAll(buttons);
        hintBar.getChildren().addAll(line, row);
    }

    private static StackPane slot(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        StackPane slot = new StackPane(label);
        slot.getStyleClass().add("slot");
        slot.setPrefSize(TRICK_CARD, TRICK_CARD * 1.5);
        slot.setMinSize(TRICK_CARD, TRICK_CARD * 1.5);
        slot.setPadding(new Insets(6));
        return slot;
    }
}
