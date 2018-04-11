package vowelcapt.utils;

import javax.sound.sampled.AudioFormat;

public class AudioUtils {

    /** TODO: where was this method originally found in? + link
     * This method was originally found in
     */
    public static AudioFormat getAudioFormat() {
        float sampleRate = 22050;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate,
                sampleSizeInBits, channels, signed, bigEndian);
    }
}
