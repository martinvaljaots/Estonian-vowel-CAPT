package vowelcapt;

import be.tarsos.dsp.*;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;
import de.fau.cs.jstk.framed.LPCSpectrum;
import vowelcapt.panels.*;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

public class EssentialFeaturesFrame extends JFrame {

    float sampleRate = 44100;
    int bufferSize = 1024 * 4;
    int overlap = 768 * 4;
    Mixer currentMixer;
    SoundDetector soundDetector;
    AudioDispatcher dispatcher;
    double threshold = -60;
    SilenceDetector silenceDetector;
    PitchDetectorExample pitchDetector;
    Spectrogram spectrogram;
    AudioProcessor fftProcessor;
    public double pitch;


    public EssentialFeaturesFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Essential features");
        JPanel inputPanel = new InputPanel();
        add(inputPanel, BorderLayout.NORTH);
        inputPanel.addPropertyChangeListener("mixer",
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent arg0) {
                        try {
                            setNewMixer((Mixer) arg0.getNewValue());
                        } catch (LineUnavailableException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });

        
        fftProcessor = new AudioProcessor(){

            FFT fft = new FFT(bufferSize);
            float[] amplitudes = new float[bufferSize/2];

            @Override
            public void processingFinished() {
                // TODO Auto-generated method stub
            }

            @Override
            public boolean process(AudioEvent audioEvent) {
                float[] audioFloatBuffer = audioEvent.getFloatBuffer();
                float[] transformbuffer = new float[bufferSize*2];
                System.arraycopy(audioFloatBuffer, 0, transformbuffer, 0, audioFloatBuffer.length);
                fft.forwardTransform(transformbuffer);
                fft.modulus(transformbuffer, amplitudes);
                spectrogram.panel.drawFFT(spectrogram.pitch, amplitudes,fft);
                spectrogram.panel.repaint();
                return true;
            }

        };

        /*
        final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true,
                true);
        final DataLine.Info dataLineInfo = new DataLine.Info(
                TargetDataLine.class, format);
        TargetDataLine line;
        line = (TargetDataLine) mixer.getLine(dataLineInfo);
        final int numberOfSamples = bufferSize;
        line.open(format, numberOfSamples);
        line.start();
        final AudioInputStream stream = new AudioInputStream(line);

        JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
        */
        soundDetector = new SoundDetector(initialzeThresholdSlider());
        add(soundDetector, BorderLayout.EAST);
        //add(new Spectrogram(null, inputPanel), BorderLayout.WEST);
        pitchDetector = new PitchDetectorExample();
        //add(pitchDetector, BorderLayout.CENTER);

        spectrogram = new Spectrogram();
        spectrogram.fftProcessor = fftProcessor;
        add(spectrogram, BorderLayout.CENTER);
    }

    private JSlider initialzeThresholdSlider() {
        JSlider thresholdSlider = new JSlider(-120,0);
        thresholdSlider.setValue((int)threshold);
        thresholdSlider.setPaintLabels(true);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.setMajorTickSpacing(20);
        thresholdSlider.setMinorTickSpacing(10);
        thresholdSlider.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                threshold = source.getValue();
                soundDetector.graphPanel.setThresholdLevel(threshold);
                if (!source.getValueIsAdjusting()) {
                    try {
                        setNewMixer(currentMixer);
                    } catch (LineUnavailableException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        });
        return thresholdSlider;
    }

    private void setNewMixer(Mixer mixer) throws LineUnavailableException {
        if(dispatcher!= null){
            dispatcher.stop();
        }
        currentMixer = mixer;

        soundDetector.textArea.append("Started listening with " + Shared.toLocalString(mixer.getMixerInfo().getName()) + "\n\tparams: " + threshold + "dB\n");

        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true,
                true);
        DataLine.Info dataLineInfo = new DataLine.Info(
                TargetDataLine.class, format);
        TargetDataLine line;
        line = (TargetDataLine) mixer.getLine(dataLineInfo);
        int numberOfSamples = bufferSize;
        line.open(format, numberOfSamples);
        line.start();
        AudioInputStream stream = new AudioInputStream(line);

        JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
        // create a new dispatcher
        dispatcher = new AudioDispatcher(audioStream, bufferSize,
                overlap);

        // add a processor, handle percussion event.
        silenceDetector = new SilenceDetector(threshold,false);
        soundDetector.setSilenceDetector(silenceDetector);
        dispatcher.addAudioProcessor(silenceDetector);
        dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, sampleRate, bufferSize, pitchDetector));

        dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, sampleRate, bufferSize, spectrogram));
        dispatcher.addAudioProcessor(fftProcessor);
        soundDetector.setThreshold(threshold);
        dispatcher.addAudioProcessor(soundDetector);

        // run the dispatcher (on a new thread).
        new Thread(dispatcher,"Audio dispatching").start();
    }

    public static void main(String... strings) throws InterruptedException,
            InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    //ignore failure to set default look en feel;
                }
                JFrame frame = new EssentialFeaturesFrame();
                frame.pack();
                frame.setVisible(true);
            }
        });

    }
}