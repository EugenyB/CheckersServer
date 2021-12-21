package checkers.client.main;

import javafx.scene.paint.Color;
import lombok.Data;

import static checkers.client.main.GameConstants.COLORS;

@Data
public class Piece {
    private int row;
    private int column;
    private Color color;
    private boolean selected;

    public Piece(int row, int column, int colorNum) {
        this.row = row;
        this.column = column;
        this.color = COLORS[colorNum];
        selected = false;
    }

}
