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
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import vowelcapt.utils.*;
import vowelcapt.utils.helpers.HasPitchBeenDetected;
import vowelcapt.utils.helpers.IsRecording;
import vowelcapt.utils.helpers.SilenceDetectorCurrentSPL;

import javax.sound.sampled.*;
import java.io.*;


// TODO: remove graph panel from this view entirely once ThresholdSetter is properly implemented
// TODO: make an AudioProcessor version of this
// TODO: link the bubble chart to results from the analysis
// TODO: make the layout pretty
public class PronunciationExercise extends Application implements PitchDetectionHandler {

    private final Button recordButton = new Button("Record");
    private final Button playBackButton = new Button("Play back");
    private final Button listenButton = new Button("Listen");
    private final GraphPanel graphPanel = new GraphPanel(-80);
    private final BubbleChart formantChart = setUpFormantChart();
    private XYChart.Series userResults = new XYChart.Series();
    private XYChart.Series nativeResults = new XYChart.Series();
    private FormantUtils formantUtils = new FormantUtils();
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream vowelOut;
    private Label formantInfo = new Label();
    private SilenceDetector silenceDetector = new SilenceDetector();
    private AccountUtils accountUtils = new AccountUtils();
    private String word = "võõp";
    private Account currentAccount = new Account("test", "test", "male");
    private char vowel = 'õ';
    private String userPath = "resources/accounts/test/";
    private double threshold = -80;

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label pronounceTheWordLabel = new Label("Listen to the recording and \npronounce the word:");
        grid.add(pronounceTheWordLabel, 0, 0);

        Label wordLabel = new Label(word);
        wordLabel.setFont(Font.font("Arial", 30));
        grid.add(wordLabel, 0, 1);

        VBox animationVBox = new VBox();

        Animation ani = new AnimatedGif("resources/animations/" + vowel + ".gif", 1000);
        ani.setCycleCount(1);

        Button playBtn = new Button("Play animation of " + vowel);
        playBtn.setOnAction(e -> ani.play());

        HBox playHBox = new HBox();
        playHBox.setAlignment(Pos.CENTER);
        playHBox.getChildren().add(playBtn);

        animationVBox.getChildren().addAll(ani.getView(), playHBox);
        grid.add(animationVBox, 0, 2);

        //grid.add(formantInfo, 0, 2);

        HBox hBox = new HBox(25);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(listenButton, recordButton, playBackButton);
        grid.add(hBox, 0, 3);

        graphPanel.setSize(300, 400);
        final SwingNode swingNode = new SwingNode();
        swingNode.setContent(graphPanel);

        Pane pane = new Pane();
        pane.getChildren().add(swingNode);
        //grid.add(pane, 4, 5);

        listenButton.setOnAction(e -> {
            recordButton.setDisable(true);
            listenButton.setDisable(true);
            String pronunciationFileLocation = "resources/sample_sounds/pronunciation/" + word + ".wav";
            Media pronunciationFile = new Media(new File(pronunciationFileLocation).toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(pronunciationFile);
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
                final AudioFormat format = AudioUtils.getAudioFormat();
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
                                int count =
                                        line.read(buffer, 0, buffer.length);
                                if (count > 0) {
                                    byte secondBuffer[] = buffer;
                                    out.write(buffer, 0, count);
                                    if (SilenceDetectorCurrentSPL.get() > threshold && HasPitchBeenDetected.get()) {
                                        vowelOut.write(secondBuffer, 0, count);
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
                                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(userPath + vowel + "_last.wav"));
                                AudioSystem.write(aisVowel, AudioFileFormat.Type.WAVE, new File(userPath + vowel + "_justVowel.wav"));
                                ais.close();
                                aisVowel.close();
                                input.close();
                                vowelInput.close();
                                dispatcher.stop();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }

                            double[] formantResults = formantUtils.findFormants(currentAccount.getUserName(),
                                    currentAccount.getGender(), vowel);
                            //String recordingPath = userPath + vowel + "_last.wav";
                            //String waveFormPath = userPath + vowel + "_img.png";
                            System.out.println(formantUtils.isWithinStandardDeviation(vowel,
                                    currentAccount.getGender(), formantResults[0], formantResults[1]));
                            //AudioWaveformCreator audioWaveformCreator = new AudioWaveformCreator(new File(recordingPath), waveFormPath);
                            // boolean isWaveFormCreated = audioWaveformCreator.createWaveImage();
                            //System.out.println(isWaveFormCreated);
                            // TODO: don't save this result but things here should be logged
                            //accountUtils.saveResult(currentAccount.getUserName(), vowel, false, formantResults);
                        }
                    };
                    Thread captureThread = new Thread(runner);
                    captureThread.start();
                } catch (LineUnavailableException e1) {
                    e1.printStackTrace();
                }
                //userResults.getData().clear();
                userResults.getData().add(new XYChart.Data(1711, 586, 20));
                //formantChart.getData().add(userResults);
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
                final AudioFormat format = AudioUtils.getAudioFormat();
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

        nativeResults = setUpNativePronunciationBubble();
        formantChart.getData().add(nativeResults);

        userResults.setName("Your pronunciation of /" + vowel + "/");
        userResults.getData().add(new XYChart.Data(2000, 500, 10));
        formantChart.getData().add(userResults);
        grid.add(formantChart, 1, 0, 1, 3);

        Scene scene = new Scene(grid);
        primaryStage.setTitle("EstonianVowelCAPT - Pronunciation: " + vowel);
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

    @Override
    public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
        //graphPanel.addDataPoint(silenceDetector.currentSPL(), System.currentTimeMillis());
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

    private BubbleChart setUpFormantChart() {
        // Example of creating bubble chart https://www.tutorialspoint.com/javafx/bubble_chart.htm

        NumberAxis xAxis = new NumberAxis(500, 3000, 500);
        xAxis.setLabel("F2");

        NumberAxis yAxis = new NumberAxis(200, 900, 100);
        yAxis.setLabel("F1");

        BubbleChart formantChart = new BubbleChart(xAxis, yAxis);

        return formantChart;
    }

    private XYChart.Series setUpNativePronunciationBubble() {
        XYChart.Series series = new XYChart.Series();
        series.setName("Native pronunciation of /" + vowel + "/");

        series.getData().add(new XYChart.Data(1111, 586, 80));
        series.getData().add(new XYChart.Data(25, 40, 5));
        series.getData().add(new XYChart.Data(40, 50, 9));
        series.getData().add(new XYChart.Data(55, 60, 7));
        series.getData().add(new XYChart.Data(70, 70, 9));
        series.getData().add(new XYChart.Data(85, 80, 6));

        return series;
    }


    public void initializeAndStart(Stage primaryStage, Account account, String word, char vowel) {
        this.word = word;
        currentAccount = account;
        threshold = account.getThreshold();
        this.vowel = vowel;
        userPath = "resources/accounts/" + account.getUserName() + "/";
        start(primaryStage);
    }
}
