package name.ulbricht.skat;

import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.stage.Stage;

import name.ulbricht.skat.ui.MainWindow;

/**
 * JavaFX application that shows the main window.
 */
public final class SkatApp extends Application {

    @Override
    public void start(Stage stage) {
        ResourceBundle texts = ResourceBundle.getBundle("name.ulbricht.skat.i18n.messages");
        MainWindow.show(stage, texts.getString("app.title"));
    }
}
