package vowelcapt.views;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import de.fau.cs.jstk.exceptions.MalformedParameterStringException;
import de.fau.cs.jstk.framed.*;
import de.fau.cs.jstk.io.FrameOutputStream;
import de.fau.cs.jstk.sampled.AudioFileReader;
import de.fau.cs.jstk.sampled.AudioSource;
import de.fau.cs.jstk.sampled.RawAudioFormat;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import vowelcapt.helpers.IsRecording;

import javax.sound.sampled.*;
import java.io.*;
import java.util.Arrays;

public class SampleLesson extends Application implements PitchDetectionHandler {

    private final Button recordButton = new Button("Record");
    private final Button playBackButton = new Button ("Play back");
    private ByteArrayOutputStream out;
    private Label formantInfo = new Label();
    private static String formants = "";

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label whatToPronounce = new Label("Pronounce the word: ");
        grid.add(whatToPronounce, 0, 0);

        Label word = new Label("sada");
        grid.add(word, 1, 0);

        grid.add(formantInfo, 0, 2);

        HBox hbRecordButton = new HBox(15);
        hbRecordButton.setAlignment(Pos.BOTTOM_RIGHT);
        hbRecordButton.getChildren().add(recordButton);
        grid.add(hbRecordButton, 0, 1);

        HBox hbPlayBackButton = new HBox(15);
        hbPlayBackButton.setAlignment(Pos.BOTTOM_RIGHT);
        hbPlayBackButton.getChildren().add(playBackButton);
        grid.add(hbPlayBackButton, 1, 1);

        playBackButton.setDisable(true);

        recordButton.setOnAction(e -> {
            if (!IsRecording.get()) {
                IsRecording.set(true);
                playBackButton.setDisable(true);
                recordButton.setText("Stop recording");
                final AudioFormat format = getAudioFormat();
                DataLine.Info info = new DataLine.Info(
                        TargetDataLine.class, format);

                try {
                    final TargetDataLine line;
                    line = (TargetDataLine)
                            AudioSystem.getLine(info);
                    line.open(format);
                    line.start();

                    final AudioInputStream stream = new AudioInputStream(line);

                    JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
                    // create a new dispatcher
                    AudioDispatcher dispatcher = new AudioDispatcher(audioStream, 1024,
                            0);

                    // add a processor
                    dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN,
                            44100, 1024, this));

                    new Thread(dispatcher,"Audio dispatching").start();

                    Runnable runner = new Runnable() {
                        int bufferSize = (int) format.getSampleRate()
                                * format.getFrameSize();
                        byte buffer[] = new byte[bufferSize];

                        public void run() {
                            System.out.println(bufferSize);
                            out = new ByteArrayOutputStream();
                            while (IsRecording.get()) {
                                System.out.println("yes, recording");
                                int count =
                                        line.read(buffer, 0, buffer.length);
                                if (count > 0) {
                                    out.write(buffer, 0, count);
                                }
                            }

                            try {
                                out.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            //ais.close();
                            line.stop();
                            line.close();
                            byte audio[] = out.toByteArray();
                            InputStream input =
                                    new ByteArrayInputStream(audio);
                            final AudioInputStream ais = new AudioInputStream(input, format, audio.length / format.getFrameSize());
                            try {
                                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File("C:/Test/RecordAudio3.wav"));
                                ais.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            findFormants();
                            System.out.println("Supposedly done");
                        }
                    };
                    Thread captureThread = new Thread(runner);
                    captureThread.start();
                } catch (LineUnavailableException e1) {
                    e1.printStackTrace();
                }
            } else {
                IsRecording.set(false);
                formantInfo.setText("Recording stopped.");
                recordButton.setText("Record");
                playBackButton.setDisable(false);
                formantInfo.setText(formants);
            }
        });

        playBackButton.setOnAction(e -> {
            recordButton.setDisable(true);
            try {
                byte audio[] = out.toByteArray();
                InputStream input =
                        new ByteArrayInputStream(audio);
                final AudioFormat format = getAudioFormat();
                final AudioInputStream ais =
                        new AudioInputStream(input, format,
                                audio.length / format.getFrameSize());
                DataLine.Info info = new DataLine.Info(
                        SourceDataLine.class, format);
                final SourceDataLine line = (SourceDataLine)
                        AudioSystem.getLine(info);
                line.open(format);
                line.start();

                Runnable runner = new Runnable() {
                    int bufferSize = (int) format.getSampleRate()
                            * format.getFrameSize();
                    byte buffer[] = new byte[bufferSize];

                    public void run() {
                        try {
                            int count;
                            while ((count = ais.read(
                                    buffer, 0, buffer.length)) != -1) {
                                if (count > 0) {
                                    line.write(buffer, 0, count);
                                }
                            }
                            line.drain();
                            line.close();
                            recordButton.setDisable(false);
                        } catch (IOException e) {
                            System.err.println("I/O problems: " + e);
                            System.exit(-3);
                        }
                    }
                };
                Thread playThread = new Thread(runner);
                playThread.start();
            } catch (LineUnavailableException l) {
                System.err.println("Line unavailable: " + l);
                System.exit(-4);
            }
        });

        Scene scene = new Scene(grid);
        primaryStage.setTitle("EstonianVowelCAPT");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    private void findFormants() {
        String[] trial = new String[]{"C:/Test/RecordAudio3.wav", "3"};
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
            formants = "";

            while (fs.read(buf)) {
                System.out.println(Arrays.toString(buf));
                formants += Arrays.toString(buf) + "\n";
            }

        } catch (UnsupportedAudioFileException | IOException | MalformedParameterStringException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate,
                sampleSizeInBits, channels, signed, bigEndian);
    }

    @Override
    public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
        if(pitchDetectionResult.getPitch() != -1){
            double timeStamp = audioEvent.getTimeStamp();
            float pitch = pitchDetectionResult.getPitch();
            float probability = pitchDetectionResult.getProbability();
            double rms = audioEvent.getRMS() * 100;
            String message = String.format("Pitch detected at %.2fs: %.2fHz ( %.2f probability, RMS: %.5f )\n", timeStamp,pitch,probability,rms);
            System.out.println(message);
        }
    }
}
