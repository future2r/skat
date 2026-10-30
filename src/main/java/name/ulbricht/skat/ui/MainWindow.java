package name.ulbricht.skat.ui;

import javafx.scene.Scene;
import javafx.stage.Stage;

import name.ulbricht.skat.sound.SoundPlayer;

/**
 * Sets up the main window with the Skat table.
 */
public final class MainWindow {

    private static final double MIN_WIDTH = 800;
    private static final double MIN_HEIGHT = 600;

    private MainWindow() {
    }

    /**
     * Creates the scene, shows the window maximized and deals the first game.
     *
     * @param stage the primary stage
     * @param title the window title
     */
    public static void show(Stage stage, String title) {
        GameController controller = new GameController(System.nanoTime(), Settings.forUser(), SoundPlayer.create());
        stage.setTitle(title);
        stage.getIcons().addAll(AppIcon.images());
        stage.setScene(createScene(controller, TableScreen.WIDTH, TableScreen.HEIGHT));
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setMaximized(true);
        stage.show();
        controller.start();
    }

    /** Creates the scene for a controller; also used to render screenshots without a window. */
    static Scene createScene(GameController controller, double width, double height) {
        Scene scene = new Scene(controller.node(), width, height);
        scene.getStylesheets().add(MainWindow.class.getResource("skat.css").toExternalForm());
        return scene;
    }
}
