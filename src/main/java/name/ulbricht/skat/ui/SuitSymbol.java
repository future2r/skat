package name.ulbricht.skat.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;

import name.ulbricht.skat.model.Suit;

/**
 * Draws the four German suit symbols (acorn, leaf, heart, bell) or the French ones (club,
 * spade, heart, diamond) as vector shapes.
 * All shapes are designed in a 24 x 24 box and scaled to the requested size.
 */
final class SuitSymbol {

    private static final double DESIGN_SIZE = 24;

    private SuitSymbol() {
    }

    /** Creates the German symbol of a suit with its top-left corner at (x, y). */
    static Node create(Suit suit, double x, double y, double size) {
        return create(suit, x, y, size, false);
    }

    /** Creates the German or French ({@code french}) symbol of a suit with its top-left corner at (x, y). */
    static Node create(Suit suit, double x, double y, double size, boolean french) {
        Group group = new Group(french ? frenchShapes(suit) : shapes(suit));
        group.getTransforms().add(new Scale(size / DESIGN_SIZE, size / DESIGN_SIZE, 0, 0));
        Group placed = new Group(group);
        placed.setLayoutX(x);
        placed.setLayoutY(y);
        return placed;
    }

    /** Returns the colour used for texts of a suit (corner index). */
    static Color textColor(Suit suit) {
        switch (suit) {
            case ACORNS:
                return Color.web("#5b3a1a");
            case LEAVES:
                return Color.web("#1f7a2e");
            case HEARTS:
                return Color.web("#c8102e");
            default:
                return Color.web("#a36a00");
        }
    }

    /** Returns the clothing colour of the court figures of a suit. */
    static Color bodyColor(Suit suit) {
        switch (suit) {
            case ACORNS:
                return Color.web("#7a4b22");
            case LEAVES:
                return Color.web("#2e7d3a");
            case HEARTS:
                return Color.web("#c8102e");
            default:
                return Color.web("#d99a1a");
        }
    }

    private static Node[] shapes(Suit suit) {
        switch (suit) {
            case ACORNS:
                return new Node[] {
                    line(12, 4, 12, 1, "#5b3a1a", 2),
                    fill(new Ellipse(12, 14.5, 6, 7.5), "#c98b3c"),
                    path("M4.5 11.5C4.5 6.5 8 4 12 4s7.5 2.5 7.5 7.5z", "#5b3a1a"),
                };
            case LEAVES:
                return new Node[] {
                    path("M12 1.5C19 6.5 20.5 13 12 21.5 3.5 13 5 6.5 12 1.5z", "#2e8b3a"),
                    strokePath("M12 5v18M12 10l-4-2.5M12 10l4-2.5M12 14.5l-4.5-2.5M12 14.5l4.5-2.5", "#145a1f", 1.2),
                };
            case HEARTS:
                return new Node[] {
                    path("M12 21.5C5 16 2 12.5 2 8.5 2 5.4 4.4 3 7.3 3c2 0 3.7 1.1 4.7 2.8"
                            + "C13 4.1 14.7 3 16.7 3 19.6 3 22 5.4 22 8.5c0 4-3 7.5-10 13z", "#d0021b"),
                };
            default:
                return new Node[] {
                    outline(new Circle(12, 3, 1.6), "#8a5a00", 1.2),
                    stroke(fill(new Circle(12, 13, 8.8), "#f0b323"), "#8a5a00", 1),
                    line(3.4, 11, 20.6, 11, "#8a5a00", 2),
                    fill(new Circle(12, 16.5, 1.8), "#5a3a00"),
                    line(12, 18, 12, 21.6, "#5a3a00", 1.6),
                };
        }
    }

    /** Clubs, spades, hearts and diamonds, which stand for acorns, leaves, hearts and bells. */
    private static Node[] frenchShapes(Suit suit) {
        String black = "#1a1a1a";
        String red = "#d0021b";
        switch (suit) {
            case ACORNS:
                return new Node[] {
                    fill(new Circle(12, 6.5, 4.6), black),
                    fill(new Circle(6.6, 13.2, 4.6), black),
                    fill(new Circle(17.4, 13.2, 4.6), black),
                    path("M11 12 L9.5 22.5 L14.5 22.5 L13 12 Z", black),
                };
            case LEAVES:
                return new Node[] {
                    path("M12 1.5C9 6 3 9.5 3 14.2 3 17.4 5.5 19 8 18.7 9.6 18.5 10.7 17.6 11.2 16.6"
                            + "C11 19.2 10.1 21.2 8.5 22.5L15.5 22.5C13.9 21.2 13 19.2 12.8 16.6"
                            + "C13.3 17.6 14.4 18.5 16 18.7 18.5 19 21 17.4 21 14.2 21 9.5 15 6 12 1.5Z", black),
                };
            case HEARTS:
                return new Node[] {
                    path("M12 21.5C5 16 2 12.5 2 8.5 2 5.4 4.4 3 7.3 3c2 0 3.7 1.1 4.7 2.8"
                            + "C13 4.1 14.7 3 16.7 3 19.6 3 22 5.4 22 8.5c0 4-3 7.5-10 13z", red),
                };
            default:
                return new Node[] {path("M12 1 L20.5 12 L12 23 L3.5 12 Z", red)};
        }
    }

    private static SVGPath path(String content, String color) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.setFill(Color.web(color));
        return path;
    }

    private static SVGPath strokePath(String content, String color, double width) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        return outline(path, color, width);
    }

    private static Line line(double x1, double y1, double x2, double y2, String color, double width) {
        return stroke(new Line(x1, y1, x2, y2), color, width);
    }

    private static <T extends Shape> T fill(T shape, String color) {
        shape.setFill(Color.web(color));
        return shape;
    }

    private static <T extends Shape> T outline(T shape, String color, double width) {
        shape.setFill(Color.TRANSPARENT);
        return stroke(shape, color, width);
    }

    private static <T extends Shape> T stroke(T shape, String color, double width) {
        shape.setStroke(Color.web(color));
        shape.setStrokeWidth(width);
        return shape;
    }
}
