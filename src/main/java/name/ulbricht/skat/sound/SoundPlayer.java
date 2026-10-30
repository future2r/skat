package name.ulbricht.skat.sound;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

/**
 * Plays the sounds on a background thread, so the table never waits for the sound card. If the
 * computer cannot play sound, the game simply stays silent.
 */
public final class SoundPlayer {

    private final ExecutorService worker;
    private final Map<Sound, Clip> clips = new EnumMap<>(Sound.class);
    private boolean broken;

    private SoundPlayer(ExecutorService worker) {
        this.worker = worker;
    }

    /**
     * Creates a player that uses the computer's speakers.
     *
     * @return the player
     */
    public static SoundPlayer create() {
        return new SoundPlayer(Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "sound");
            thread.setDaemon(true);
            return thread;
        }));
    }

    /**
     * Creates a player that never makes a sound, e.g. for screenshots.
     *
     * @return the silent player
     */
    public static SoundPlayer silent() {
        return new SoundPlayer(null);
    }

    /**
     * Plays a sound without waiting for it.
     *
     * @param sound the sound
     */
    public void play(Sound sound) {
        if (worker != null) {
            worker.execute(() -> playNow(sound));
        }
    }

    /** Runs on the sound thread only. */
    private void playNow(Sound sound) {
        if (broken) {
            return;
        }
        try {
            Clip clip = clips.get(sound);
            if (clip == null) {
                clip = open(sound);
                clips.put(sound, clip);
            }
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        } catch (LineUnavailableException | IllegalArgumentException | IllegalStateException | SecurityException e) {
            // No usable sound device: stay silent from now on instead of trying again and again.
            broken = true;
        }
    }

    private static Clip open(Sound sound) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(SoundSynth.SAMPLE_RATE, 16, 1, true, false);
        byte[] data = SoundSynth.pcm(sound);
        Clip clip = AudioSystem.getClip();
        clip.open(format, data, 0, data.length);
        return clip;
    }
}
