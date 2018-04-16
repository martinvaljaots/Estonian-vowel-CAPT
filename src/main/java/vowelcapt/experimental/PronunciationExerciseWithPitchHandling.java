package vowelcapt.experimental;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import com.sun.javafx.charts.Legend;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import vowelcapt.utils.account.Account;
import vowelcapt.utils.account.AccountUtils;
import vowelcapt.utils.animation.AnimatedGif;
import vowelcapt.utils.animation.Animation;
import vowelcapt.utils.audio.*;
import vowelcapt.utils.formants.FormantResults;
import vowelcapt.utils.formants.FormantUtils;
import vowelcapt.utils.formants.VowelInfo;
import vowelcapt.views.ExerciseSelection;

import javax.sound.sampled.*;
import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;


// TODO: remove graph panel from this view entirely once ThresholdSetter is properly implemented
// TODO: make an AudioProcessor version of this
// TODO: link the bubble chart to results from the analysis
// TODO: bubble chart ellipses correctness and checking
// TODO: make the layout pretty

// Audio processing in this class is partly based on examples from TarsosDSP https://github.com/JorenSix/TarsosDSP
public class PronunciationExerciseWithPitchHandling extends Application implements PitchDetectionHandler {

    private final Button recordButton = new Button("Record");
    private final Button playBackButton = new Button("Play back");
    private final Button listenButton = new Button("Listen");
    private final Button quitButton = new Button("Back to exercise selection");
    private final GraphPanel graphPanel = new GraphPanel(-80);
    private final BubbleChart<Number, Number> formantChart = setUpFormantChart();
    private XYChart.Series<Number, Number> userResults = new XYChart.Series<>();
    private FormantUtils formantUtils = new FormantUtils();
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream vowelOut;
    private Label recordingInfo = new Label();
    private Label resultsInfo = new Label(" \n ");
    private SilenceDetector silenceDetector = new SilenceDetector();
    private AccountUtils accountUtils = new AccountUtils();
    //TODO: remove these before user testing starts
    private String word = "võõp";
    private Account currentAccount = new Account("test", "test", "male");
    private char vowel = 'õ';
    private String userPath = "resources/accounts/test/";
    private double threshold = -80;
    private String translation;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setX(200);
        primaryStage.setY(50);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label pronounceTheWordLabel = new Label("Listen to the recording and pronounce the word:");
        pronounceTheWordLabel.setFont(Font.font("Arial", 20));
        grid.add(pronounceTheWordLabel, 0, 0, 2, 1);

        Label wordLabel = new Label(word);
        wordLabel.setFont(Font.font("Arial", 40));
        grid.add(wordLabel, 0, 1);

        Label yourResultsLabel = new Label("Your results");
        yourResultsLabel.setFont(Font.font("Arial", 40));
        Label explanationGuideLabel = new Label("( Move your cursor to the graph for instructions )");

        VBox graphExplainingVbox = new VBox(15);
        graphExplainingVbox.setAlignment(Pos.CENTER);
        graphExplainingVbox.getChildren().addAll(yourResultsLabel, explanationGuideLabel);
        grid.add(graphExplainingVbox, 2, 1, 1, 2);

        Label translationLabel = new Label("( English: " + translation + " )");
        grid.add(translationLabel, 0, 2);

        VBox animationVBox = new VBox(15);
        Animation ani = new AnimatedGif("resources/animations/" + vowel + ".gif", 1000);
        ani.setCycleCount(1);

        Button playAnimationBtn = new Button("Play animation of " + vowel);
        playAnimationBtn.setOnAction(e -> ani.play());

        HBox playHBox = new HBox();
        playHBox.setAlignment(Pos.CENTER);
        playHBox.getChildren().add(playAnimationBtn);

        animationVBox.getChildren().addAll(ani.getView(), playHBox);
        grid.add(animationVBox, 0, 3);

        HBox audioButtonsHbox = new HBox(25);
        audioButtonsHbox.setAlignment(Pos.CENTER);
        audioButtonsHbox.getChildren().addAll(listenButton, recordButton, playBackButton);
        grid.add(audioButtonsHbox, 0, 4);

        HBox recordingInfoHbox = new HBox(0);
        recordingInfoHbox.setAlignment(Pos.CENTER);
        recordingInfoHbox.getChildren().add(recordingInfo);
        grid.add(recordingInfoHbox, 0, 5);

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
                WasSoundIntensityAboveThreshold.set(false);
                IsRecording.set(true);
                HasPitchBeenDetected.set(false);
                playBackButton.setDisable(true);
                quitButton.setDisable(true);
                recordButton.setText("Stop recording");
                resultsInfo.setText(" \n ");
                recordingInfo.setText("Recording...");

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
                    AudioDispatcher dispatcher = new AudioDispatcher(audioStream, 1024,
                            0);

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
                                        WasSoundIntensityAboveThreshold.set(true);
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
                            boolean isWithinStandardDeviation = formantUtils.isWithinStandardDeviation(vowel,
                                    currentAccount, formantResults[0], formantResults[1]);
                            System.out.println(isWithinStandardDeviation);
                            FormantResults results = new FormantResults(formantResults[0], formantResults[1], isWithinStandardDeviation);
                            formantUtils.addLastResults(results);
                            // TODO: don't save this result but things here should be logged
                            //accountUtils.saveToLog(currentAccount.getUserName(), vowel, false, formantResults);
                        }
                    };
                    Thread captureThread = new Thread(runner);
                    captureThread.start();
                } catch (LineUnavailableException e1) {
                    e1.printStackTrace();
                }
            } else {
                IsRecording.set(false);
                recordingInfo.setText("Recording finished.");
                recordButton.setText("Record");
                playBackButton.setDisable(false);
                quitButton.setDisable(false);
                Optional<FormantResults> lastResults = formantUtils.getLastResults();
                lastResults.ifPresent(this::updateChart);
            }
        });

        playBackButton.setOnAction(e -> {
            recordingInfo.setText(" \n ");
            recordButton.setDisable(true);
            listenButton.setDisable(true);
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
                            listenButton.setDisable(false);
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

        userResults.setName("Your pronunciation of /" + vowel + "/");
        formantChart.getData().add(userResults);
        setUpNativePronunciationRangesOnChart();
        formantChart.setPrefWidth(800);
        Tooltip chartToolTip = new Tooltip("This graph represents your pronunciation.\n" +
                "F1 represents your tongue position high to low,\n" +
                "F2 represents your tongue position back to front.\n" +
                "The green bubble represents the target area for your pronunciation.\n" +
                "Try different mouth positions as you pronounce the vowel.");
        chartToolTip.setFont(Font.font(14));

        //https://coderanch.com/t/622070/java/control-Tooltip-visible-time-duration
        formantChart.setOnMouseEntered(event -> {
            Point2D p = formantChart.localToScreen(formantChart.getLayoutBounds().getMaxX() - 200,
                    formantChart.getLayoutBounds().getMinY() - 125);
            chartToolTip.show(formantChart, p.getX(), p.getY());
        });
        formantChart.setOnMouseExited(event -> chartToolTip.hide());

        quitButton.setOnAction(e -> new ExerciseSelection().initializeAndStart(primaryStage, currentAccount));
        HBox quitButtonHbox = new HBox();
        quitButtonHbox.setAlignment(Pos.BOTTOM_RIGHT);
        quitButtonHbox.getChildren().add(quitButton);

        HBox resultsInfoHbox = new HBox();
        resultsInfoHbox.setAlignment(Pos.CENTER);
        resultsInfoHbox.getChildren().add(resultsInfo);
        grid.add(resultsInfoHbox, 2, 4);

        HBox resultsAndQuitHbox = new HBox(15);
        resultsAndQuitHbox.setAlignment(Pos.CENTER_RIGHT);
        resultsAndQuitHbox.getChildren().addAll(resultsInfoHbox, quitButtonHbox);

        VBox chartAndQuitBtnVbox = new VBox(5);
        chartAndQuitBtnVbox.getChildren().addAll(formantChart, resultsAndQuitHbox);
        grid.add(chartAndQuitBtnVbox, 1, 3, 2, 4);


        Scene scene = new Scene(grid);
        primaryStage.setTitle("EstonianVowelCAPT - Pronunciation: " + vowel);
        primaryStage.setScene(scene);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(750);
        primaryStage.show();
    }

    private void updateChart(FormantResults results) {
        System.out.println(results);
        double firstFormant = results.getFirstFormantAverage();
        double secondFormant = results.getSecondFormantAverage();
        String resultsInfoMessage = "";
        if (results.isWithinStandardDeviation()) {
            resultsInfoMessage = "Your last pronunciation was within the range of a native speaker!\n ";
        }

        if (!results.isWithinStandardDeviation()) {
            if (firstFormant == 0 || secondFormant == 0) {
                if (WasSoundIntensityAboveThreshold.get()) {
                    resultsInfoMessage = "The application was unable to detect a pronunciation of /" + vowel + "/." +
                            " Please try again.\n ";
                } else {
                    resultsInfoMessage = "The application was unable to detect a pronunciation of /" + vowel + "/." +
                            "\nPlease try again and consider adjusting your microphone volume.";
                }
            } else {
                resultsInfoMessage = "Your last pronunciation was outside the range of a native speaker. Keep trying.\n ";
            }
        }
        resultsInfo.setText(resultsInfoMessage);

        userResults.getData().add(new XYChart.Data<>(
                secondFormant,
                firstFormant,
                10));

        Set<Node> nodes = formantChart.lookupAll(".series0");
        for (Node n : nodes) {
            n.setStyle("-fx-bubble-fill:  red; "
                    + "-fx-background-color: radial-gradient(center 50% 50%, radius 80%, "
                    + "derive(-fx-bubble-fill,20%), derive(-fx-bubble-fill,-30%));");
        }
    }

    //TODO: remove this main method
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

    private BubbleChart<Number, Number> setUpFormantChart() {

        NumberAxis xAxis = new NumberAxis(500, 3000, 500);
        xAxis.setLabel("F2");

        NumberAxis yAxis = new NumberAxis(200, 900, 100);
        yAxis.setLabel("F1");

        return new BubbleChart<>(xAxis, yAxis);
    }

    private void setUpNativePronunciationRangesOnChart() {
        List<VowelInfo> vowels = formantUtils.getVowels(currentAccount.getGender());

        for (VowelInfo vowel : vowels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            if (vowel.getVowel() == this.vowel) {
                series.setName("Native pronunciation of /" + vowel.getVowel() + "/");
            } else {
                series.setName("");
            }

            series.getData().add(new XYChart.Data<>(
                    vowel.getSecondFormantMean(),
                    vowel.getFirstFormantMean(),
                    (vowel.getFirstFormantSd() + vowel.getSecondFormantSd()) / 2));

            formantChart.getData().add(series);
            for (XYChart.Data<Number, Number> data : series.getData()) {
                Label label = new Label(String.valueOf(vowel.getVowel()));
                label.setAlignment(Pos.CENTER);
                Node bubble = data.getNode();
                if (bubble != null && bubble instanceof StackPane) {
                    StackPane region = (StackPane) bubble;
                    region.getChildren().add(label);
                }
            }
        }


        //https://gist.github.com/d0tplist/558856af06c373b9f3f9
        Set<Node> items = formantChart.lookupAll("Label.chart-legend-item");
        if (!items.isEmpty()) {
            Optional<Node> nodeOptional = items.stream().findFirst();
            try {
                Node node = nodeOptional.get();
                Parent legendParent = node.getParent();
                final Legend existingLegend = (Legend) legendParent;

                existingLegend.getItems().removeIf(item -> item.getText().equals(""));
                System.out.println(existingLegend.getItems());
            } catch (ClassCastException ex) {
                ex.printStackTrace();
            }
        }

        //https://stackoverflow.com/a/38953486
        for (int i = 1; i < 10; i++) {
            Set<Node> nodes = formantChart.lookupAll(".series" + i);
            for (Node n : nodes) {
                StackPane bubble = (StackPane) n;
                System.out.println(bubble.getChildren().get(0));
                Label bubbleLabel = (Label) bubble.getChildren().get(0);

                if (bubbleLabel.getText().equals(String.valueOf(vowel))) {
                    n.setStyle("-fx-bubble-fill:  #7fff00aa; "
                            + "-fx-background-color: radial-gradient(center 50% 50%, radius 80%, "
                            + "derive(-fx-bubble-fill,20%), derive(-fx-bubble-fill,-30%));");

                    //TODO: maybe a better color
                } else {
                    n.setStyle("-fx-bubble-fill:  #FFFB00aa; "
                            + "-fx-background-color: radial-gradient(center 50% 50%, radius 80%, "
                            + "derive(-fx-bubble-fill,20%), derive(-fx-bubble-fill,-30%));");
                }
            }
        }

        Set<Node> nodes = formantChart.lookupAll("Label.chart-legend-item");
        for (Node item : nodes) {
            Label label = (Label) item;
            if (label.getText().equals("Native pronunciation of /" + vowel + "/")) {
                label.getGraphic().setStyle("-fx-bubble-fill:  #7fff00aa; "
                        + "-fx-background-color: radial-gradient(center 50% 50%, radius 80%, "
                        + "derive(-fx-bubble-fill,20%), derive(-fx-bubble-fill,-30%));");
            } else {
                label.getGraphic().setStyle("-fx-bubble-fill:  red; "
                        + "-fx-background-color: radial-gradient(center 50% 50%, radius 80%, "
                        + "derive(-fx-bubble-fill,20%), derive(-fx-bubble-fill,-30%));");
            }
        }
    }

    public void initializeAndStart(Stage primaryStage, Account account, String word, String translation, char vowel) {
        this.word = word;
        this.translation = translation;
        currentAccount = account;
        threshold = account.getThreshold();
        this.vowel = vowel;
        userPath = "resources/accounts/" + account.getUserName() + "/";
        start(primaryStage);
    }
}
