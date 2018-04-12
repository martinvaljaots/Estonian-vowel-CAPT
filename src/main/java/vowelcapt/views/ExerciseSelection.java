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

        Button firstExerciseButton = new Button("Pronunciation exercise: a");
        firstExerciseButton.setOnAction(e -> {
            new PronunciationExercise().initializeAndStart(primaryStage, currentAccount, "maam", 'a');
        });

        grid.add(firstExerciseButton, 0, 4);

        Label listeningExercisesLabel = new Label("Listening exercises:");
        listeningExercisesLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        grid.add(listeningExercisesLabel, 0, 5);

        Label settingsLabel = new Label("Settings:");
        settingsLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        grid.add(settingsLabel, 0, 6);

        Button thresholdReadjustButton = new Button("Adjust microphone level");
        thresholdReadjustButton.setOnAction(e -> {
            new ThresholdSetter().initializeAndStart(primaryStage, currentAccount, false);
        });

        grid.add(thresholdReadjustButton, 0, 7);

        Scene scene = new Scene(grid, 300, 300);
        primaryStage.setTitle("EstonianVowelCAPT - Exercise selection");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public void initializeAndStart(Stage primaryStage, Account account) {
        currentAccount = account;
        System.out.println("Exercise selection screen: " + currentAccount.toString());
        start(primaryStage);
    }
}
