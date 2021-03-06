package vowelcapt.views;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import vowelcapt.utils.account.Account;
import vowelcapt.utils.account.AccountUtils;

import java.io.File;
import java.util.*;

public class ListeningExercise extends Application {

    private List<String> audioFileNames;
    private String audioFilesFolder;
    private int questionCounter = 1;
    private int scoreCounter = 0;
    private MediaPlayer mediaPlayer;
    private final Label questionLabel = new Label();
    private final Button firstDegreeButton = new Button("short");
    private final Button secondDegreeButton = new Button("long");
    private final Button thirdDegreeButton = new Button("overlong");
    private final Button questionListenButton = new Button();
    private String currentQuestionFileName;
    private Account currentAccount;
    private Stage stage;
    private AccountUtils accountUtils = new AccountUtils();

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label introductoryTextLabel = new Label("Vowels in Estonian have 3 quantity degrees: short, long and overlong.\n" +
                "Listen to the example below and attempt the exercise.");
        grid.add(introductoryTextLabel, 0, 0);

        Button exampleListeningButton = new Button("Listen: short - long - overlong");
        exampleListeningButton.setOnAction(e -> playSoundFile("short_long_overlong"));
        grid.add(exampleListeningButton, 0, 1);

        Label exerciseLabel = new Label("Listening:\nshort, long, overlong");
        exerciseLabel.setFont(Font.font("Arial", 30));
        grid.add(exerciseLabel, 0, 3);

        questionLabel.setText("Choose the quantity degree of the first vowel in the word.\n" +
                "Question " + questionCounter + "/15:");
        grid.add(questionLabel, 0, 4);

        questionListenButton.setText("Listen to question " + questionCounter + "/15");
        questionListenButton.setOnAction(e -> playSoundFile(audioFilesFolder + "/" + currentQuestionFileName));
        grid.add(questionListenButton, 0, 5);

        Label answerLabel = new Label("Which quantity degree did you hear?");
        grid.add(answerLabel, 0, 6);

        firstDegreeButton.setFont(Font.font(20));
        firstDegreeButton.setOnAction(e -> checkAnswer(currentQuestionFileName, 1));

        secondDegreeButton.setFont(Font.font(20));
        secondDegreeButton.setOnAction(e -> checkAnswer(currentQuestionFileName, 2));

        thirdDegreeButton.setFont(Font.font(20));
        thirdDegreeButton.setOnAction(e -> checkAnswer(currentQuestionFileName, 3));

        HBox answerHbox = new HBox(20);
        answerHbox.setAlignment(Pos.BOTTOM_LEFT);
        answerHbox.getChildren().addAll(firstDegreeButton, secondDegreeButton, thirdDegreeButton);
        grid.add(answerHbox, 0, 7);

        Button quitButton = new Button("Quit");
        quitButton.setOnAction(e -> {
            Alert quitAlert = new Alert(Alert.AlertType.CONFIRMATION);
            quitAlert.setTitle("Quit Exercise");
            quitAlert.setHeaderText("Quit to exercise selection?");
            quitAlert.setContentText(null);
            ButtonType yesButtonType = new ButtonType("Yes");
            ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            quitAlert.getButtonTypes().setAll(yesButtonType, cancelButtonType);

            Optional<ButtonType> result = quitAlert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == yesButtonType) {
                    new ExerciseSelection().initializeAndStart(primaryStage, currentAccount);
                }
            }
        });
        HBox quitButtonHbox = new HBox(10);
        quitButtonHbox.setAlignment(Pos.BOTTOM_LEFT);
        quitButtonHbox.getChildren().add(quitButton);
        grid.add(quitButtonHbox, 1, 10);

        Scene scene = new Scene(grid, 500, 500);
        primaryStage.setTitle("EstonianVowelCAPT - Listening: short, long, overlong");
        primaryStage.setScene(scene);
        primaryStage.setWidth(500);
        primaryStage.setHeight(525);
        primaryStage.show();
    }

    private void checkAnswer(String questionFileName, int answer) {
        String[] questionFileNameSplit = questionFileName.split("_");

        int correctAnswer = Integer.parseInt(questionFileNameSplit[1]);

        boolean isAnswerCorrect = answer == correctAnswer;

        String[] quantityDegrees = {"short", "long", "overlong"};

        String message = "Incorrect!\nCorrect answer: " + quantityDegrees[correctAnswer - 1];
        if (isAnswerCorrect) {
            message = "Correct!";
            scoreCounter++;
        }

        accountUtils.saveToLog(currentAccount.getUserName(), Collections.singletonList(audioFilesFolder
                + " question " + questionCounter + "/15: " + message + " User answer: " + quantityDegrees[answer - 1]));

        Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
        resultAlert.setTitle("Question " + questionCounter + "/15");
        resultAlert.setHeaderText(null);
        resultAlert.setContentText(message);
        resultAlert.showAndWait();

        if (audioFileNames.size() > 0) {
            currentQuestionFileName = audioFileNames.get(0);
            audioFileNames.remove(0);
            questionCounter++;
            questionListenButton.setText("Listen to question " + questionCounter + "/15");
            questionLabel.setText("Choose the quantity degree of the first vowel.\n" +
                    "Question " + questionCounter + "/15:");
            playSoundFile(audioFilesFolder + "/" + currentQuestionFileName);
        } else {
            accountUtils.saveToLog(currentAccount.getUserName(), Collections.singletonList("Finished, correct answers: "
                    + scoreCounter + "/15"));
            Alert finishedAlert = new Alert(Alert.AlertType.INFORMATION);
            finishedAlert.setTitle("Exercise finished!");
            finishedAlert.setHeaderText(null);
            finishedAlert.setContentText("Correct answers: " + scoreCounter + "/15");

            finishedAlert.showAndWait();
            new ExerciseSelection().initializeAndStart(stage, currentAccount);
        }
    }

    private void playSoundFile(String soundFilePath) {
        String pronunciationFileLocation = "resources/sample_sounds/listening/" + soundFilePath + ".wav";
        Media pronunciationFile = new Media(new File(pronunciationFileLocation).toURI().toString());
        mediaPlayer = new MediaPlayer(pronunciationFile);
        mediaPlayer.play();
    }

    public void initializeAndStart(Stage primaryStage, String audioFilesFolder,
                                   List<String> audioFileNames, Account account) {
        accountUtils.saveToLog(account.getUserName(), Collections.singletonList("Moved to " + audioFilesFolder
                + " listening exercise."));
        this.stage = primaryStage;
        this.audioFilesFolder = audioFilesFolder;
        this.audioFileNames = audioFileNames;
        Collections.shuffle(this.audioFileNames);
        currentAccount = account;
        currentQuestionFileName = audioFileNames.get(0);
        audioFileNames.remove(0);
        start(primaryStage);
    }
}
