package checkers.client.main.util;

import javafx.stage.Stage;

/**
 * Contains info of window on screen position. Singleton
 */
public class GameParameters {
    private static GameParameters instance;

    /**
     * Stage of game (Primary Stage of JavaFX App)
     */
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
