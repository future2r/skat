package name.ulbricht.skat.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

import name.ulbricht.skat.model.Suit;

/**
 * The program icon: two fanned cards of the own deck (hearts and acorns) on the green table.
 * It is drawn in code; {@code IconFileTool} (test sources) renders it to the PNG files in
 * {@code icon/}, which the window uses, and to {@code Skat.ico}. Windows ignores window icons
 * made by a snapshot, so the window loads the files.
 */
final class AppIcon {

    private static final double DESIGN_SIZE = 256;
    private static final int[] SIZES = {16, 24, 32, 48, 64, 128, 256};

    private AppIcon() {
    }

    /** Loads the icon in all sizes from the PNG files. */
    static List<Image> images() {
        List<Image> images = new ArrayList<>();
        for (int size : SIZES) {
            URL url = AppIcon.class.getResource("icon/" + fileName(size));
            if (url != null) {
                images.add(new Image(url.toExternalForm()));
            }
        }
        return images;
    }

    /** The PNG file of one size, e.g. "skat-48.png". */
    static String fileName(int size) {
        return "skat-" + size + ".png";
    }

    /** Renders the icon at one size; must run on the JavaFX application thread. */
    static Image render(int size) {
        Group icon = new Group(design());
        icon.getTransforms().add(new Scale(size / DESIGN_SIZE, size / DESIGN_SIZE, 0, 0));
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(0, 0, size, size));
        return new Group(icon).snapshot(parameters, null);
    }

    private static Node design() {
        Rectangle table = new Rectangle(DESIGN_SIZE, DESIGN_SIZE);
        table.setArcWidth(56);
        table.setArcHeight(56);
        table.setFill(Color.web("#1e6b3c"));
        Node back = card(Suit.HEARTS, 34, 50, -14);
        Node front = card(Suit.ACORNS, 112, 50, 12);
        Group design = new Group(table, back, front);
        Rectangle clip = new Rectangle(DESIGN_SIZE, DESIGN_SIZE);
        clip.setArcWidth(56);
        clip.setArcHeight(56);
        design.setClip(clip);
        return design;
    }

    private static Node card(Suit suit, double x, double y, double angle) {
        double width = 110;
        double height = 160;
        Rectangle paper = new Rectangle(width, height);
        paper.setArcWidth(22);
        paper.setArcHeight(22);
        paper.setFill(Color.WHITE);
        paper.setStroke(Color.web("#9a9a9a"));
        paper.setStrokeWidth(4);
        double symbolSize = 84;
        Node symbol = SuitSymbol.create(suit, (width - symbolSize) / 2, (height - symbolSize) / 2, symbolSize);
        Group card = new Group(paper, symbol);
        card.getTransforms().add(new Rotate(angle, width / 2, height));
        card.setLayoutX(x);
        card.setLayoutY(y);
        card.setEffect(new DropShadow(10, 0, 3, Color.rgb(0, 0, 0, 0.45)));
        return card;
    }
}
