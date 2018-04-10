package vowelcapt.views;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setTitle("EstonianVowelCAPT");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public void initializeAndStart(Stage primaryStage, Account account) {
        currentAccount = account;
        System.out.println("Exercise selection screen: " + currentAccount.toString());
        start(primaryStage);
    }
}
