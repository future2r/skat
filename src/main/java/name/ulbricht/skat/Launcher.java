package name.ulbricht.skat;

import javafx.application.Application;

/**
 * Entry point of the program.
 *
 * <p>Kept separate from {@link SkatApp} so that the JavaFX runtime can be loaded from the
 * class path; a main class that extends {@code Application} directly would require the
 * JavaFX modules on the module path.
 */
public final class Launcher {

    private Launcher() {
    }

    /**
     * Starts the Skat application.
     *
     * @param args command line arguments, passed on to JavaFX
     */
    public static void main(String[] args) {
        Application.launch(SkatApp.class, args);
    }
}
