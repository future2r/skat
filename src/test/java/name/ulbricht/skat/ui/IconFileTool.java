package name.ulbricht.skat.ui;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

/**
 * Writes the application icon as a Windows icon file ({@code src/packaging/skat.ico}) for
 * {@code Skat.exe}, with PNG images in the usual sizes, and the same PNGs for the window
 * ({@code src/main/resources/name/ulbricht/skat/ui/icon}). Run manually after changing {@link AppIcon};
 * not part of the unit tests.
 */
public final class IconFileTool {

    private static final int[] SIZES = {16, 24, 32, 48, 64, 128, 256};
    private static final int HEADER = 6;
    private static final int ENTRY = 16;
    private static final int MAX_BYTE_SIZE = 255;

    private IconFileTool() {
    }

    public static void main(String[] args) throws InterruptedException {
        Path target = Paths.get(args.length > 0 ? args[0] : "src/packaging/skat.ico");
        Path pngFolder = Paths.get("src/main/resources/name/ulbricht/skat/ui/icon");
        CountDownLatch done = new CountDownLatch(1);
        Platform.startup(() -> {
            try {
                List<byte[]> pngs = new ArrayList<>();
                Files.createDirectories(pngFolder);
                for (int size : SIZES) {
                    byte[] png = png(AppIcon.render(size));
                    pngs.add(png);
                    Files.write(pngFolder.resolve(AppIcon.fileName(size)), png);
                }
                write(target, pngs);
                System.out.println("Wrote " + target.toAbsolutePath() + " and the PNGs in " + pngFolder);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } finally {
                done.countDown();
            }
        });
        done.await();
        Platform.exit();
    }

    private static byte[] png(Image image) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = image.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffered.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(buffered, "png", out);
        return out.toByteArray();
    }

    /** ICO layout: header, one directory entry per image, then the PNG data. */
    private static void write(Path target, List<byte[]> pngs) throws IOException {
        ByteBuffer directory = ByteBuffer.allocate(HEADER + ENTRY * pngs.size()).order(ByteOrder.LITTLE_ENDIAN);
        directory.putShort((short) 0).putShort((short) 1).putShort((short) pngs.size());
        int offset = HEADER + ENTRY * pngs.size();
        for (int i = 0; i < pngs.size(); i++) {
            int size = SIZES[i];
            byte dimension = (byte) (size > MAX_BYTE_SIZE ? 0 : size);
            directory.put(dimension).put(dimension).put((byte) 0).put((byte) 0);
            directory.putShort((short) 1).putShort((short) 32);
            directory.putInt(pngs.get(i).length).putInt(offset);
            offset += pngs.get(i).length;
        }
        File parent = target.toAbsolutePath().getParent().toFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Cannot create " + parent);
        }
        try (OutputStream out = Files.newOutputStream(target)) {
            out.write(directory.array());
            for (byte[] png : pngs) {
                out.write(png);
            }
        }
    }
}
