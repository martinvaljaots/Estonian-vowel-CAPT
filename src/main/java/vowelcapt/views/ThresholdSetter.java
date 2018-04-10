package vowelcapt.views;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import vowelcapt.utils.AccountUtils;
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label instructionLabel = new Label();
        instructionLabel.setText("Listen to the pronunciation of \"maam\" and try to replicate it.\n" +
                "Move the slider to change your microphone volume so that only \nthe vowel part \"aa\" lights up as green.\n" +
                "This is necessary for accurately measuring your pronunciations.");

        grid.add(instructionLabel, 0, 0);

        Button listenButton = new Button();
        listenButton.setText("Listen to \"maam\"");
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

        thresholdSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                graphPanel.setThresholdLevel(newValue.doubleValue());
                threshold = newValue.doubleValue();
                System.out.println("Threshold set to: " + newValue.doubleValue());
            }
        });

        grid.add(thresholdSlider, 0, 3);

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Microphone volume level confirmation");
        confirmationAlert.setHeaderText("The current microphone volume level will be overwritten");
        confirmationAlert.setContentText("Are you sure? \nThis level can be changed in the exercise selection screen.");

        Button saveButton = new Button();
        saveButton.setText("Save");
        saveButton.setOnAction(e -> {
            Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();
            confirmationResult.ifPresent(a -> {
                if (confirmationResult.get() == ButtonType.OK) {
                    System.out.println("Saving threshold level: " + threshold);
                    accountUtils.saveThreshold("maie", threshold);
                }
            });
        });

        Button cancelButton = new Button();
        cancelButton.setText("Cancel");
        cancelButton.setOnAction(e -> {
            // TODO: hitting the cancel button should move user to exercise picking screen
            System.out.println("Need to add routing to exercise picking screen");
        });

        HBox hBox = new HBox(25);
        hBox.getChildren().addAll(saveButton, cancelButton);
        grid.add(hBox, 0, 4);
        startAudioDispatching();

        Scene scene = new Scene(grid);
        primaryStage.setTitle("EstonianVowelCAPT");
        primaryStage.setScene(scene);
        primaryStage.setWidth(550);
        primaryStage.setHeight(600);
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    private void startAudioDispatching() {
        try {
            final AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(
                    TargetDataLine.class, format);
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

            dispatcher.addAudioProcessor(silenceDetector);
            silenceDetector = new SilenceDetector(threshold, false);
            dispatcher.addAudioProcessor(silenceDetector);
            dispatcher.addAudioProcessor(this);

            new Thread(dispatcher, "Audio dispatching").start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
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

    private AudioFormat getAudioFormat() {
        float sampleRate = 22050;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate,
                sampleSizeInBits, channels, signed, bigEndian);
    }
}
