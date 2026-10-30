package name.ulbricht.skat.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import name.ulbricht.skat.ai.Strength;

/**
 * The player's settings, kept in a small properties file in the user profile so they survive a
 * restart. If the file cannot be read or written, the defaults are used and nothing is saved.
 */
final class Settings {

    private static final String SOUND = "sound";
    private static final String STRENGTH = "strength";
    private static final String DECK = "deck";

    private final Path file;
    private final Properties values = new Properties();

    private Settings(Path file) {
        this.file = file;
        if (file != null && Files.isRegularFile(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                values.load(in);
            } catch (IOException e) {
                values.clear();
            }
        }
    }

    /** Settings stored in {@code file}. */
    static Settings stored(Path file) {
        return new Settings(file);
    }

    /** Settings of the current Windows user: {@code %APPDATA%\Skat}, else {@code ~/.skat}. */
    static Settings forUser() {
        String appData = System.getenv("APPDATA");
        Path folder = appData != null ? Paths.get(appData, "Skat")
                : Paths.get(System.getProperty("user.home"), ".skat");
        return new Settings(folder.resolve("settings.properties"));
    }

    /** Settings that live only in memory, e.g. for screenshots. */
    static Settings inMemory() {
        return new Settings(null);
    }

    boolean isSoundOn() {
        return Boolean.parseBoolean(values.getProperty(SOUND, "true"));
    }

    void setSoundOn(boolean on) {
        values.setProperty(SOUND, String.valueOf(on));
        save();
    }

    /** The computer players' strength; "Normal" unless chosen otherwise. */
    Strength strength() {
        String name = values.getProperty(STRENGTH, Strength.NORMAL.name());
        for (Strength strength : Strength.values()) {
            if (strength.name().equals(name)) {
                return strength;
            }
        }
        return Strength.NORMAL;
    }

    void setStrength(Strength strength) {
        values.setProperty(STRENGTH, strength.name());
        save();
    }

    /** The card deck; the Altenburg deck unless chosen otherwise. */
    CardDeck deck() {
        String name = values.getProperty(DECK, CardDeck.ALTENBURG.name());
        for (CardDeck deck : CardDeck.values()) {
            if (deck.name().equals(name)) {
                return deck;
            }
        }
        return CardDeck.ALTENBURG;
    }

    void setDeck(CardDeck deck) {
        values.setProperty(DECK, deck.name());
        save();
    }

    /** Writes the settings; returns {@code false} if that was not possible. */
    boolean save() {
        if (file == null) {
            return false;
        }
        try {
            Path folder = file.toAbsolutePath().getParent();
            if (folder != null) {
                Files.createDirectories(folder);
            }
            try (OutputStream out = Files.newOutputStream(file)) {
                values.store(out, "Skat");
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
