package vowelcapt.views;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import vowelcapt.utils.Account;
import vowelcapt.utils.AccountUtils;
import vowelcapt.utils.AudioUtils;
import vowelcapt.utils.GraphPanel;

import javax.sound.sampled.*;
import java.io.File;
import java.util.Optional;

/**
 * Based on a SoundDetector example in TarsosDSP.
 * https://github.com/JorenSix/TarsosDSP
 */

public class ThresholdSetter extends Application implements AudioProcessor {

    private GraphPanel graphPanel = new GraphPanel(-80);
    private SilenceDetector silenceDetector = new SilenceDetector();
    private double threshold = -80;
    private AccountUtils accountUtils = new AccountUtils();
    //TODO: remove this test account before user testing
    private Account currentAccount = new Account("test", "test", "male");
    private boolean isFirstRegistration = false;

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label instructionLabel = new Label("Listen to the pronunciation of \"maam\" and try to replicate it.\n" +
                "Move the slider to change your microphone volume so that only \nthe vowel part \"aa\" lights up as green.\n" +
                "This is necessary for accurately measuring your pronunciations.\n" +
                "The red bar shows the current highest recorded volume.");

        grid.add(instructionLabel, 0, 0);

        Button listenButton = new Button("Listen to \"maam\"");
        listenButton.setOnAction(e -> {
            String bip = "resources/sample_sounds/maam.wav";
            Media hit = new Media(new File(bip).toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(hit);
            mediaPlayer.play();
        });

        grid.add(listenButton, 0, 1);

        final SwingNode swingNode = new SwingNode();
        swingNode.setContent(graphPanel);
        Pane pane = new Pane();
        pane.getChildren().add(swingNode);
        grid.add(pane, 0, 2);

        Slider thresholdSlider = new Slider();
        thresholdSlider.setMin(-120);
        thresholdSlider.setMax(0);
        thresholdSlider.setValue(-80);
        thresholdSlider.setShowTickLabels(true);
        thresholdSlider.setShowTickMarks(true);
        thresholdSlider.setMajorTickUnit(20);
        thresholdSlider.setMinorTickCount(5);
        thresholdSlider.setBlockIncrement(10);

        thresholdSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            graphPanel.setThresholdLevel(newValue.doubleValue());
            threshold = newValue.doubleValue();
            System.out.println("Threshold set to: " + newValue.doubleValue());
        });

        grid.add(thresholdSlider, 0, 3);

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Microphone volume level confirmation");
        confirmationAlert.setHeaderText("Saving current microphone volume level");
        confirmationAlert.setContentText("Are you sure? \nThis level can also be changed in the exercise selection screen.");
        final TargetDataLine line;
        final AudioFormat format = AudioUtils.getAudioFormat();
        DataLine.Info info = new DataLine.Info(
                TargetDataLine.class, format);
        try {



            line = (TargetDataLine)
                    AudioSystem.getLine(info);

            line.open(format);
            line.start();

            final AudioInputStream stream = new AudioInputStream(line);

            JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
            AudioDispatcher dispatcher = new AudioDispatcher(audioStream, 1024,
                    0);

            dispatcher.addAudioProcessor(silenceDetector);
            silenceDetector = new SilenceDetector(threshold, false);
            dispatcher.addAudioProcessor(silenceDetector);
            dispatcher.addAudioProcessor(this);

            new Thread(dispatcher, "Audio dispatching").start();

            Button saveButton = new Button();
            saveButton.setText("Save");
            saveButton.setOnAction(e -> {
                Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();
                confirmationResult.ifPresent(a -> {
                    if (confirmationResult.get() == ButtonType.OK) {
                        System.out.println("Saving threshold level: " + threshold);
                        accountUtils.saveThreshold(currentAccount.getUserName(), threshold);
                        currentAccount.setThreshold(threshold);
                        line.stop();
                        line.close();
                        new ExerciseSelection().initializeAndStart(primaryStage, currentAccount);
                    }
                });
            });

            Button cancelButton = new Button();
            cancelButton.setText("Cancel");
            cancelButton.setOnAction(e -> {
                line.stop();
                line.close();
                primaryStage.setWidth(300);
                primaryStage.setHeight(300);
                new ExerciseSelection().initializeAndStart(primaryStage, currentAccount);
            });

            HBox hBox = new HBox(25);
            if (isFirstRegistration) {
                hBox.getChildren().add(saveButton);
            } else {
                hBox.getChildren().addAll(saveButton, cancelButton);
            }
            grid.add(hBox, 0, 4);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(grid);
        primaryStage.setTitle("EstonianVowelCAPT");
        primaryStage.setScene(scene);
        primaryStage.setWidth(500);
        primaryStage.setHeight(625);
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
    public boolean process(AudioEvent audioEvent) {
        graphPanel.addDataPoint(silenceDetector.currentSPL(), System.currentTimeMillis());
        return false;
    }

    @Override
    public void processingFinished() {

    }

    public void initializeAndStart(Stage primaryStage, Account account, boolean isFirstRegistration) {
        currentAccount = account;
        this.isFirstRegistration = isFirstRegistration;
        start(primaryStage);
    }
}
