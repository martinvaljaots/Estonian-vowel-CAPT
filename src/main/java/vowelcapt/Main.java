package vowelcapt;

import de.fau.cs.jstk.exceptions.MalformedParameterStringException;
import de.fau.cs.jstk.framed.*;
import de.fau.cs.jstk.io.FrameOutputStream;
import de.fau.cs.jstk.sampled.AudioFileReader;
import de.fau.cs.jstk.sampled.AudioSource;
import de.fau.cs.jstk.sampled.RawAudioFormat;
import vowelcapt.panels.Spectrogram;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {


        String[] trial = new String[]{"Record-002.wav", "2"};
        try {
            AudioSource as = new AudioFileReader(trial[0],
                    RawAudioFormat.create(trial.length > 2 ? trial[1] : "f:" + trial[0]),
                    true);
            Window wnd = new HammingWindow(as, 25, 10, false);
            AutoCorrelation acf = new FastACF(wnd);
            LPCSpectrum lpc = new LPCSpectrum(acf);
            Formants fs = new Formants(lpc, as.getSampleRate(), Integer.parseInt(trial[1]));
            System.out.println(as);
            System.out.println(wnd);
            System.out.println(acf);
            System.out.println(lpc);
            System.out.println(fs);
            double [] buf = new double [fs.getFrameSize()];
            FrameOutputStream fos = new FrameOutputStream(buf.length);
// TODO: saada aru, millised formandid on millised. tundub, et
            // küll on aga võimalik siiski saada esimesed n formanti - hetkel saabki 3.

            while (fs.read(buf)) {
                for (double d : buf) {
                    System.out.println(d + " ");
                }
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
