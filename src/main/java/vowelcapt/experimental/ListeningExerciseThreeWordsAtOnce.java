package vowelcapt.experimental;

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
import vowelcapt.views.ExerciseSelection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This class is what the listening exercise was before refactoring according to feedback. (Second idea)
 */

public class ListeningExerciseThreeWordsAtOnce extends Application {

    private List<String> audioFileNames;
    private String audioFilesFolder;
    private int questionCounter = 1;
    private int scoreCounter = 0;
    private MediaPlayer mediaPlayer;
    private final Label questionLabel = new Label();
    private final CheckBox firstAnswerButton = new CheckBox();
    private final CheckBox secondAnswerButton = new CheckBox();
    private final CheckBox thirdAnswerButton = new CheckBox();
    private final Button questionListenButton = new Button();
    private List<Integer> answerSequence = new ArrayList<>();
    private String currentQuestionFileName;
    private Account currentAccount;
    private Stage stage;
    private AccountUtils accountUtils = new AccountUtils();

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

        Label exerciseLabel = new Label("Listening:\nshort, long, overlong");
        exerciseLabel.setFont(Font.font("Arial", 30));
        grid.add(exerciseLabel, 0, 3);

        questionLabel.setText("Order the words in increasing degree of quantity.\n" +
                "Question " + questionCounter + "/5:");
        grid.add(questionLabel, 0, 4);

        questionListenButton.setText("Listen to question " + questionCounter + "/5");
        questionListenButton.setOnAction(e -> playSoundFile(audioFilesFolder + "/" + currentQuestionFileName));
        grid.add(questionListenButton, 0, 5);

        Label answerLabel = new Label("Your answer - click the boxes in order of increasing quantity degree:");
        grid.add(answerLabel, 0, 6);

        firstAnswerButton.setFont(Font.font(20));
        firstAnswerButton.setOnAction(e -> {
            if (firstAnswerButton.isSelected()) {
                answerSequence.add(1);
                firstAnswerButton.setText(String.valueOf(answerSequence.size()));
                if (answerSequence.size() == 3) {
                    checkAnswer(currentQuestionFileName, answerSequence);
                }
            } else {
                answerSequence.remove((Integer) 1);
                firstAnswerButton.setText("");
                secondAnswerButton.setText(answerSequence.indexOf(2) != -1 ?
                        String.valueOf(answerSequence.indexOf(2) + 1) : "");
                thirdAnswerButton.setText(answerSequence.indexOf(3) != -1 ?
                        String.valueOf(answerSequence.indexOf(3) + 1) : "");
            }
        });

        secondAnswerButton.setFont(Font.font(20));
        secondAnswerButton.setOnAction(e -> {
            if (secondAnswerButton.isSelected()) {
                answerSequence.add(2);
                secondAnswerButton.setText(String.valueOf(answerSequence.size()));
                if (answerSequence.size() == 3) {
                    checkAnswer(currentQuestionFileName, answerSequence);
                }
            } else {
                answerSequence.remove((Integer) 2);
                secondAnswerButton.setText("");
                firstAnswerButton.setText(answerSequence.indexOf(1) != -1 ?
                        String.valueOf(answerSequence.indexOf(1) + 1) : "");
                thirdAnswerButton.setText(answerSequence.indexOf(3) != -1 ?
                        String.valueOf(answerSequence.indexOf(3) + 1) : "");
            }
        });

        thirdAnswerButton.setFont(Font.font(20));
        thirdAnswerButton.setOnAction(e -> {
            if (thirdAnswerButton.isSelected()) {
                answerSequence.add(3);
                thirdAnswerButton.setText(String.valueOf(answerSequence.size()));
                if (answerSequence.size() == 3) {
                    checkAnswer(currentQuestionFileName, answerSequence);
                }
            } else {
                answerSequence.remove((Integer) 3);
                thirdAnswerButton.setText("");
                firstAnswerButton.setText(answerSequence.indexOf(1) != -1 ?
                        String.valueOf(answerSequence.indexOf(1) + 1) : "");
                secondAnswerButton.setText(answerSequence.indexOf(2) != -1 ?
                        String.valueOf(answerSequence.indexOf(2) + 1) : "");
            }
        });

        HBox answerHbox = new HBox(20);
        answerHbox.setAlignment(Pos.BOTTOM_LEFT);
        answerHbox.getChildren().addAll(firstAnswerButton, secondAnswerButton, thirdAnswerButton);
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
        primaryStage.setHeight(500);
        primaryStage.show();
    }

    private void checkAnswer(String questionFileName, List<Integer> answer) {
        String[] questionFileNameSplit = questionFileName.split("_");

        String[] correctAnswerSequence = questionFileNameSplit[1].split("-");
        int[] correctAnswerArray = {Integer.parseInt(correctAnswerSequence[0]),
                Integer.parseInt(correctAnswerSequence[1]), Integer.parseInt(correctAnswerSequence[2])};

        boolean answerCorrectness = (answer.get(0) == correctAnswerArray[0]) && (answer.get(1) == correctAnswerArray[1])
                && (answer.get(2) == correctAnswerArray[2]);

        String message = "Incorrect!\nCorrect order: " + correctAnswerArray[0] + " "
                + correctAnswerArray[1] + " " + correctAnswerArray[2];
        if (answerCorrectness) {
            message = "Correct!";
            scoreCounter++;
        }

        accountUtils.saveToLog(currentAccount.getUserName(), Collections.singletonList(audioFilesFolder
                + " question " + questionCounter + "/5: " + message + " User answer: " + answer.toString()));

        Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
        resultAlert.setTitle("Question " + questionCounter + "/5");
        resultAlert.setHeaderText(null);
        resultAlert.setContentText(message);
        resultAlert.showAndWait();

        if (audioFileNames.size() > 0) {
            currentQuestionFileName = audioFileNames.get(0);
            audioFileNames.remove(0);
            questionCounter++;
            firstAnswerButton.setText("");
            firstAnswerButton.setSelected(false);
            secondAnswerButton.setText("");
            secondAnswerButton.setSelected(false);
            thirdAnswerButton.setText("");
            thirdAnswerButton.setSelected(false);
            answerSequence = new ArrayList<>();
            questionListenButton.setText("Listen to question " + questionCounter + "/5");
            questionLabel.setText("Order the words in increasing degree of quantity.\n" +
                    "Question " + questionCounter + "/5:");
            playSoundFile(audioFilesFolder + "/" + currentQuestionFileName);
        } else {
            accountUtils.saveToLog(currentAccount.getUserName(), Collections.singletonList("Finished, correct answers: "
                    + scoreCounter + "/5"));
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
                                   List<String> audioFileNames, Account account) {
        accountUtils.saveToLog(account.getUserName(), Collections.singletonList("Moved to " + audioFilesFolder
                + " listening exercise."));
        this.stage = primaryStage;
        this.audioFilesFolder = audioFilesFolder;
        this.audioFileNames = audioFileNames;
        currentAccount = account;
        currentQuestionFileName = audioFileNames.get(0);
        audioFileNames.remove(0);
        start(primaryStage);
    }
}
