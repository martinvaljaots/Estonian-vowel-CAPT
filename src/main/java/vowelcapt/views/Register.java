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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class Register extends Application {

    @Override
    public void start(Stage primaryStage) {

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Register user");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
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

        final Button registerBtn = new Button("Register");
        HBox hbBtn = new HBox(15);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(registerBtn);
        grid.add(hbBtn, 1, 5);

        Label sexSelectionLabel = new Label("Select sex:");
        grid.add(sexSelectionLabel, 0, 4);

        final ComboBox sexSelect = new ComboBox(FXCollections.observableArrayList("Male", "Female"));
        grid.add(sexSelect, 1, 4);

        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 6);
        registerBtn.setOnAction(e -> {
            if (!passwordField.getText().equals(passwordAgainField.getText())) {
                actiontarget.setFill(Color.FIREBRICK);
                actiontarget.setText("Passwords don't match!");
            } else if (userNameField.getText().equals("")
                    || passwordField.getText().equals("")
                    || passwordAgainField.getText().equals("")
                    || sexSelect.getValue() == null) {
                actiontarget.setFill(Color.FIREBRICK);
                actiontarget.setText("All fields are mandatory!");
            } else {
                System.out.println(sexSelect.getValue().toString());
                registerUser(userNameField.getText(), passwordField.getText(), sexSelect.getValue().toString());
                actiontarget.setFill(Color.GREEN);
                actiontarget.setText("Registered!");
            }
        });

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setTitle("EstonianVowelCAPT");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void registerUser(String userName, String password, String sex) {
        Path path = FileSystems.getDefault().getPath("resources/accounts/acc.csv");

        String newUserInfo = userName + ";" + password + ";" + sex.toLowerCase() + ";";
        System.out.println(newUserInfo);
        System.out.println(path.toString());

        List<String> newUserInfoLines = Arrays.asList(newUserInfo);

        try {
            Files.write(path, newUserInfoLines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new File("resources/accounts/" + userName).mkdirs();


    }

    public static void main(String[] args) {
        launch(args);
    }
}
