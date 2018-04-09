package vowelcapt.views;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import vowelcapt.utils.*;
import vowelcapt.utils.helpers.HasPitchBeenDetected;
import vowelcapt.utils.helpers.IsRecording;
import vowelcapt.utils.helpers.SilenceDetectorCurrentSPL;

import javax.sound.sampled.*;
import java.io.*;


// TODO: find a way for the graph panel to update nicely - will be necessary in the volume threshold setting page
public class SampleLesson extends Application implements PitchDetectionHandler {

    private final Button recordButton = new Button("Record");
    private final Button playBackButton = new Button("Play back");
    private final Button listenButton = new Button("Listen");
    private final GraphPanel graphPanel = new GraphPanel(-80);
    private FormantUtils formantUtils = new FormantUtils();
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream vowelOut;
    private Label formantInfo = new Label();
    private SilenceDetector silenceDetector = new SilenceDetector();

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

        HBox hbListenButton = new HBox(15);
        hbListenButton.setAlignment(Pos.BOTTOM_RIGHT);
        hbListenButton.getChildren().add(listenButton);
        grid.add(hbListenButton, 3, 0);

        graphPanel.setSize(300, 400);
        final SwingNode swingNode = new SwingNode();
        swingNode.setContent(graphPanel);

        Pane pane = new Pane();
        pane.getChildren().add(swingNode);
        grid.add(pane, 4, 5);

        listenButton.setOnAction(e -> {
            recordButton.setDisable(true);
            listenButton.setDisable(true);
            String bip = "C:/Test/sada.wav";
            Media hit = new Media(new File(bip).toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(hit);
            mediaPlayer.play();

            recordButton.setDisable(false);
            listenButton.setDisable(false);
        });

        playBackButton.setDisable(true);

        recordButton.setOnAction(e -> {
            if (!IsRecording.get()) {
                IsRecording.set(true);
                HasPitchBeenDetected.set(false);
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
                    dispatcher.addAudioProcessor(silenceDetector);

                    new Thread(dispatcher, "Audio dispatching").start();

                    Runnable runner = new Runnable() {
                        int bufferSize = (int) format.getSampleRate()
                                * format.getFrameSize();
                        byte buffer[] = new byte[bufferSize];

                        public void run() {
                            out = new ByteArrayOutputStream();
                            vowelOut = new ByteArrayOutputStream();
                            while (IsRecording.get()) {
                                //TODO: this buffer writing might be the cause of the clipping issue
                                int count =
                                        line.read(buffer, 0, buffer.length);
                                if (count > 0) {
                                    out.write(buffer, 0, count);
                                    if (SilenceDetectorCurrentSPL.get() > -70 && HasPitchBeenDetected.get()) {
                                        vowelOut.write(buffer, 0, count);
                                    }
                                }
                            }

                            try {
                                out.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            line.stop();
                            line.close();
                            byte audio[] = out.toByteArray();
                            InputStream input =
                                    new ByteArrayInputStream(audio);
                            final AudioInputStream ais = new AudioInputStream(input, format, audio.length / format.getFrameSize());

                            //Formants
                            byte vowelAudio[] = vowelOut.toByteArray();
                            InputStream vowelInput =
                                    new ByteArrayInputStream(vowelAudio);
                            final AudioInputStream aisVowel = new AudioInputStream(vowelInput, format, vowelAudio.length / format.getFrameSize());
                            try {
                                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File("AK/RecordAudio3.wav"));
                                AudioSystem.write(aisVowel, AudioFileFormat.Type.WAVE, new File("AK/VowelPartTrial.wav"));
                                ais.close();
                                aisVowel.close();
                                input.close();
                                vowelInput.close();
                                dispatcher.stop();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            formantUtils.findFormants('a');
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
        primaryStage.setWidth(600);
        primaryStage.setHeight(700);
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 22050;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate,
                sampleSizeInBits, channels, signed, bigEndian);
    }

    @Override
    public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
        graphPanel.addDataPoint(silenceDetector.currentSPL(), System.currentTimeMillis());
        SilenceDetectorCurrentSPL.set(silenceDetector.currentSPL());
        if (pitchDetectionResult.getPitch() != -1) {
            HasPitchBeenDetected.set(true);
            double timeStamp = audioEvent.getTimeStamp();
            float pitch = pitchDetectionResult.getPitch();
            float probability = pitchDetectionResult.getProbability();
            double rms = audioEvent.getRMS() * 100;
            String message = String.format("Pitch detected at %.2fs: %.2fHz ( %.2f probability, RMS: %.5f )\n", timeStamp, pitch, probability, rms);
            System.out.println(message);
        }
    }
}
