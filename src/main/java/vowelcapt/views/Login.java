package vowelcapt.views;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import vowelcapt.utils.Account;
import vowelcapt.utils.AccountUtils;

import java.util.Optional;

public class Login extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setX(600);
        primaryStage.setY(100);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Welcome");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label userName = new Label("Username:");
        grid.add(userName, 0, 1);

        TextField userNameField = new TextField();
        grid.add(userNameField, 1, 1);

        Label userPassword = new Label("Password:");
        grid.add(userPassword, 0, 2);

        PasswordField passwordField = new PasswordField();
        grid.add(passwordField, 1, 2);

        Button signIn = new Button("Sign in");
        HBox hbBtn = new HBox(15);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(signIn);
        grid.add(hbBtn, 1, 4);

        Button register = new Button("Register new user");
        HBox registerHBox = new HBox(15);
        registerHBox.setAlignment(Pos.BOTTOM_RIGHT);
        registerHBox.getChildren().add(register);
        grid.add(registerHBox, 0, 4);

        final Text actiontarget = new Text();
        actiontarget.setFill(Color.FIREBRICK);
        grid.add(actiontarget, 1, 6);
        signIn.setOnAction(e -> {
            if (userNameField.getText().equals("")
                    || passwordField.getText().equals("")) {
                actiontarget.setText("All fields are mandatory!");
            } else {
                actiontarget.setText("");
                AccountUtils accountUtils = new AccountUtils();
                Optional<Account> accountOptional = accountUtils
                        .attemptLogin(userNameField.getText(), passwordField.getText());
                if (accountOptional.isPresent()) {
                    Account account = accountOptional.get();
                    account.setThreshold(accountUtils.getThreshold(account.getUserName()));
                    new ExerciseSelection().initializeAndStart(primaryStage, accountOptional.get());

                } else {
                    actiontarget.setText("Invalid username/password!");
                }
            }
        });

        final Text registerTarget = new Text();
        grid.add(registerTarget, 1, 7);
        register.setOnAction(e -> {
            registerTarget.setFill(Color.FIREBRICK);
            Register registerScreen = new Register();
            try {
                registerScreen.start(primaryStage);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setTitle("EstonianVowelCAPT - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
