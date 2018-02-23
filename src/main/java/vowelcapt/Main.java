package vowelcapt;

import be.tarsos.dsp.resample.SoundTouchRateTransposer;
import de.fau.cs.jstk.exceptions.MalformedParameterStringException;
import de.fau.cs.jstk.framed.*;
import de.fau.cs.jstk.io.FrameOutputStream;
import de.fau.cs.jstk.sampled.AudioFileReader;
import de.fau.cs.jstk.sampled.AudioSource;
import de.fau.cs.jstk.sampled.RawAudioFormat;
import vowelcapt.panels.Spectrogram;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {


        String[] trial = new String[]{"Record-i.wav", "3"};
        try {
            AudioSource as = new AudioFileReader(trial[0],
                    RawAudioFormat.create(trial.length > 2 ? trial[1] : "f:" + trial[0]),
                    true);
            Window wnd = new HammingWindow(as, 25, 10, false);
            // AutoCorrelation acf = new FastACF(wnd);
            AutoCorrelation acf = new SimpleACF(wnd);
            LPCSpectrum lpc = new LPCSpectrum(acf, 22, true);
            Formants fs = new Formants(lpc, as.getSampleRate(), Integer.parseInt(trial[1]));
            System.out.println(as.getSampleRate());
            System.out.println(Arrays.toString(trial));
            System.out.println(wnd.getFrameSize());
            System.out.println(acf);
            System.out.println(lpc.getFrameSize());
            System.out.println(fs);
            double[] buf = new double[fs.getFrameSize()];
            FrameOutputStream fos = new FrameOutputStream(buf.length);
// TODO: leida, mis värk on sellega, et teine ja kolmas formant pole ÜLDSE usutavate väärtustega

            while (fs.read(buf)) {
                System.out.println(Arrays.toString(buf));
            }

        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MalformedParameterStringException e) {
            e.printStackTrace();
        }

    }
}
