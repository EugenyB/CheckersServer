package checkers.client.main;

import javafx.scene.paint.Color;

public class GameConstants {
    public static final String FIELD = "Field:";
    public static final String MOVE = "Move:";
    public static final String WINNER = "Winner:";
    public static final String WELCOME = "welcome";
    public static final String SORRY = "sorry";

    public static final int IMPOSSIBLE = 9;

    public static final int H = 17;
    public static final int W = 25;

    public static final Color[] COLORS = {
        Color.GRAY, Color.BLUE, Color.RED, Color.rgb(0,255,0), Color.CYAN, Color.MAGENTA, Color.YELLOW
    };

    public static final Color[] COLORS_3 = {
            Color.GRAY, Color.BLUE, Color.rgb(0,255,0), Color.CYAN
    };

    public static final Color[] COLORS_4 = {
            Color.GRAY, Color.BLUE, Color.MAGENTA, Color.RED, Color.CYAN
    };

    public static final Color[] COLORS_6 = {
            Color.GRAY, Color.BLUE, Color.MAGENTA,  Color.rgb(0,255,0), Color.RED, Color.CYAN, Color.YELLOW
    };
}
