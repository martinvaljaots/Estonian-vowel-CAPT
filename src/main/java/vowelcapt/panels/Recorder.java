package vowelcapt.panels;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Recorder implements AudioProcessor {

    TargetDataLine line;
    AudioFormat format;
    Thread stopper;

    public Recorder(TargetDataLine line, AudioFormat format) {
        this.line = line;
        this.format = format;
    }


    @Override
    public boolean process(AudioEvent audioEvent) {
        recordAudio();
        return true;
    }

    private void recordAudio() {

        File wavFile = new File("C:/Test/RecordAudio.wav");

        // format of audio file
        AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
/*
        stopper = new Thread(new Runnable() {
            public void run() {

                        System.out.println("Start capturing...");

                        AudioInputStream ais = new AudioInputStream(line);

                        System.out.println("Start recording...");

                        // start recording
                        try {
                            AudioSystem.write(ais, fileType, wavFile);
                            System.out.println("still writing?");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
        });
        */
       while(IsRecording.get()) {
           // path of the wav file
          // stopper.run();
           System.out.println("Start capturing...");

           AudioInputStream ais = new AudioInputStream(line);

           System.out.println("Start recording...");

           // start recording
           try {
               AudioSystem.write(ais, fileType, wavFile);
               System.out.println("still writing?");
           } catch (IOException e) {
               e.printStackTrace();
           }
           System.out.println("HPADHSOASHDOHAS");
       }
    }

    @Override
    public void processingFinished() {
        System.out.println("FINISHED");
       // stopper.interrupt();
    }

    public void setLine(TargetDataLine line) {
        this.line = line;
    }

    public void setFormat(AudioFormat format) {
        this.format = format;
    }
}
