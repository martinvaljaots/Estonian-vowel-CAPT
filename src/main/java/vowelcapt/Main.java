package vowelcapt;

import de.fau.cs.jstk.exceptions.MalformedParameterStringException;
import de.fau.cs.jstk.framed.*;
import de.fau.cs.jstk.io.FrameOutputStream;
import de.fau.cs.jstk.sampled.AudioFileReader;
import de.fau.cs.jstk.sampled.AudioSource;
import de.fau.cs.jstk.sampled.RawAudioFormat;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String[] trial = new String[]{"Record-00djdkd.wav", "3"};
        try {
            AudioSource as = new AudioFileReader(trial[0],
                    RawAudioFormat.create(trial.length > 2 ? trial[1] : "f:" + trial[0]),
                    true);
            Window wnd = new HammingWindow(as, 25, 10, false);
            AutoCorrelation acf = new FastACF(wnd);
            LPCSpectrum lpc = new LPCSpectrum(acf);
            Formants fs = new Formants(lpc, as.getSampleRate());
            System.out.println(as);
            System.out.println(wnd);
            System.out.println(acf);
            System.out.println(lpc);
            System.out.println(fs);
            double [] buf = new double [fs.getFrameSize()];
            System.out.println(buf[2]);
            FrameOutputStream fos = new FrameOutputStream(buf.length);
// TODO: GENEREERIB SEOSETUT JURA. VÕIMALIK ENCODING PROBLEEM.
            // küll on aga võimalik siiski saada esimesed n formanti - hetkel saabki 3.
            while (fs.read(buf)) {
                fos.write(buf);
            }

            fos.close();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MalformedParameterStringException e) {
            e.printStackTrace();
        }

    }
}
