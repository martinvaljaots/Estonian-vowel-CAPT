package vowelcapt.necessaryexamples;

import be.tarsos.dsp.*;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;
import vowelcapt.panels.*;
import vowelcapt.panels.InputPanel;
import vowelcapt.panels.PitchDetectorExample;
import vowelcapt.panels.Shared;
import vowelcapt.panels.SoundDetector;
import vowelcapt.panels.Spectrogram;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class EssentialFeaturesFrame extends JFrame {

    float sampleRate = 44100;
    int bufferSize = 1024 * 4;
    int overlap = 768 * 4;
    Mixer currentMixer;
    vowelcapt.panels.SoundDetector soundDetector;
    AudioDispatcher dispatcher;
    double threshold = -80;
    SilenceDetector silenceDetector;
    vowelcapt.panels.PitchDetectorExample pitchDetector;
    vowelcapt.panels.Spectrogram spectrogram;
    AudioProcessor fftProcessor;
    Capture audioCapture;
    public double pitch;
    private static boolean isRecording = false;

    ByteArrayOutputStream out;


    public EssentialFeaturesFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Essential features");
        audioCapture = new Capture();
        final JButton capture = new JButton("Lindista");

        ActionListener captureListener =
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(!IsRecording.get()) {
                            IsRecording.set(true);
                            capture.setText("Lindistamine...");
                        } else {
                            try {
                                setNewMixer(currentMixer);
                            } catch (LineUnavailableException e1) {
                                e1.printStackTrace();
                            }
                            IsRecording.set(false);
                            capture.setText("Lindista");
                        }
                        //captureAudio();
                    }
                };
        capture.addActionListener(captureListener);
        add(capture, BorderLayout.LINE_START);

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
        System.out.println("HEY");
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
        dispatcher.addAudioProcessor(new Recorder(line, format));
        dispatcher.addAudioProcessor(soundDetector);

        // run the dispatcher (on a new thread).
        new Thread(dispatcher,"Audio dispatching").start();
       // handleRecording(format, line);
        System.out.println("YES DONE");
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


    public void handleRecording(AudioFormat format, TargetDataLine line) {
        Runnable runner = new Runnable() {
            int bufferSize = (int)format.getSampleRate()
                    * format.getFrameSize();
            byte buffer[] = new byte[bufferSize];

            public void run() {
                System.out.println("HEEEEEDSAE");
                out = new ByteArrayOutputStream();
                System.out.println(IsRecording.get());
                try {
                    while (IsRecording.get()) {
                        System.out.println("RECORDING AUDIO");
                        int count =
                                line.read(buffer, 0, buffer.length);
                        if (count > 0) {
                            out.write(buffer, 0, count);
                        }
                    }
                    out.close();
                } catch (IOException e) {
                    System.err.println("I/O problems: " + e);
                    System.exit(-1);
                }
            }
        };
        runner.run();
    }
}