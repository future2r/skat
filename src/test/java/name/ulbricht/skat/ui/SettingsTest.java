package name.ulbricht.skat.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsTest {

    @Test
    void soundIsOnByDefaultAndTheChoiceSurvivesARestart(@TempDir Path folder) {
        Path file = folder.resolve("Skat").resolve("settings.properties");
        Settings settings = Settings.stored(file);
        assertTrue(settings.isSoundOn());
        settings.setSoundOn(false);
        assertFalse(Settings.stored(file).isSoundOn());
    }

    @Test
    void altenburgDeckIsTheDefaultAndTheChoiceSurvivesARestart(@TempDir Path folder) {
        Path file = folder.resolve("settings.properties");
        assertEquals(CardDeck.ALTENBURG, Settings.stored(file).deck());
        Settings.stored(file).setDeck(CardDeck.FRENCH);
        assertEquals(CardDeck.FRENCH, Settings.stored(file).deck());
    }

    @Test
    void inMemorySettingsAreNotSaved() {
        Settings settings = Settings.inMemory();
        settings.setSoundOn(false);
        assertFalse(settings.isSoundOn());
        assertFalse(settings.save());
    }
}
