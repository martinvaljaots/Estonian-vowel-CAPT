package vowelcapt.views;

import be.tarsos.dsp.resample.SoundTouchRateTransposer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import vowelcapt.helpers.IsRecording;

import javax.sound.sampled.*;
import java.io.*;

public class SampleLesson extends Application {

    private final Button recordButton = new Button("Record");
    private ByteArrayOutputStream out;
    private TextArea formantInfo = new TextArea();
    AudioInputStream ais;

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

        HBox hbRecordButton = new HBox(15);
        hbRecordButton.setAlignment(Pos.BOTTOM_RIGHT);
        hbRecordButton.getChildren().add(recordButton);
        grid.add(hbRecordButton, 0, 1);


        recordButton.setOnAction(e -> {
            if (!IsRecording.get()) {
                IsRecording.set(true);
                recordButton.setText("Recording...");
                final AudioFormat format = getAudioFormat();
                DataLine.Info info = new DataLine.Info(
                        TargetDataLine.class, format);

                try {
                    final TargetDataLine line;
                    line = (TargetDataLine)
                            AudioSystem.getLine(info);
                    line.open(format);
                    line.start();

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
                formantInfo.setText("NUTT");
                recordButton.setText("Record");
            }
        });


        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setTitle("EstonianVowelCAPT");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate,
                sampleSizeInBits, channels, signed, bigEndian);
    }
}
