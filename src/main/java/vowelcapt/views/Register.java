package vowelcapt.views;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import vowelcapt.utils.Account;
import vowelcapt.utils.AccountUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

public class Register extends Application {

    private AccountUtils accountUtils = new AccountUtils();

    @Override
    public void start(Stage primaryStage) {

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Register user");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 24));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label userName = new Label("Enter username:");
        grid.add(userName, 0, 1);

        TextField userNameField = new TextField();
        grid.add(userNameField, 1, 1);

        Label userPassword = new Label("Enter password:");
        grid.add(userPassword, 0, 2);

        PasswordField passwordField = new PasswordField();
        grid.add(passwordField, 1, 2);

        Label userPasswordAgain = new Label("Re-enter password:");
        grid.add(userPasswordAgain, 0, 3);

        PasswordField passwordAgainField = new PasswordField();
        grid.add(passwordAgainField, 1, 3);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            Login login = new Login();
            login.start(primaryStage);
        });
        grid.add(backButton, 0, 5);

        final Button registerBtn = new Button("Register");
        HBox hbBtn = new HBox(15);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(registerBtn);
        grid.add(hbBtn, 1, 5);

        Label genderSelectionLabel = new Label("Select gender:");
        grid.add(genderSelectionLabel, 0, 4);

        final ComboBox genderSelect = new ComboBox<>(FXCollections.observableArrayList("Male", "Female"));
        Tooltip genderToolTip = new Tooltip("Gender is used to evaluate\nyour pronunciation.");
        genderSelect.setTooltip(genderToolTip);
        grid.add(genderSelect, 1, 4);

        Alert continuationAlert = new Alert(Alert.AlertType.INFORMATION);
        continuationAlert.setTitle("Account registered");
        continuationAlert.setHeaderText(null);
        continuationAlert.setContentText("Account registered successfully!\n" +
                "You will now be guided through adjusting your microphone volume.");

        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 6);
        registerBtn.setOnAction(e -> {
            if (accountUtils.accountExists(userNameField.getText())) {
                actiontarget.setFill(Color.FIREBRICK);
                actiontarget.setText("Username already in use!");
            }
            else if (!passwordField.getText().equals(passwordAgainField.getText())) {
                actiontarget.setFill(Color.FIREBRICK);
                actiontarget.setText("Passwords don't match!");
            } else if (userNameField.getText().equals("")
                    || passwordField.getText().equals("")
                    || passwordAgainField.getText().equals("")
                    || genderSelect.getValue() == null) {
                actiontarget.setFill(Color.FIREBRICK);
                actiontarget.setText("All fields are mandatory!");
            } else {
                actiontarget.setText("");
                Account account = new Account(userNameField.getText(), passwordField.getText(),
                        genderSelect.getValue().toString().toLowerCase());
                registerUser(account);
                continuationAlert.showAndWait();
                ThresholdSetter thresholdSetter = new ThresholdSetter();
                thresholdSetter.initializeAndStart(primaryStage, account, true);
            }
        });

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setTitle("EstonianVowelCAPT - Registration");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void registerUser(Account account) {
        Path path = FileSystems.getDefault().getPath("resources/accounts/acc.csv");

        String newUserInfo = account.getUserName() + ";" + account.getPassword() + ";" + account.getGender();
        System.out.println(newUserInfo);
        System.out.println(path.toString());

        List<String> newUserInfoLines = Collections.singletonList(newUserInfo);

        try {
            Files.write(path, newUserInfoLines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new File("resources/accounts/" + account.getUserName()).mkdirs();
        try {
            new File("resources/accounts/" + account.getUserName() + "/log.txt").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
