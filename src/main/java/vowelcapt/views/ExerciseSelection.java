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
import vowelcapt.utils.account.Account;
import vowelcapt.utils.account.AccountUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExerciseSelection extends Application {

    private Account currentAccount;
    private AccountUtils accountUtils = new AccountUtils();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setX(600);
        primaryStage.setY(100);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(25);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Welcome");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 24));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label currentUserLabel = new Label("Currently logged in as " + currentAccount.getUserName());
        currentUserLabel.setFont(Font.font(14));
        grid.add(currentUserLabel, 0, 1);

        Label pronunciationExerciseLabel = new Label("Pronunciation exercises:");
        pronunciationExerciseLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        grid.add(pronunciationExerciseLabel, 0, 3);

        Button pronunciationExerciseButton1 = new Button("Pronunciation exercise: a");
        pronunciationExerciseButton1.setOnAction(e -> new PronunciationExercise().initializeAndStart(primaryStage, currentAccount, "taat",
                "old man, father", 'a'));
        grid.add(pronunciationExerciseButton1, 0, 4);

        Button pronunciationExerciseButton2 = new Button("Pronunciation exercise: e");
        pronunciationExerciseButton2.setOnAction(e -> new PronunciationExercise().initializeAndStart(primaryStage, currentAccount, "sees",
                "inside", 'e'));
        grid.add(pronunciationExerciseButton2, 0, 5);

        Button pronunciationExerciseButton3 = new Button("Pronunciation exercise: i");
        pronunciationExerciseButton3.setOnAction(e -> new PronunciationExercise().initializeAndStart(primaryStage, currentAccount, "kiip",
                "chip (electronics)", 'i'));
        grid.add(pronunciationExerciseButton3, 0, 6);

        Button pronunciationExerciseButton4 = new Button("Pronunciation exercise: o");
        pronunciationExerciseButton4.setOnAction(e -> new PronunciationExercise().initializeAndStart(primaryStage, currentAccount, "kook",
                "cake", 'o'));
        grid.add(pronunciationExerciseButton4, 0, 7);

        Button pronunciationExerciseButton5 = new Button("Pronunciation exercise: u");
        pronunciationExerciseButton5.setOnAction(e -> new PronunciationExercise().initializeAndStart(primaryStage, currentAccount, "puuk",
                "tick", 'u'));
        grid.add(pronunciationExerciseButton5, 0, 8);

        Button pronunciationExerciseButton6 = new Button("Pronunciation exercise: õ");
        pronunciationExerciseButton6.setOnAction(e -> new PronunciationExercise().initializeAndStart(primaryStage, currentAccount, "võõp",
                "paint, varnish", 'õ'));
        grid.add(pronunciationExerciseButton6, 0, 9);

        Button pronunciationExerciseButton7 = new Button("Pronunciation exercise: ä");
        pronunciationExerciseButton7.setOnAction(e -> new PronunciationExercise().initializeAndStart(primaryStage, currentAccount, "tääk",
                "bayonet", 'ä'));
        grid.add(pronunciationExerciseButton7, 0, 10);

        Button pronunciationExerciseButton8 = new Button("Pronunciation exercise: ö");
        pronunciationExerciseButton8.setOnAction(e -> new PronunciationExercise().initializeAndStart(primaryStage, currentAccount, "söök",
                "food", 'ö'));
        grid.add(pronunciationExerciseButton8, 0, 11);

        Button pronunciationExerciseButton9 = new Button("Pronunciation exercise: ü");
        pronunciationExerciseButton9.setOnAction(e -> new PronunciationExercise().initializeAndStart(primaryStage, currentAccount, "tüüp",
                "type, form", 'ü'));
        grid.add(pronunciationExerciseButton9, 0, 12);

        Label listeningExercisesLabel = new Label("Listening exercises:");
        listeningExercisesLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        grid.add(listeningExercisesLabel, 1, 3, 2, 1);

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
        grid.add(listeningExerciseButton1, 1, 4, 2, 1);

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
        grid.add(listeningExerciseButton2, 1, 5, 2, 1);

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
        grid.add(listeningExerciseButton3, 1, 6, 2, 1);

        Label settingsLabel = new Label("Settings:");
        settingsLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        grid.add(settingsLabel, 3, 3);

        Button thresholdReadjustButton = new Button("Adjust microphone level");
        thresholdReadjustButton.setOnAction(e -> new ThresholdSetter().initializeAndStart(primaryStage, currentAccount, false));

        grid.add(thresholdReadjustButton, 3, 4);

        Scene scene = new Scene(grid, 500, 500);
        primaryStage.setTitle("EstonianVowelCAPT - Exercise selection");
        primaryStage.setScene(scene);
        primaryStage.setHeight(600);
        primaryStage.setWidth(650);
        primaryStage.show();
    }


    public void initializeAndStart(Stage primaryStage, Account account) {
        currentAccount = account;
        accountUtils.saveToLog(account.getUserName(), Collections.singletonList("Moved to exercise selection screen: "
                + currentAccount.toString()));
        start(primaryStage);
    }
}
