package name.ulbricht.skat.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SoundSynthTest {

    @Test
    void everySoundIsShortQuietAndEndsSilently() {
        for (Sound sound : Sound.values()) {
            double[] samples = SoundSynth.samples(sound);
            assertTrue(samples.length > 0, sound + " is empty");
            assertTrue(samples.length < 2 * SoundSynth.SAMPLE_RATE, sound + " is longer than two seconds");
            double peak = 0;
            for (double sample : samples) {
                peak = Math.max(peak, Math.abs(sample));
            }
            assertTrue(peak > 0.01, sound + " cannot be heard");
            assertTrue(peak <= SoundSynth.MAX_LEVEL, sound + " is too loud");
            assertEquals(0, samples[samples.length - 1], 1e-9, sound + " ends with a click");
            assertEquals(2 * samples.length, SoundSynth.pcm(sound).length);
        }
    }
}
