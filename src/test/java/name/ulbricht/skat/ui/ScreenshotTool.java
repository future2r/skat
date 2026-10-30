package name.ulbricht.skat.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import name.ulbricht.skat.ai.HandEvaluator;
import name.ulbricht.skat.engine.Game;
import name.ulbricht.skat.engine.Phase;
import name.ulbricht.skat.model.Card;
import name.ulbricht.skat.model.Declaration;
import name.ulbricht.skat.model.GameType;
import name.ulbricht.skat.sound.SoundPlayer;

/**
 * Plays one game off-screen and saves a screenshot of every phase to {@code target/screenshots}.
 * No window is opened. Run manually with the test class path; not part of the unit tests.
 */
public final class ScreenshotTool {

    private static final File OUTPUT = new File("target/screenshots");

    private ScreenshotTool() {
    }

    public static void main(String[] args) throws InterruptedException {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : 3;
        String mode = args.length > 1 ? args[1] : "";
        CountDownLatch done = new CountDownLatch(1);
        Platform.startup(() -> {
            try {
                if ("partner".equals(mode)) {
                    runAsPartner(seed);
                } else if ("ouvert".equals(mode)) {
                    runOuvert(seed);
                } else {
                    run(seed);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } finally {
                done.countDown();
            }
        });
        done.await();
        Platform.exit();
    }

    private static void run(long seed) throws IOException {
        if (!OUTPUT.isDirectory() && !OUTPUT.mkdirs()) {
            throw new IOException("Cannot create " + OUTPUT);
        }
        for (int size : new int[] {16, 48, 256}) {
            writePng(AppIcon.render(size), "icon-" + size);
        }
        GameController controller = new GameController(seed, settings(), SoundPlayer.silent());
        controller.setAnimated(false);
        Scene scene = MainWindow.createScene(controller, TableScreen.WIDTH, TableScreen.HEIGHT);
        controller.start();

        runComputers(controller);
        save(scene, "1-bidding");
        if (controller.game().phase() == Phase.BIDDING) {
            controller.onTip();
            save(scene, "1b-tip-bidding");
            controller.onWhy();
            save(scene, "1c-why-bidding");
            controller.closeOverlay();
        }
        while (controller.game().phase() == Phase.BIDDING) {
            controller.onBid(true);
            runComputers(controller);
        }
        Game game = controller.game();
        if (game.declarer() != controller.humanSeat()) {
            throw new IllegalStateException("Human did not win the bidding with seed " + seed);
        }
        save(scene, "2-skat");
        controller.onTakeSkat();
        List<Card> hand = game.hand(controller.humanSeat());
        controller.onCard(hand.get(hand.size() - 1));
        controller.onCard(hand.get(hand.size() - 2));
        save(scene, "3-discard");
        controller.onTip();
        save(scene, "3b-tip-discard");
        controller.onDiscard();
        save(scene, "4-declaration");
        controller.onTip();
        save(scene, "4a-tip-declaration");
        controller.onMoreAnnouncements();
        save(scene, "4b-more-announcements");
        controller.onDeclare(declaration(game));
        runComputers(controller);
        save(scene, "5-playing");
        if (!controller.isComputerTurn() && game.phase() == Phase.PLAYING) {
            controller.onTip();
            save(scene, "5a-tip-playing");
            controller.onToggleSound();
            save(scene, "5d-sound-off");
            controller.onToggleSound();
            controller.onCard(game.playableCards().get(0));
            save(scene, "5b-order");
            controller.computerStep();
            save(scene, "5c-order");
        }
        boolean savedTrick = false;
        while (game.phase() != Phase.FINISHED) {
            if (game.phase() == Phase.TRICK_COMPLETE) {
                if (!savedTrick) {
                    save(scene, "6-trick");
                }
                controller.onCollect();
                if (!savedTrick) {
                    controller.onLastTrick();
                    save(scene, "6b-last-trick");
                    controller.closeOverlay();
                    controller.onMenu();
                    save(scene, "6c-menu");
                    controller.onRules();
                    save(scene, "6d-rules");
                    controller.onSettings();
                    save(scene, "6e-settings");
                    controller.closeOverlay();
                    savedTrick = true;
                }
            } else if (controller.isComputerTurn()) {
                controller.computerStep();
            } else {
                controller.onCard(game.playableCards().get(0));
            }
        }
        save(scene, "7-result");
        controller.onToggleSound();
        save(scene, "7c-result-sound-off");
        controller.onToggleSound();
        controller.onAllTricks();
        save(scene, "7b-all-tricks");
    }

    /**
     * The human passes; shows the table while a computer plays alone and the human is its opponent.
     * Rendered in a 16:9 window to check the wide layout.
     */
    private static void runAsPartner(long seed) throws IOException {
        if (!OUTPUT.isDirectory() && !OUTPUT.mkdirs()) {
            throw new IOException("Cannot create " + OUTPUT);
        }
        GameController controller = new GameController(seed, settings(), SoundPlayer.silent());
        controller.setAnimated(false);
        Scene scene = MainWindow.createScene(controller, TableScreen.MAX_WIDTH, TableScreen.HEIGHT);
        controller.start();
        for (int attempt = 0; attempt < 20; attempt++) {
            runComputers(controller);
            while (controller.game().phase() == Phase.BIDDING) {
                controller.onBid(false);
                runComputers(controller);
            }
            if (controller.game().phase() != Phase.PASSED_IN) {
                break;
            }
            controller.onNextGame();
        }
        runComputers(controller);
        save(scene, "8-partner");
        if (controller.game().phase() == Phase.PLAYING) {
            controller.onCard(controller.game().playableCards().get(0));
            runComputers(controller);
            save(scene, "9-partner-trick");
            controller.onToggleSound();
            save(scene, "9b-partner-sound-off");
            controller.onToggleSound();
            controller.onMenu();
            save(scene, "9c-partner-menu");
            controller.closeOverlay();
        }
    }

    /**
     * Looks for a deal, starting at {@code seed}, where the human passes and a computer player
     * announces Null ouvert, and shows the table with the open cards.
     */
    private static void runOuvert(long seed) throws IOException {
        if (!OUTPUT.isDirectory() && !OUTPUT.mkdirs()) {
            throw new IOException("Cannot create " + OUTPUT);
        }
        for (long candidate = seed; candidate < seed + 20_000; candidate++) {
            GameController controller = new GameController(candidate, settings(), SoundPlayer.silent());
            controller.setAnimated(false);
            Scene scene = MainWindow.createScene(controller, TableScreen.WIDTH, TableScreen.HEIGHT);
            controller.start();
            runComputers(controller);
            while (controller.game().phase() == Phase.BIDDING) {
                controller.onBid(false);
                runComputers(controller);
            }
            Declaration declared = controller.game().declaration();
            if (declared != null && declared.isOuvert()) {
                System.out.println("Null ouvert with seed " + candidate);
                save(scene, "10-ouvert");
                return;
            }
        }
        throw new IllegalStateException("No Null ouvert found");
    }

    private static Declaration declaration(Game game) {
        List<Card> cards = new ArrayList<>(game.hand(game.declarer()));
        cards.addAll(game.skat());
        for (GameType type : GameType.values()) {
            Declaration candidate = Declaration.of(type);
            if (type != GameType.NULL && HandEvaluator.value(cards, candidate) >= game.bid()) {
                return candidate;
            }
        }
        return Declaration.of(HandEvaluator.bestSuitGame(game.hand(game.declarer())));
    }

    /** In-memory settings; the deck can be chosen with -Ddeck=CLEAR, ALTENBURG or FRENCH. */
    private static Settings settings() {
        Settings settings = Settings.inMemory();
        String deck = System.getProperty("deck");
        if (deck != null) {
            settings.setDeck(CardDeck.valueOf(deck));
        }
        return settings;
    }

    private static void runComputers(GameController controller) {
        while (controller.isComputerTurn()) {
            controller.computerStep();
        }
    }

    private static void save(Scene scene, String name) throws IOException {
        writePng(scene.snapshot(null), name);
    }

    private static void writePng(Image image, String name) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = image.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffered.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        ImageIO.write(buffered, "png", new File(OUTPUT, name + ".png"));
    }
}
