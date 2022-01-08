package checkers.client.main.util;

import javafx.stage.Stage;

public class GameParameters {
    private static GameParameters instance;

    private Stage stage;

    private GameParameters(){}

    public static GameParameters getInstance() {
        if (instance == null) {
            instance = new GameParameters();
        }
        return instance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public double getX() {
        return stage.getX();
    }

    public double getY() {
        return stage.getY();
    }
}
