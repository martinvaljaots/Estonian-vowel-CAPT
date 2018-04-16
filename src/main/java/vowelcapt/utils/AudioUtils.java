package vowelcapt.utils;

import javax.sound.sampled.AudioFormat;

public class AudioUtils {

    /**
     * author: www.codejava.net
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
