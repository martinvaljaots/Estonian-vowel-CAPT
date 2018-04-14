package vowelcapt.views;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import vowelcapt.utils.Account;

import java.util.ArrayList;
import java.util.List;

public class ExerciseSelection extends Application {

    private Account currentAccount;

    @Override
    public void start(Stage primaryStage) {

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Welcome");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label currentUserLabel = new Label("Currently logged in as " + currentAccount.getUserName());
        grid.add(currentUserLabel, 0, 1);

        Label pronunciationExerciseLabel = new Label("Pronunciation exercises:");
        pronunciationExerciseLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        grid.add(pronunciationExerciseLabel, 0, 3);

        Button pronunciationExerciseButton1 = new Button("Pronunciation exercise: a");
        pronunciationExerciseButton1.setOnAction(e -> {
            new PronunciationExercise().initializeAndStart(primaryStage, currentAccount, "maam", 'a');
        });

        grid.add(pronunciationExerciseButton1, 0, 4);

        Label listeningExercisesLabel = new Label("Listening exercises:");
        listeningExercisesLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        grid.add(listeningExercisesLabel, 1, 3);

        Button listeningExerciseButton1 = new Button("Quantity degrees: short vs. long");
        listeningExerciseButton1.setOnAction(e -> {
            List<String> filenames = new ArrayList<>();
            filenames.add("1_2");
            filenames.add("2_1");
            filenames.add("3_2");
            filenames.add("4_2");
            filenames.add("5_2");
            new ListeningExercise().initializeAndStart(primaryStage, "short_long", filenames,
                    "long", currentAccount);
        });
        grid.add(listeningExerciseButton1, 1, 4);

        Button listeningExerciseButton2 = new Button("Quantity degrees: short vs. overlong");
        listeningExerciseButton2.setOnAction(e -> {
            List<String> filenames = new ArrayList<>();
            filenames.add("1_2");
            filenames.add("2_2");
            filenames.add("3_1");
            filenames.add("4_2");
            filenames.add("5_2");
            new ListeningExercise().initializeAndStart(primaryStage, "short_overlong", filenames,
                    "short", currentAccount);
        });
        grid.add(listeningExerciseButton2, 1, 5);

        Button listeningExerciseButton3 = new Button("Quantity degrees: long vs. overlong");
        listeningExerciseButton3.setOnAction(e -> {
            List<String> filenames = new ArrayList<>();
            filenames.add("1_2");
            filenames.add("2_2");
            filenames.add("3_1");
            filenames.add("4_2");
            filenames.add("5_1");
            new ListeningExercise().initializeAndStart(primaryStage, "long_overlong", filenames,
                    "overlong", currentAccount);
        });
        grid.add(listeningExerciseButton3, 1, 6);

        Label settingsLabel = new Label("Settings:");
        settingsLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        grid.add(settingsLabel, 2, 3);

        Button thresholdReadjustButton = new Button("Adjust microphone level");
        thresholdReadjustButton.setOnAction(e -> {
            new ThresholdSetter().initializeAndStart(primaryStage, currentAccount, false);
        });

        grid.add(thresholdReadjustButton, 2, 4);

        Scene scene = new Scene(grid, 500, 500);
        primaryStage.setTitle("EstonianVowelCAPT - Exercise selection");
        primaryStage.setScene(scene);
        primaryStage.setHeight(500);
        primaryStage.setWidth(650);
        primaryStage.show();
    }


    public void initializeAndStart(Stage primaryStage, Account account) {
        currentAccount = account;
        System.out.println("Exercise selection screen: " + currentAccount.toString());
        start(primaryStage);
    }
}
