package name.ulbricht.skat.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AppIconTest {

    /** The window loads its icon from these files; Windows ignores icons made by a snapshot. */
    @Test
    void iconFilesExistForAllSizes() {
        for (int size : new int[] {16, 24, 32, 48, 64, 128, 256}) {
            assertNotNull(AppIcon.class.getResource("icon/" + AppIcon.fileName(size)), "Missing icon " + size);
        }
    }
}
