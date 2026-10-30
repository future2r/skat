package name.ulbricht.skat.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;

class AnnouncementsTest {

    @Test
    void withoutAnnouncementsTheDeclarationIsPlain() {
        Announcements announcements = new Announcements();
        assertEquals(Declaration.of(GameType.LEAVES), announcements.declaration(GameType.LEAVES, false));
        assertEquals(Declaration.hand(GameType.GRAND), announcements.declaration(GameType.GRAND, true));
    }

    @Test
    void schwarzIncludesSchneiderAndSwitchingSchneiderOffRemovesSchwarz() {
        Announcements announcements = new Announcements();
        announcements.toggleSchwarz();
        assertTrue(announcements.isSchneider());
        assertEquals(new Declaration(GameType.ACORNS, true, true, true, false),
                announcements.declaration(GameType.ACORNS, true));
        announcements.toggleSchneider();
        assertFalse(announcements.isSchwarz());
        assertFalse(announcements.isAny());
    }

    @Test
    void announcementsNeedAHandGame() {
        Announcements announcements = new Announcements();
        announcements.toggleSchneider();
        assertNull(announcements.declaration(GameType.LEAVES, false));
    }

    @Test
    void ouvertIncludesSchwarzForSuitGamesButNotForNull() {
        Announcements announcements = new Announcements();
        announcements.toggleOuvert();
        assertEquals(new Declaration(GameType.GRAND, true, true, true, true),
                announcements.declaration(GameType.GRAND, true));
        assertEquals(new Declaration(GameType.NULL, false, false, false, true),
                announcements.declaration(GameType.NULL, false));
        assertNull(announcements.declaration(GameType.HEARTS, false));
    }
}
