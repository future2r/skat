package name.ulbricht.skat.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;

/**
 * Simple drawn faces of the computer players: Ludwig with a flat cap and a moustache, Richard
 * with grey hair and glasses. Designed in a 54 x 54 box and shown round.
 */
final class Portrait {

    private static final double DESIGN_SIZE = 54;
    private static final String SKIN = "#f2c9a0";
    private static final String INK = "#333333";

    private Portrait() {
    }

    /** The face of Ludwig (left) or Richard (right), {@code size} pixels wide and round. */
    static Node of(boolean ludwig, double size) {
        Group face = new Group();
        face.getChildren().add(circle(27, 27, 27, ludwig ? "#cfe3f2" : "#f3dfc2"));
        face.getChildren().add(circle(27, 30, 13, SKIN));
        if (ludwig) {
            face.getChildren().addAll(
                    path("M12 22c2-9 28-9 30 0l-30 0z", "#5a4636"),
                    brim(),
                    path("M19 35q8 5 16 0q-8 2-16 0z", "#7a5a3a"));
        } else {
            face.getChildren().addAll(
                    path("M15 25q12-16 24 0q-3-6-12-6t-12 6z", "#b9b9b9"),
                    ring(22, 29),
                    ring(32, 29),
                    line("M25.6 29h2.8"),
                    line("M22 37q5 3 10 0"));
        }
        face.getChildren().addAll(circle(22, 29, 1.3, INK), circle(32, 29, 1.3, INK));
        face.setClip(new Circle(27, 27, 27));
        face.getTransforms().add(new Scale(size / DESIGN_SIZE, size / DESIGN_SIZE, 0, 0));
        return new Group(face);
    }

    private static Circle circle(double x, double y, double radius, String color) {
        return new Circle(x, y, radius, Color.web(color));
    }

    private static Rectangle brim() {
        Rectangle brim = new Rectangle(10, 20, 34, 4);
        brim.setArcWidth(4);
        brim.setArcHeight(4);
        brim.setFill(Color.web("#3e3026"));
        return brim;
    }

    private static Circle ring(double x, double y) {
        Circle ring = new Circle(x, y, 3.6, Color.TRANSPARENT);
        ring.setStroke(Color.web(INK));
        ring.setStrokeWidth(1.4);
        return ring;
    }

    private static SVGPath path(String content, String color) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.setFill(Color.web(color));
        return path;
    }

    private static SVGPath line(String content) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.setFill(Color.TRANSPARENT);
        path.setStroke(Color.web(INK));
        path.setStrokeWidth(1.4);
        return path;
    }
}
