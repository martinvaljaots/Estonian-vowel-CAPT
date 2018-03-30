package vowelcapt.processors;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;

// TODO: Remove this if a SilenceDetector can be used on its own (and currently, it is)
public class SoundLevelMonitor implements AudioProcessor {

    private SilenceDetector silenceDetector = new SilenceDetector(-80, false);

    @Override
    public boolean process(AudioEvent audioEvent) {
        handleSound();
        return true;
    }

    private void handleSound(){
        System.out.println(silenceDetector.currentSPL());
        if(silenceDetector.currentSPL() > 60){
            System.out.println("Sound detected at:" + System.currentTimeMillis() + ", " + (int)(silenceDetector.currentSPL()) + "dB SPL\n");
        }
        System.out.println("hello, handling sound");
    }

    @Override
    public void processingFinished() {

    }
}
