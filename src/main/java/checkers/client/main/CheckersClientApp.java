package checkers.client.main;

import checkers.client.main.util.GameParameters;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main class for start Client Application
 */
public class CheckersClientApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(CheckersClientApp.class.getResource("checkers.fxml"));
        Scene scene = new Scene(loader.load(), 521, 695);
        primaryStage.setTitle("Chinese checkers");
        primaryStage.setScene(scene);
        GameParameters.getInstance().setStage(primaryStage);
        primaryStage.show();
    }
}
