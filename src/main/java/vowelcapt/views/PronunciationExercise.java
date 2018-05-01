package vowelcapt.views;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import com.sun.javafx.charts.Legend;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import vowelcapt.utils.audio.AudioUtils;
import vowelcapt.utils.audio.IsRecording;
import vowelcapt.utils.audio.SilenceDetectorCurrentSPL;
import vowelcapt.utils.audio.WasSoundIntensityAboveThreshold;
import vowelcapt.utils.formants.EllipseBubbleChart;
import vowelcapt.utils.formants.FormantResults;
import vowelcapt.utils.formants.FormantUtils;
import vowelcapt.utils.formants.VowelInfo;

import javax.sound.sampled.*;
import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Audio processing in this class is partly based on examples from TarsosDSP https://github.com/JorenSix/TarsosDSP
public class PronunciationExercise extends Application implements AudioProcessor {

    private final Button recordButton = new Button("Record");
    private final Button playBackButton = new Button("Play back");
    private final Button listenButton = new Button("Listen");
    private final Button quitButton = new Button("Back to exercise selection");
    private EllipseBubbleChart<Number, Number> formantChart;
    private XYChart.Series<Number, Number> userResults = new XYChart.Series<>();
    private FormantUtils formantUtils = new FormantUtils();
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream vowelOut;
    private Label recordingInfo = new Label("Click the record button to start recording your pronunciation.\n" +
            "Click the button again to stop recording and receive your result.");
    private Label resultsInfo = new Label(" \n ");
    private SilenceDetector silenceDetector = new SilenceDetector();
    private AccountUtils accountUtils = new AccountUtils();
    private String translation;
    private String word;
    private Account currentAccount;
    private char vowel;
    private String userPath;
    private double threshold;
    private MediaPlayer mediaPlayer;
    private int attemptCounter = 0;
    private String[] attemptsSymbols = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "X", "Y", "Z"};

    @Override
    public void start(Stage primaryStage) {
        accountUtils.saveToLog(currentAccount.getUserName(), Collections.singletonList(currentAccount.toString()
                + " navigated to " + vowel + " pronunciation exercise"));
        primaryStage.setX(100);
        primaryStage.setY(50);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(15, 25, 15, 25));

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
        grid.add(recordingInfoHbox, 0, 5, 1, 3);

        recordButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14");

        listenButton.setOnAction(e -> {
            recordButton.setDisable(true);
            listenButton.setDisable(true);
            if (!recordingInfo.getText().contains("Click the record button to start recording your pronunciation.")) {
                recordingInfo.setText(" \n ");
            }
            String pronunciationFileLocation = "resources/sample_sounds/pronunciation/" + word + ".wav";
            Media pronunciationFile = new Media(new File(pronunciationFileLocation).toURI().toString());
            mediaPlayer = new MediaPlayer(pronunciationFile);
            mediaPlayer.play();

            recordButton.setDisable(false);
            listenButton.setDisable(false);
        });

        playBackButton.setDisable(true);

        recordButton.setOnAction(e -> {
            if (!IsRecording.get()) {
                accountUtils.saveToLog(currentAccount.getUserName(), Collections.singletonList("Started recording."));
                WasSoundIntensityAboveThreshold.set(false);
                IsRecording.set(true);
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

                    dispatcher.addAudioProcessor(this);
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
                                    if (SilenceDetectorCurrentSPL.get() > threshold) {
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
                            FormantResults results = new FormantResults(formantResults[0], formantResults[1], isWithinStandardDeviation);
                            formantUtils.addLastResults(results);
                        }
                    };
                    Thread captureThread = new Thread(runner);
                    captureThread.start();
                } catch (LineUnavailableException e1) {
                    e1.printStackTrace();
                }
            } else {
                accountUtils.saveToLog(currentAccount.getUserName(), Collections.singletonList("Finished recording."));
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
            if (!recordingInfo.getText().contains("Click the record button to start recording your pronunciation.")) {
                recordingInfo.setText(" \n ");
            }
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
        formantChart = setUpFormantChart();
        formantChart.setUserGender(currentAccount.getGender());
        formantChart.getData().add(userResults);
        setUpNativePronunciationRangesOnChart();
        formantChart.setPrefWidth(800);
        Tooltip chartToolTip = new Tooltip("This graph represents your pronunciation.\n" +
                "After recording, your result will appear here as a red dot.\n" +
                "The axes describe your tongue position for your pronunciation result.\n" +
                "The green bubble represents the target area for your pronunciation.\n" +
                "The animation is a guide for how your mouth and tongue should be positioned.\n" +
                "Try different positions as you pronounce the vowel.");
        chartToolTip.setFont(Font.font(14));

        //https://coderanch.com/t/622070/java/control-Tooltip-visible-time-duration
        formantChart.setOnMouseEntered(event -> {
            Point2D p = formantChart.localToScreen(formantChart.getLayoutBounds().getMaxX() - 200,
                    formantChart.getLayoutBounds().getMinY() - 150);
            chartToolTip.show(formantChart, p.getX(), p.getY());
        });
        formantChart.setOnMouseExited(event -> chartToolTip.hide());

        quitButton.setOnAction(e -> {
            accountUtils.saveToLog(currentAccount.getUserName(), Collections.singletonList("Exited " + vowel
                    + " pronunciation."));
            new ExerciseSelection().initializeAndStart(primaryStage, currentAccount);
        });
        HBox quitButtonHbox = new HBox();
        quitButtonHbox.setAlignment(Pos.CENTER_RIGHT);
        quitButtonHbox.getChildren().add(quitButton);

        HBox resultsInfoHbox = new HBox();
        resultsInfoHbox.setAlignment(Pos.CENTER);
        resultsInfoHbox.getChildren().add(resultsInfo);
        grid.add(resultsInfoHbox, 2, 4);

        HBox resultsAndQuitHbox = new HBox(15);
        resultsAndQuitHbox.setAlignment(Pos.CENTER_RIGHT);
        resultsAndQuitHbox.getChildren().addAll(resultsInfoHbox, quitButtonHbox);

        VBox chartAndQuitBtnVbox = new VBox();
        chartAndQuitBtnVbox.getChildren().addAll(formantChart, resultsAndQuitHbox);
        grid.add(chartAndQuitBtnVbox, 1, 3, 2, 4);


        Scene scene = new Scene(grid);
        primaryStage.setTitle("EstonianVowelCAPT - Pronunciation: " + vowel);
        primaryStage.setScene(scene);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(700);
        primaryStage.show();
    }

    private void updateChart(FormantResults results) {
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
                resultsInfoMessage = "Your last pronunciation was outside the range of a native speaker. Keep trying.\n" +
                        "Position your tongue so that it's closer to the target bubble on the low-high, front-back dimensions.";
            }
        }
        resultsInfo.setText(resultsInfoMessage);
        accountUtils.saveToLog(currentAccount.getUserName(), Collections.singletonList(resultsInfoMessage));

        userResults.getData().add(new XYChart.Data<>(
                secondFormant,
                firstFormant,
                20));

        setUserResultsColor();
    }

    private void setUserResultsColor() {
        XYChart.Data<Number, Number> data = userResults.getData().get(attemptCounter);
        Label label = new Label();
        if (attemptCounter < attemptsSymbols.length) {
            label.setText(attemptsSymbols[attemptCounter]);
        } else {
            label.setText("...");
        }
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 12; -fx-font-weight: bold; -fx-wrap-text: true;");
        Node bubble = data.getNode();
        bubble.setStyle("-fx-bubble-fill:  red;"
                + "-fx-background-color: radial-gradient(center 50% 50%, radius 80%, "
                + "derive(-fx-bubble-fill,20%), derive(-fx-bubble-fill,-30%));");
        StackPane region = (StackPane) bubble;
        region.getChildren().add(label);

        attemptCounter++;
    }

    private EllipseBubbleChart<Number, Number> setUpFormantChart() {
        int xAxisLowerBound = 500;
        int xAxisUpperBound = 3000;
        int yAxisLowerBound = 100;
        int yAxisUpperBound = 900;

        if (currentAccount.getGender().equals("male")) {
            xAxisUpperBound = 2500;
        }

        NumberAxis xAxis = new NumberAxis(xAxisUpperBound, xAxisLowerBound, 500);
        xAxis.setLabel("Tongue front - back");

        NumberAxis yAxis = new NumberAxis(yAxisUpperBound, yAxisLowerBound, 100);
        yAxis.setLabel("Tongue low - high");

        return new EllipseBubbleChart<>(xAxis, yAxis);
    }

    private void setUpNativePronunciationRangesOnChart() {
        List<VowelInfo> vowels = formantUtils.getVowels(currentAccount.getGender());

        //For removing axis values from chart
        formantChart.getXAxis().setTickLabelsVisible(false);
        formantChart.getXAxis().setTickMarkVisible(false);
        formantChart.getXAxis().lookup(".axis-minor-tick-mark").setVisible(false);

        formantChart.getYAxis().setTickLabelsVisible(false);
        formantChart.getYAxis().setTickMarkVisible(false);
        formantChart.getYAxis().lookup(".axis-minor-tick-mark").setVisible(false);

        for (VowelInfo vowel : vowels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            if (vowel.getVowel() == this.vowel) {
                series.setName("Native pronunciation of /" + vowel.getVowel() + "/");
            } else {
                series.setName("");
            }

            double extraValue = vowel.getFirstFormantSd();
            extraValue += extraValue / 5;

            series.getData().add(new XYChart.Data<>(
                    vowel.getSecondFormantMean(),
                    vowel.getFirstFormantMean(),
                    extraValue));

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
            } catch (ClassCastException ex) {
                ex.printStackTrace();
            }
        }

        //https://stackoverflow.com/a/38953486
        for (int i = 1; i < 10; i++) {
            Set<Node> nodes = formantChart.lookupAll(".series" + i);
            for (Node n : nodes) {
                StackPane bubble = (StackPane) n;
                Label bubbleLabel = (Label) bubble.getChildren().get(0);

                if (bubbleLabel.getText().equals(String.valueOf(vowel))) {
                    n.setStyle("-fx-bubble-fill:  #7fff00aa; "
                            + "-fx-background-color: radial-gradient(center 50% 50%, radius 80%, "
                            + "derive(-fx-bubble-fill,20%), derive(-fx-bubble-fill,-30%));");
                } else {
                    n.setStyle("-fx-bubble-fill:  #ffff00aa; "
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

    @Override
    public boolean process(AudioEvent audioEvent) {
        SilenceDetectorCurrentSPL.set(silenceDetector.currentSPL());
        return false;
    }

    @Override
    public void processingFinished() {

    }
}
