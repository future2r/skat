package name.ulbricht.skat.sound;

import java.util.Random;

/**
 * Computes the sounds as 16-bit mono PCM samples, so no audio files are needed. All sounds are
 * kept quiet: their peak stays well below full scale.
 */
public final class SoundSynth {

    /** Samples per second. */
    public static final float SAMPLE_RATE = 44_100f;
    /** The loudest any sound gets, as a fraction of full scale. */
    public static final double MAX_LEVEL = 0.3;

    private static final double FADE_SECONDS = 0.005;
    private static final double C5 = 523.25;
    private static final double E5 = 659.25;
    private static final double G5 = 783.99;
    private static final double C6 = 1046.5;
    private static final double G4 = 392.0;
    private static final double F4 = 349.23;
    private static final double D4 = 293.66;

    private SoundSynth() {
    }

    /**
     * Returns the samples of a sound, little-endian signed 16 bit, one channel.
     *
     * @param sound the sound
     * @return the PCM bytes
     */
    public static byte[] pcm(Sound sound) {
        return toPcm(samples(sound));
    }

    /**
     * Returns the samples of a sound in the range -1 to 1.
     *
     * @param sound the sound
     * @return the samples
     */
    public static double[] samples(Sound sound) {
        switch (sound) {
            case CARD:
                return tap();
            case COLLECT:
                return swish();
            case TURN:
                return bell(new double[] {880}, new double[] {0.4}, 0.12);
            case WON:
                return bell(new double[] {C5, E5, G5, C6}, new double[] {0.12, 0.12, 0.12, 0.45}, 0.14);
            default:
                return bell(new double[] {G4, F4, D4}, new double[] {0.22, 0.22, 0.55}, 0.12);
        }
    }

    /** A short, soft knock of a card on the table: filtered noise with a fast decay. */
    private static double[] tap() {
        double[] out = new double[length(0.07)];
        Random random = new Random(1);
        double filtered = 0;
        for (int i = 0; i < out.length; i++) {
            double t = i / SAMPLE_RATE;
            filtered += 0.3 * (random.nextDouble() * 2 - 1 - filtered);
            double thump = Math.sin(2 * Math.PI * 180 * t);
            double envelope = Math.min(1, t / 0.002) * Math.exp(-t / 0.012);
            out[i] = 0.25 * envelope * (0.8 * filtered + 0.4 * thump);
        }
        return fadeOut(out);
    }

    /** Cards sliding together: noise that swells and fades. */
    private static double[] swish() {
        double[] out = new double[length(0.22)];
        Random random = new Random(2);
        double filtered = 0;
        for (int i = 0; i < out.length; i++) {
            filtered += 0.15 * (random.nextDouble() * 2 - 1 - filtered);
            double shape = Math.sin(Math.PI * i / out.length);
            out[i] = 0.35 * shape * shape * filtered;
        }
        return fadeOut(out);
    }

    /** Notes one after another, each like a small bell: a sine with a soft overtone that dies away. */
    private static double[] bell(double[] frequencies, double[] durations, double level) {
        int total = 0;
        for (double duration : durations) {
            total += length(duration);
        }
        double[] out = new double[total];
        int start = 0;
        for (int n = 0; n < frequencies.length; n++) {
            int count = length(durations[n]);
            double decay = durations[n] / 3;
            for (int i = 0; i < count; i++) {
                double t = i / SAMPLE_RATE;
                double envelope = Math.min(1, t / 0.005) * Math.exp(-t / decay);
                double tone = Math.sin(2 * Math.PI * frequencies[n] * t)
                        + 0.3 * Math.sin(2 * Math.PI * 2 * frequencies[n] * t);
                out[start + i] = level * envelope * tone / 1.3;
            }
            start += count;
        }
        return fadeOut(out);
    }

    private static int length(double seconds) {
        return (int) Math.round(seconds * SAMPLE_RATE);
    }

    /** Fades the last few milliseconds to silence, so the sound ends without a click. */
    private static double[] fadeOut(double[] samples) {
        int fade = Math.min(samples.length, length(FADE_SECONDS));
        for (int i = 0; i < fade; i++) {
            samples[samples.length - 1 - i] *= (double) i / fade;
        }
        for (int i = 0; i < samples.length; i++) {
            samples[i] = Math.max(-MAX_LEVEL, Math.min(MAX_LEVEL, samples[i]));
        }
        return samples;
    }

    private static byte[] toPcm(double[] samples) {
        byte[] bytes = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            int value = (int) Math.round(samples[i] * Short.MAX_VALUE);
            bytes[2 * i] = (byte) value;
            bytes[2 * i + 1] = (byte) (value >> 8);
        }
        return bytes;
    }
}
