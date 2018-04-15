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
import vowelcapt.utils.Account;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class ListeningExercise extends Application {

    private List<String> audioFileNames;
    private String audioFilesFolder;
    private int questionCounter = 1;
    private int scoreCounter = 0;
    private MediaPlayer mediaPlayer;
    private final Label questionLabel = new Label();
    private final Button firstAnswerButton = new Button("A");
    private final Button secondAnswerButton = new Button("B");
    private final Button questionListenButton = new Button();
    private String mainQuantityDegree;
    private String currentQuestionFileName;
    private String article = "a";
    private Account currentAccount;
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        String[] quantityDegrees = audioFilesFolder.split("_");

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

        Label exerciseLabel = new Label("Listening: " + quantityDegrees[0] + " vs. " + quantityDegrees[1]);
        exerciseLabel.setFont(Font.font("Arial", 30));
        grid.add(exerciseLabel, 0, 3);

        if (mainQuantityDegree.equals("overlong")) {
            article = "an";
        }
        questionLabel.setText("Which of the two words has " + article + " " + mainQuantityDegree + " vowel?\n" +
                "Question " + questionCounter + "/5:");
        grid.add(questionLabel, 0, 4);

        questionListenButton.setText("Listen to question " + questionCounter + "/5");
        questionListenButton.setOnAction(e -> playSoundFile(audioFilesFolder + "/" + currentQuestionFileName));
        grid.add(questionListenButton, 0, 5);

        Label answerLabel = new Label("Your answer:");
        grid.add(answerLabel, 0, 6);

        firstAnswerButton.setFont(Font.font(20));
        firstAnswerButton.setOnAction(e -> checkAnswer(currentQuestionFileName, 1));

        secondAnswerButton.setFont(Font.font(20));
        secondAnswerButton.setOnAction(e -> checkAnswer(currentQuestionFileName, 2));

        HBox answerHbox = new HBox(20);
        answerHbox.setAlignment(Pos.BOTTOM_LEFT);
        answerHbox.getChildren().addAll(firstAnswerButton, secondAnswerButton);
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
        primaryStage.setTitle("EstonianVowelCAPT - Quantity degrees: " +
                quantityDegrees[0] + " vs. " + quantityDegrees[1]);
        primaryStage.setScene(scene);
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);
        primaryStage.show();
    }

    private void checkAnswer(String questionFileName, int answer) {
        String[] questionFileNameSplit = questionFileName.split("_");
        int correctAnswer = Integer.parseInt(questionFileNameSplit[1]);

        boolean answerCorrectness = (answer == correctAnswer);

        String message = "Incorrect!";
        if (answerCorrectness) {
            message = "Correct!";
            scoreCounter++;
        }

        Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
        resultAlert.setTitle("Question " + questionCounter + "/5");
        resultAlert.setHeaderText(null);
        resultAlert.setContentText(message);
        resultAlert.showAndWait();

        if (audioFileNames.size() > 0) {
            currentQuestionFileName = audioFileNames.get(0);
            audioFileNames.remove(0);
            questionCounter++;
            questionListenButton.setText("Listen to question " + questionCounter + "/5");
            questionLabel.setText("Which of the two words has " + article + " " + mainQuantityDegree + " vowel?\n" +
                    "Question " + questionCounter + "/5:");
            playSoundFile(audioFilesFolder + "/" + currentQuestionFileName);
        } else {
            Alert finishedAlert = new Alert(Alert.AlertType.INFORMATION);
            finishedAlert.setTitle("Exercise finished!");
            finishedAlert.setHeaderText(null);
            finishedAlert.setContentText("Correct answers: " + scoreCounter + "/5");

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
                                   List<String> audioFileNames, String mainQuantityDegree, Account account) {
        this.stage = primaryStage;
        this.audioFilesFolder = audioFilesFolder;
        this.audioFileNames = audioFileNames;
        this.mainQuantityDegree = mainQuantityDegree;
        currentAccount = account;
        currentQuestionFileName = audioFileNames.get(0);
        audioFileNames.remove(0);
        start(primaryStage);
    }
}
